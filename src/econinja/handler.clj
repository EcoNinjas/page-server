(ns econinja.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-params]]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))

(def users {"root" {:username "root"
                    :password (creds/hash-bcrypt "admin_password")
                    :roles #{::admin}}})
(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/protected" req
       (friend/authorize #{::admin}
                         "Hello private work"))
  (PUT "/update" req
       (friend/authorize #{::admin}
                         "Hello private update"))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (wrap-defaults app-routes api-defaults)
      (friend/authenticate
       {:credential-fn (partial creds/bcrypt-credential-fn users)
        :workflows [(workflows/interactive-form)
                    (workflows/http-basic :realm "Friend demo")]})
      wrap-json-params
      wrap-json-response))

;; (ns econinja.handler
;;   (:require [compojure.core :refer :all]
;;             [compojure.route :as route]
;;             [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
;;             [ring.middleware.json :refer [wrap-json-response wrap-json-params]]
;;             [environ.core :refer [env]]
;;             [cemerick.friend :as friend]
;;             (cemerick.friend [workflows :as workflows]
;;                              [credentials :as creds])))
;;
;; (def users {"root" {:username "root"
;;                     :password (creds/hash-bcrypt "password")
;;                     :roles #{::admin}}})
;;
;; (defroutes app-routes
;;   (GET "/secret"  {:keys [params]}
;;        (println "PUT received:" params)
;;        (friend/authorize #{::admin} "foo"))
;;   (GET "/" [] "Hello World")
;;   (route/resources "/" {:root (or (env :page-root) "public")})
;;   (route/not-found "Not Found"))
;;
;; (defroutes app-routes2
;;   (GET "/" [] "Hello World2")
;;   (GET "/protected" req
;;        (friend/authorize #{::admin}
;;                          "Hello private work"))
;;   (route/not-found "Not Found"))
;;
;; (def app
;;   (-> (wrap-defaults app-routes2 site-defaults)
;;       ; wrap-json-params
;;       ; (wrap-json-response)
;;       (friend/authenticate
;;        {:credential-fn (partial creds/bcrypt-credential-fn users)
;;         :workflow [(workflows/interactive-form)
;;                    (workflows/http-basic :realm "EcoNinjas")]})))
;;
