(ns velin.utils)


(defrecord Statistics [id name bean-name value-name min-value max-value transform-function])

(defrecord Application [id name statistics instances health-check-path])

(defrecord Server [host port jmx-port environment])

(defrecord Environment [name ])

(defrecord Environment-group [name environments])

(defn get-health-id
  [server app]
  (str "health-" (:id app) (.replace (:host server) "." "") (:port server))
  )

(defn get-stats-id
  [server stat]
  (str (.replace (:host server) "." "") (:port server) (:id stat))
  )

