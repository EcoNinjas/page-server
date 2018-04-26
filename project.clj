(defproject econinja (->> (java.util.Date.) (.format (java.text.SimpleDateFormat. "yyyy-MM-dd")))
  :description "web server for EcoNinja website"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]
                 [aleph "0.4.4"]
                 [environ "1.1.0"]
                 [com.cemerick/friend "0.2.3"]
                 [hiccup "1.0.5"]]
  :plugins [[lein-ring "0.9.7"]
            [lein-environ "1.1.0"]]
  :ring {:handler econinja.handler/app}
  :main econinja.server
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}
   :uberjar {:aot :all}})
