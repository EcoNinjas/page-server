(ns econinja.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [econinja.vars :refer [get-var]]
            [clojure.java.shell :refer [sh]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-params]]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [hiccup.page :refer [html5]]))

(def users {"root" {:username "root"
                    :password (creds/hash-bcrypt "admin_password")
                    :roles #{::admin}}})
(defroutes app-routes
;;  (GET "/protected" req
;;       (friend/authorize #{::admin}
;;                         "Update initialised"))
  (PUT "/update" req
       (friend/authorize #{::admin}
                         ((sh "sh" (get-var :update-script)) :out)))
  (GET "/update" req
       ((sh "sh" (get-var :update-script)) :out))
  (route/files "/" {:root (get-var :page-root)})
  (route/not-found (html5 [:head [:title "EcoNinjas - 404 - Sie sind auf der falschen Fährte"]]
                          [:body {:background "/images/Hintergrund.jpg"
                                  :style "background-position:center top;"}
                           [:a {:href "/"}
                            [:div {:style "width: 100vw; height:100vh; position:fixed; left:0; bottom:0; display:flex; flex-direction:column; justify-content:center; align-items:center;"}
                             [:img {:src "/images/404.png"
                                    :style "max-height: 70vh; max-width: 70vw;opacity:0.8;"}]]]])))

(def app
  (-> (wrap-defaults app-routes api-defaults)
      (friend/authenticate
       {:credential-fn (partial creds/bcrypt-credential-fn users)
        :workflows [(workflows/interactive-form)
                    (workflows/http-basic :realm "Friend demo")]})
      wrap-json-params
      wrap-json-response))
