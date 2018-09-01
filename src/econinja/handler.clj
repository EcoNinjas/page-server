(ns econinja.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [econinja.vars :refer [get-var]]
            [econinja.db.core :as db]
            [clojure.java.shell :refer [sh]]
            [ring.middleware.cors :refer  [wrap-cors]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-params]]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [hiccup.page :refer [html5]]))

(def users {"root" {:username "root"
                    :password (creds/hash-bcrypt "admin_password")
                    :roles #{::admin}}})

(defroutes api-routes
  (context
   "/api/users" []
   (GET "/" [] {:body {:users (db/get-all-people)}})
   (GET "/:email" [email] {:body {:user (first (db/get-person email))}}))
  (context
   "/api/events" []
   (GET "/" [] {:body {:events (db/get-all-events)}})
   (GET "/:eventid" [eventid] {:body {:events (db/get-event eventid)}})
   (GET "/:eventid/slots" [eventid] {:body {:available-slots (db/get-free-slots eventid)}})
   (GET "/:eventid/participants" [eventid] {:body {:people (db/get-participants eventid)}})))

(defroutes app-routes
  (GET "/update" req
       (println "Starting update...")
       (html5 [:ul (-> (sh "sh" (get-var :update-script))
                       (#(or (% :out) (% :err)))
                       (#(do (println "result" %) %))
                       (clojure.string/split #"\n")
                       (#(map (fn [item] [:li item]) %)))]))

  (POST "/update" req
        (println "Starting update...")
        (friend/authorize
         #{::admin}
         (html5 [:ul (-> (sh "sh" (get-var :update-script))
                         (#(or (% :out) (% :err)))
                         (#(do (println "result" %) %))
                         (clojure.string/split #"\n")
                         (#(map (fn [item] [:li item]) %)))])))


  (route/files "/" {:root (get-var :page-root)})
  (route/not-found (html5 [:head [:title "EcoNinjas - 404 - Sie sind auf der falschen Fährte"]]
                          [:body {:background "/images/Hintergrund.jpg"
                                  :style "background-position:center top;min-height:100vh;min-width:100vw"}
                           [:a {:href "/"}
                            [:div {:style "width: 100vw; height:100vh; position:fixed; left:0; bottom:0; display:flex; flex-direction:column; justify-content:center; align-items:center;"}
                             [:img {:src "/images/404.png"
                                    :style "max-height: 70vh; max-width: 70vw;opacity:0.8;"}]]]])))

(def app
  (-> (routes api-routes app-routes)
      (wrap-defaults api-defaults)
      (friend/authenticate
       {:credential-fn (partial creds/bcrypt-credential-fn users)
        :workflows [(workflows/interactive-form)
                    (workflows/http-basic :realm "Friend demo")]})
      wrap-json-params
      wrap-json-response
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:post])))
