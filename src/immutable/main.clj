(ns immutable.main
  (:gen-class)
  (:require [immutable.handler :refer [app-handle create-person-table drop-tables]]
            [ring.adapter.jetty :as jetty]))

(defn start-jetty
  ([port] (start-jetty port false))
  ([port reload?] (jetty/run-jetty (app-handle reload?) {:port port :join? false})))


(defn setup-db
  []
  (create-person-table))

(defn tear-down-db
  []
  (drop-tables))

(defn start-app
  ([port] (start-app port false))
  ([port reload?] (let [server (start-jetty port)]
                    (setup-db)
                    server)))

(defn stop-app
  [server]
  (tear-down-db)
  (.stop server))

(defn -main [& args]
  (start-jetty 8080))
