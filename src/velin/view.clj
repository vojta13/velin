(ns velin.view
  (:use velin.utils
        )
  (:require
    [hiccup.page :as page])

  )

(defn index [mapOfArguments]
  (page/html5
    [:head [:title "Hello World"]
     [:script {:src "something"}]]
    [:body [:h1 (:h1 mapOfArguments)]]
    )
  )

(defn create-progress-bar
  [server stat]
  [:div {:class "progress"}
   [:div {:class "progress-bar" :id (velin.utils/get-stats-id server stat) :role "progressbar" :aria-valuemin (:min-value stat) :aria-valuemax (:max-value stat) :aria-valuenow 0 :style "width: 90%"}
    "60"
    ]
   ]
  )

(defn get-list-of-instances
  [instances stat]
  (for [server instances]
    (do
      [:span (:host server) (create-progress-bar server stat)]
      )
    )
  )

(defn create-panel
  [heading-text & body]
  [:div {:class "panel panel-default"}
   [:div {:class "panel-heading"} heading-text]
   [:div {:class "panel-body"} body]
   ]
  )

(defn get-environments
  [instances]
  (set (map
         :environment
         instances))
  )

(defn get-list-of-envs-panels
  [envs instances stat]
  (for [env envs]
    (let [instances-in-env (filter #(= env (:environment %)) instances)]
      (create-panel (:name env)
                    (get-list-of-instances instances-in-env stat)
                    ))
    )
  )

(defn create-one-stats-panel
  [stats app]
  (map
    (fn [stat]
      (let [envs (get-environments (:instances app))]
        [:div {:class "col-xs-2"}
         (create-panel (:name stat)
                       (get-list-of-envs-panels envs (:instances app) stat)
                       )
         ]
        ))
    stats)
  )

(defn list-server-names
  [app]
  (for [server (:instances app)]
    [:span {:class "label label-default" :id (velin.utils/get-health-id server app)} (str (:host server))]
    )
  )

(defn create-application-panel
  [application]
  (let [stats (:statistics application)]
    (cons (str (:name application) ": ")
          (cons (list-server-names application)
                (for [list-of-stats (partition 6 6 nil stats)]
                  [:div {:class "row"}
                   (create-one-stats-panel list-of-stats application)]
                  )))))

(defn limit-apps-to-env-group
  [apps selected-env-group]
  (let [selected-envs-set (set (:environments selected-env-group))]
    (filter

      (fn [app] (not-empty (:instances app)))
      (map
        (fn [app] (velin.utils/->Application (:id app)
                                (:name app)
                                (:statistics app)
                                (filter (fn [instance] (some #(= % (:environment instance)) selected-envs-set))
                                        (:instances app))
                                (:health-chech-url app)))
        apps)
  )))

(defn list-apps [apps env-groups]
  (page/html5
    [:head [:title "Velin"]
     [:script {:src "/static/js/jquery-3.1.1.js"}]
     [:script {:src "/static/js/mine.js"}]
     [:script {:src "/static/js/bootstrap.min.js"}]
     [:link {:href "/static/css/bootstrap.min.css" :rel "stylesheet"}]
     [:link {:href "/static/css/mine.css" :rel "stylesheet"}]
     ]
    [:body
     [:div {:class "fluid-container"}
      [:ul {:class "nav nav-tabs" :role "tablist"}
       (for [env env-groups]
         [:li {:role "presentation"}
          [:a {:href (str "#" (:name env)) :aria-controls (:name env) :role "tab" :data-toggle "tab"} (:name env)]
          ]
         )
       ]
      [:div {:class "tab-content"}
       (for [selected-env-group env-groups]
         [:div {:class "tab-pane" :role "tabpanel" :id (:name selected-env-group)}
          (for [app (limit-apps-to-env-group apps selected-env-group)]
            (create-application-panel app))
          ])
       ]]]
    )
  )