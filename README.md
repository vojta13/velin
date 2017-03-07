# velin

A Clojure library designed to provide easy-to-setup runtime monitoring of an Java applications. It consists of three key parts:
- Gathering statistics from JMXs
- Checking specified health check endpoints (via http).
- What you want to monitor is defined in clojure by you and web UI is build from that specification.

The statistics are defined by you in clojure and velin takes care of building UI on top of your definition and runs the health checks and stats gathering for you.

Targeted audiance are mostly developers who don't have tool at their hand where they can just plug their stats (e.g. graphite + Grafana) but still want to see how their application
behaves during runtime.

"Velin" is a czech word for a "control room" that is why this name :).

## Usage

This initial version can be started by lein ring server or packaged in lein ring uberjar and then run as java -jar jarfile.jar. To be able to run it you need provide
it the definition of what you want to monitor as an JVM argument "definitionFile".

```
java -DdefinitionFile=pathToYourDefinitionFile -jar uberjarfile.jar
```
This will run server at the port 3000. The URL for the apps is server:3000/apps


### Definition file
Definition file is where you specify servers, environments, statistics, and applications. The logic how these related to each other can be observed from the definition:
```
(ns velin.utils)
(defrecord Statistics [id name bean-name value-name min-value max-value transform-function])

(defrecord Application [id name statistics instances health-check-path])

(defrecord Server [host port jmx-port environment])

(defrecord Environment [name ])

(defrecord Environment-group [name environments])
```

And example how you may define your environment is the following. The only mandatory element is the function with the returned map format at the end:

```
(def prod-a (velin.utils/->Environment "PROD-A"))

(def prod-b (velin.utils/->Environment "PROD-B"))

;Server is more like and application server running as you can see it is defined by host and port so one machine can have multiple servers on multiple ports
(def server-abc (velin.utils/->Server "server-host-1" 80 1234 prod-a))
(def server-def (velin.utils/->Server "server-host-2" 8090 1235 prod-a))
(def server-dgh (velin.utils/->Server "server-host-23" 8098 1235 prod-b))

(def number-of-request (velin.utils/->Statistics "nr" "Number of requests" "foo" "value" 0 100 identity))
(def testing-stat (velin.utils/->Statistics "pv" "Number of something else" "foo" "value" 0 100 identity))

;Application has set of statistics and set of servers where it runs. There is an assumption that an app exposes same statistics across environments.
(def my-awesome-app (velin.utils/->Application "ap1" "AwesomeApp" [number-of-request testing-stat] [server-abc server-def server-dgh] "/"))
(def my-awesome-app-2 (velin.utils/->Application "ap2" "AwesomeApp2" [testing-stat] [server-def] "/healthcheck"))

;Defined for grouping so that when you have your envA and envB you can have them both in one tab on the web UI
(def whole-prod (velin.utils/->Environment-group "PROD" [prod-a prod-b]))

(def system {:name       "this is my ultra mega super application"
             :envs       [prod-a prod-b]
             :env-groups [whole-prod]
             :apps       [my-awesome-app my-awesome-app-2]})

;THIS is the only thing that you have to define in the file and the return structure has to follow the exact map above
(defn get-system-definition
  []
  system
  )
```

### Hardcoded settings
In this initial version there are some hardcoded settings. The timeout for connecting to JMX is 4000ms. The same for health checks.

The stats gathering is done every 10s and healthcheck is done every 2s (I plan to have this all configurable).


## License

Copyright © 2017

Distributed under the Eclipse Public License version 1.0.
