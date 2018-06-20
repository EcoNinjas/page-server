(ns econinja.db.spec
  (:require [clojure.spec.alpha :as s]))

(def time-regex "(([01]?\\d)|(2[0-3])):[0-5]\\d")
(def date-regex "\\d{4}-((0\\d)|(1[0-2]))-(([0-2]\\d)|(3[01]))")
(def email-regex ".+(\\.+)*@(.+\\.)+[a-zA-Z]{2,}")
(def id-regex (str date-regex "F" time-regex "T" time-regex))

(s/def ::eventtitle string?)
(s/def ::eventid (s/and string? #(re-matches (re-pattern id-regex) %)))
(s/def ::time (s/and string? #(re-matches (re-pattern time-regex) %)))
(s/def ::date (s/and string? #(re-matches (re-pattern date-regex) %)))
(s/def ::email #(re-matches (re-pattern email-regex) %))
(s/def ::mycomment string?)

(s/def ::location string?)
(s/def ::start ::time)
(s/def ::end ::time)
(s/def ::maxparticipants int?)
(s/def ::cost (s/and number? pos?))
(s/def ::event
  (s/keys :req-un [::eventid
                   ::eventtitle
                   ::location
                   ::start
                   ::end
                   ::maxparticipants
                   ::cost]))

(s/def ::firstname string?)
(s/def ::lastname string?)
(s/def ::person
  (s/keys :req-un [::email
                   ::lastname
                   ::firstname]
          :opt-un [::myComment]))

(s/def ::haspaid boolean?)
(s/def ::numpeople (s/and int? pos?))
(s/def ::hercomment string?)
(s/def ::participant
  (s/keys :req-un [::email
                   ::eventid
                   ::numpeople
                   ::haspaid]
          :opt-un [::hercomment ::mycomment]))
