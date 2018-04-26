(ns econinja.server
  (:gen-class)
  (:require [aleph.http :as http]
            [econinja.handler :as en]
            [environ.core :refer [env]]))

(defn -main [& _]
  (let [port (or (env :port) 8080)]
    (println "Starting aleph on port" port)
    (println "Serving from" (or (env :page-root) "public"))
    (http/start-server en/app {:port (Integer. port)})))
