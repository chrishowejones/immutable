(ns immutable.main
  (:require [ring.adapter.jetty :as jetty]
            [immutable.handler :refer [app-handle]])
  (:gen-class))

(defn start-jetty
  ([port] (start-jetty port false))
  ([port reload?] (jetty/run-jetty (app-handle reload?) {:port port :join? false})))

(defn -main [& args]
  (start-jetty 8080))
