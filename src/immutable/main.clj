(ns immutable.main
  (:require [ring.adapter.jetty :as jetty]
            [immutable.handler :refer [app]]))

(defn -main [& args]
  (jetty/run-jetty app {:port 8080}))
