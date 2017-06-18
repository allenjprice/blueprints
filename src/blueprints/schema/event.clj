(ns blueprints.schema.event
  "The `event` entity will replace `cmd` and `msg`."
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]))


(def ^{:added "1.8.0"} schema
  (s/generate-schema
   [(s/schema
     event
     (s/fields
      [uuid :uuid :unique-identity
       "UUID to uniquely identify this event."]

      [id :string :unique-identity
       "An external (to the system) identifier for this event."]

      [key :keyword :indexed
       "Identifies the event for dispatching."]

      [topic :keyword :indexed
       "Categorizes the event."]

      [triggered-by :ref :indexed
       "Reference to the event that spawned this event."]

      [params :string
       "The serialized parameters for the handler."]

      [meta :string
       "Serielized metadata about this event."]

      [status :ref :indexed
       "pending, successful, failed"]))]))


(defn ^{:added "1.8.0"} event-statuses [part]
  [{:db/id    (d/tempid part)
    :db/ident :event.status/pending}
   {:db/id    (d/tempid part)
    :db/ident :event.status/successful}
   {:db/id    (d/tempid part)
    :db/ident :event.status/failed}])


(defn- ^{:added "1.8.0"} add-create-event [part]
  [{:db/id    (d/tempid part)
    :db/ident :db.event/create
    :db/doc   "Create a new event."
    :db/fn
    (datomic.function/construct
     {:lang     "clojure"
      :params   '[db key opts]
      :requires '[[datomic.api :as d]]
      :code     '(let [{:keys [id params meta topic triggered-by]} opts]
                   [(merge
                     {:db/id        (d/tempid :db.part/starcity)
                      :event/uuid   (d/squuid)
                      :event/key    key
                      :event/status :event.status/pending}
                     (when (some? id) {:event/id id})
                     (when (some? topic) {:event/topic topic})
                     (when (some? triggered-by) {:event/triggered-by triggered-by})
                     (when (some? params) {:event/params (pr-str params)})
                     (when (some? meta) {:event/meta (pr-str meta)}))])})}])


(defn norms [part]
  {:schema.event/add-schema-06142017
   {:txes [schema
           (event-statuses part)
           (add-create-event part)]}})
