(ns user
  (:require [immutable.main :refer [start-jetty]]
            [ring.adapter.jetty :as jetty]))

(def server (atom nil))

(defn start-server
  "Start server reloadable"
  ([] (start-server 3000))
  ([port] (reset! server (start-jetty port true))))

(defn stop-server
  []
  (do
    (.stop @server)
    (reset! server nil)))

(defn restart
  []
  (do
    (stop-server)
    (start-server)))
