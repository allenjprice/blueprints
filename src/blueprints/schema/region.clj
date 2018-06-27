(ns blueprints.schema.region
  (:require [datomic-schema.schema :as s]))


(def ^{:added "2.7.0"} schema
  (s/generate-schema
   [(s/schema
     region
     (s/fields
      [name :string :indexed
       "The name of this region"]))]))


(defn norms [part]
  {:schema.region/schema-06282018
   {:txes [schema]}})
