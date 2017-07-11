(ns blueprints.schema.event
  "The `event` entity replaces `cmd` and `msg`."
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
    :db/ident :event.status/failed}
   {:db/id    (d/tempid part)
    :db/ident :event.status/seen}])


(defn norms [part]
  {:schema.event/add-schema-06142017
   {:txes [schema (event-statuses part)]}})
