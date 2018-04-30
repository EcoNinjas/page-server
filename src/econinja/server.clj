(ns econinja.server
  (:gen-class)
  (:require [aleph.http :as http]
            [econinja.handler :as en]
            [econinja.vars :refer [get-var]]
            [environ.core :refer [env]]))

(defn -main [& _]
  (let [port (Integer. (get-var :port))]
    (println "Starting aleph on port" port)
    (println "Serving from" (get-var :page-root))
    (http/start-server en/app {:port port})))
