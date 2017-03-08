(ns velin.handler
  (:use velin.view
        velin.utils)
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.coercions :as coerc]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [hiccup.core :as h]
            [hiccup.page :as page]
            [clojure.data.json :as json]
            [clj-http.client :as client]
            [clojure.java.jmx :as jmx]
            [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.schedule.cron :refer [schedule cron-schedule]])
  (:import (java.time LocalDateTime)))

(def health {})
(def stats {})

(if (System/getProperty "definitionFile")
  (load-file (System/getProperty "definitionFile"))
  )

(def system {})



(System/setProperty "sun.rmi.transport.tcp.responseTimeout", "4000")
(System/setProperty "sun.rmi.transport.connectionTimeout", "4000")
(System/setProperty "sun.rmi.transport.tcp.handshakeTimeout", "4000")

(defn query-for-jmx
  [server stat]
  {:id    (velin.utils/get-stats-id server stat)
   :value ((:transform-function stat) (get
                                        (jmx/mbean (:bean-name stat))
                                        (:value-name stat)))
   }
  )

(defn get-jmx-connection-map
  [server]
  (let [base-map {:host (:host server), :port (:jmx-port server)}]
    (if (:jmx-username server)
      (conj base-map {:environment {"jmx.remote.credentials" (into-array String [(:jmx-username server) (:jmx-password server)])}})
      base-map
      )
    )
  )

(defn get-data-for-app
  [app]
  (let [instances (:instances app)
        stats (:statistics app)]
    (flatten (pmap
               (fn [server]
                 (try
                   (jmx/with-connection (get-jmx-connection-map server)
                                        (doall (pmap
                                                 (fn [stat] (query-for-jmx server stat))
                                                 stats)
                                               ))
                   (catch Exception e
                     (map
                       (fn [stat] {:id    (velin.utils/get-stats-id server stat)
                                   :value -1
                                   })
                       stats)
                     ))

                 )
               instances))
    ))

(defn get-data
  []
  (flatten
    (pmap
      get-data-for-app
      (:apps system)))
  )

(defn check-healthyness
  [server app]
  (try
    (let [url (str "http://" (:host server) ":" (:port server) (:health-check-path app))
          response (client/get url {:socket-timeout 4000 :conn-timeout 4000})
          status (:status response)]
      (and (<= 200 status) (> 300 status)
           ))
    (catch Exception e
      false
      ))
  )

(defn get-health
  []
  (mapcat
    (fn [app]
      (pmap
        (fn [server] {:id (velin.utils/get-health-id server app) :value (check-healthyness server app)})
        (:instances app))
      )
    (:apps system)
    )
  )

(defjob HealthCheck
        [ctx]
        (println "Checking health")
        (def health {:last_update (.toString (LocalDateTime/now)) :data (get-health)})
        )

(defjob StatsCheck
        [ctx]
        (println "Getting stats")
        (def stats {:last_update (.toString (LocalDateTime/now)) :data (get-data)})
        )

(def scheduled false)

(defn start-schedule
  []
  (if-not scheduled
    (let [s (-> (qs/initialize) qs/start)
          job-health (j/build
                       (j/of-type HealthCheck)
                       (j/with-identity (j/key "jobs.health.1")))
          job-stats (j/build
                      (j/of-type StatsCheck)
                      (j/with-identity (j/key "jobs.stats.1")))
          tk-health (t/key "healthTrigger")
          tk-stats (t/key "statsTrigger")
          trigger-health (t/build
                           (t/with-identity tk-health)
                           (t/start-now)
                           (t/with-schedule (schedule
                                              (cron-schedule "*/2 * * * * ?"))))
          trigger-stats (t/build
                          (t/with-identity tk-stats)
                          (t/start-now)
                          (t/with-schedule (schedule
                                             (cron-schedule "*/10 * * * * ?"))))]
      ;(if-not (qs/get-job s tk-health)
      (qs/schedule s job-health trigger-health)
      ;)
      ;(if-not (qs/get-job s tk-stats)
      (qs/schedule s job-stats trigger-stats)
      ;)
      (def scheduled true)
      )
    )
  )

(defroutes app-routes
           (GET "/apps" []
             (load-file (System/getProperty "definitionFile"))
             (let [fresh-system (get-system-definition)]
               (def system fresh-system)
               (start-schedule)
               (list-apps (:apps fresh-system) (:env-groups fresh-system)))
             )
           (GET "/stats" [] (json/write-str stats))
           (GET "/health" [] (json/write-str health))
           (route/resources "/static")
           (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))



