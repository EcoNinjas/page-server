(ns econinja.db.querystrings
  (:require [clojure.spec.alpha :as s]
            [econinja.db.spec :as spec]))

(def find-public-tables
  ["SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'"])

(s/fdef find-event
        :args (s/cat :id :spec/eventId)
        :ret string?)
(defn find-event [id]
  ["SELECT * FROM events WHERE eventId = ?" id])

(s/fdef find-person
        :args (s/cat :email :spec/email)
        :ret string?)
(defn find-person [email]
  [(str "SELECT * FROM people WHERE email = '" email "'")])

(s/fdef find-person-in-event
        :args (s/cat :email :spec/email :eventId :spec/eventId)
        :ret string?)
(defn find-person-in-event [email eventId]
  [(str "SELECT * FROM participants WHERE email = '" email "' and eventid ='" eventId "'")])

(s/fdef find-participants-of
        :args (s/cat :eventId :spec/eventId)
        :ret (s/coll-of :spec/person))
(defn find-participants-of [eventId]
  [(str "SELECT * from participants WHERE eventid = '" eventId "'")])
