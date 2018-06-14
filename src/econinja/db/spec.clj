(ns econinja.db.spec
  (:require [clojure.spec.alpha :as s]))

(def time-regex "(([01]?\\d)|(2[0-3])):[0-5]\\d")
(def date-regex "\\d{4}-((0\\d)|(1[0-2]))-(([0-2]\\d)|(3[01]))")
(def email-regex ".+(\\.+)*@(.+\\.)+[a-zA-Z]{2,}")
(def id-regex (str date-regex "F" time-regex "T" time-regex))

(s/def ::event-title string?)
(s/def ::event-id (s/and string? #(re-matches (re-pattern id-regex) %)))
(s/def ::time (s/and string? #(re-matches (re-pattern time-regex) %)))
(s/def ::date (s/and string? #(re-matches (re-pattern date-regex) %)))
(s/def ::email #(re-matches (re-pattern email-regex) %))
(s/def ::my-comment string?)

(s/def ::location string?)
(s/def ::start ::time)
(s/def ::end ::time)
(s/def ::max-participants int?)
(s/def ::cost (s/and int? pos?))
(s/def ::event
  (s/keys :req-un [::event-id
                   ::event-title
                   ::location
                   ::start
                   ::end
                   ::max-partixipants
                   ::cost]))

(s/def ::first-name string?)
(s/def ::last-name string?)
(s/def ::person
  (s/keys :req-un [::email
                   ::last-name
                   ::first-name]
          :opt-un [::my-comment]))

(s/def ::has-paid boolean?)
(s/def ::num-people (s/and int? pos?))
(s/def ::her-comment string?)
(s/def ::participant
  (s/keys :req-un [::email
                   ::event-id
                   ::num-people
                   ::has-paid]
          :opt-un [::her-comment ::my-comment]))
