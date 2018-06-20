(ns econinja.db.core
  (:require [clojure.java.jdbc :as sql]
            [clojure.spec.alpha :as s]
            [econinja.db.spec :as spec]
            [econinja.db.querystrings :as query]
            [econinja.vars :refer [get-var]]))

(def pg-db (get-var :database-url))

(defn create-table-if-not-exists
  [table-name columns]
  (let [table-exists
        (->> (sql/query pg-db query/find-public-tables)
             (filter #(= (name table-name) (:table_name %)))
             first)]
    (if-not table-exists
      (do (prn "Table" table-name "not found, creating it...")
          (sql/db-do-commands
           pg-db
           (sql/create-table-ddl (keyword table-name) columns))
          true)
      (prn "Table" table-name "found in database"))))

(s/fdef create-table-if-not-exists
        :args (s/cat :table-name keyword?
                     :columns (s/and seq
                                     #(every? (s/tuple keyword? string?) %))))

(defn maybe-init []
  (let [name-type    "varchar(50)"
        title-type   "varchar(100)"
        comment-type "varchar(150)"
        id-type      "char(22)"
        date-type    "char(10)"
        time-type    "char(5)"]
    (try
     (do (create-table-if-not-exists
          :events
          [[:eventid id-type]
           [:eventtitle title-type]
           [:location title-type]
           [:date date-type]
           [:start time-type]
           [:finish time-type]
           [:maxparticipants "int"]
           [:cost "money"]])
         (create-table-if-not-exists
          :people
          [[:email title-type]
           [:lastname name-type]
           [:firstname name-type]
           [:mycomment comment-type]])
         (create-table-if-not-exists
          :participants
          [[:email title-type]
           [:eventid id-type]
           [:numpeople "int"]
           [:haspaid "boolean"]
           [:hercomment comment-type]
           [:mycomment comment-type]]))
     (catch Exception e
       (println "While trying to initialize database:" (.toString e))))))

(defn dev-reset-db!- []
  (try
    (sql/with-db-transaction [db pg-db]
      (sql/execute! db ["DROP TABLE events"])
      (sql/execute! db ["DROP TABLE people"])
      (sql/execute! db ["DROP TABLE participants"]))
    (catch Exception _ nil))
  (maybe-init))

(s/fdef gen-id
        :args (s/keys :req-un [:spec/date :spec/start :spec/end])
        :ret  string?)
(defn gen-id [{:keys [date start finish]}]
  (str date "F" start "T" finish))

(s/fdef minimum-event
        :args (s/cat :event :spec/event)
        :ret (partial s/valid? :spec/event))
(defn minimum-event [event]
  (select-keys event [:eventtitle :location :date :start :finish :maxparticipants :cost]))

(s/fdef minimum-person
        :args (s/cat :person :spec/person)
        :ret (partial s/valid? :spec/person))
(defn minimum-person
  "Reduce person map so it contains only necessary fields"
  [person]
  (select-keys person [:email :firstname :lastname :mycomment]))

(defn minimum-enroll-data [data]
  (println "working on" data)
  (-> data
      (select-keys [:email :eventid :numpeople :hercomment :mycomment])
      (update :numpeople #(or % 1))))

(defn maybe-create-event
  [event]
  (sql/with-db-transaction [db pg-db]
    (let [event (-> (minimum-event event)
                   (assoc :eventid (gen-id event)))
          {:keys [eventid]} event
          existing-events (sql/query db (query/find-event eventid))]
     (if (empty? existing-events)
       (sql/insert! db :events event))
     event)))

(s/fdef maybe-create-person
        :args (s/cat :person :spec/person))
(defn maybe-create-person
  [{:keys [email] :as person}]
  (sql/with-db-transaction [db pg-db]
    (let [person (minimum-person person)
          existing-people (sql/query db (query/find-person email))]
      (println (count existing-people) (empty? existing-people))
      (if (empty? existing-people)
        (sql/insert! db :people person))))
  person)

(s/fdef get-free-slots
        :args (s/cat :eventid :spec/eventid)
        :return number?)
(defn get-free-slots [event-id]
  {:pre [(s/valid? :econinja.db.spec/eventid event-id)]
   :post [number?]}
  (sql/with-db-connection [db pg-db]
    (let [participants (or (->> (sql/query db (query/find-participants-of event-id))
                                (map :numpeople)
                                (reduce +))
                           0)
          event (-> (sql/query db (query/find-event event-id))
                    first)
          max-participants (:maxparticipants event)]
      (println event \newline max-participants)
      (cond (empty? event) 0
            (or (nil? max-participants) (neg? max-participants)) Float/POSITIVE_INFINITY
            (every? (complement nil?) [participants max-participants]) (- max-participants participants)
            :else :db-error))))


(s/fdef maybe-enroll
        :args (s/cat :person (s/and :spec/person #(s/valid? :spec/eventid (:event-id %))))
        :ret #{:already-enrolled :success :no-more-slots})
(defn maybe-enroll
  [{:keys [email eventid] :as person}]
  {:pre [(s/valid? :econinja.db.spec/person person) (:eventid person)]}
  (maybe-create-person person)
  (println "person created")
  (let [n (or (:numpeople person) 1)]
    (if (>= (get-free-slots eventid) n)
      (sql/with-db-transaction [db pg-db]
        (if-not (empty? (sql/query db (query/find-person-in-event (person :email) (person :eventid))))
          :already-enrolled
          (do (sql/insert! db :participants (minimum-enroll-data person))
              :success)))
      :no-more-slots)))

(s/fdef get-participants
        :args (s/cat :eventid :spec/eventid)
        :ret (s/coll-of :spec/person))
(defn get-participants [event-id]
  (sql/query pg-db (query/find-participants-of event-id)))

(defn get-all-people []
  {:post [#(s/valid? (s/coll-of :econinja.db.spec/person) %)]}
  (sql/query pg-db ["SELECT * FROM people"]))
