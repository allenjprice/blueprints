(ns blueprints.schema.slack
  (:require [datomic-schema.schema :as s]))

(def ^{:added "2.6.0"} schema
  (s/generate-schema
   [(s/schema
     slack
     (s/fields
      [channel :string :indexed
       "The name of a Slack channel."]

      [user-id :string :indexed
       "The id of a Slack user."]))]))


(defn norms [part]
  {:schema.slack/schema-06072018
   {:txes [schema]}})
