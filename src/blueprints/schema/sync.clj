(ns blueprints.schema.sync
  (:require [datomic-schema.schema :as s]))


(def ^{:added "1.18.0"} add-sync-schema
  (s/generate-schema
   [(s/schema
     sync
     (s/fields
      [ref :ref :indexed
       "The entity that is being synced."]
      [ext-id :string :indexed :unique-identity
       "The id of the external representation of the synced entity (`ref`)."]
      [service :keyword :indexed
       "The service that `ref` is being synced with."]
      [last-synced :instant :indexed
       "The time at which the referenced entity was last synced."]))]))


(defn norms [part]
  {:schema.sync/add-sync-schema-01082018
   {:txes [add-sync-schema]}})
