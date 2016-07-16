(ns user
  (:require [immutable.main :refer [start-app stop-app]]
            [ring.adapter.jetty :as jetty]))

(def server (atom nil))

(defn start-server
  "Start server reloadable"
  ([] (start-server 3000))
  ([port] (reset! server (start-app port true))))

(defn stop-server
  []
  (do
    (stop-app @server)))

(defn restart
  []
  (do
    (stop-server)
    (start-server)))
