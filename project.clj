(defproject immutable "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring "1.5.0"]
                 [ring/ring-defaults "0.2.1"]
                 [org.clojure/java.jdbc "0.6.2-alpha1" :excludes [org.clojure/clojure]]
                 [com.h2database/h2 "1.4.192"]
                 [clojure.jdbc/clojure.jdbc-c3p0 "0.3.2"]]
  :plugins [[lein-ring "0.9.7"]]
  :main immutable.main
  :ring {:handler immutable.handler/app}
    :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]
         :source-paths ["dev"]
         :repl-options {:init-ns user}}
   :uberjar {:aot [immutable.main]}})
