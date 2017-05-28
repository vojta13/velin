(defproject velin "0.0.3"
  :description "FIXME: write description"
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.0"]
                 [ring/ring-defaults "0.1.5"]
                 [hiccup "1.0.5"]
                 [org.clojure/data.json "0.2.6"]
                 [clj-http "3.4.1"]
                 [org.clojure/java.jmx "0.3.3"]
                 [clojurewerkz/quartzite "2.0.0"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler velin.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
