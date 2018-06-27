(ns blueprints.schema.tipe
  (:require [datomic-schema.schema :as s]))

(def ^{:added "2.7.0"} schema
  (s/generate-schema
   [(s/schema
     tipe
     (s/fields
      [document-id :string :indexed
       "The document-id of a Tipe document."]))]))


(defn norms [part]
  {:schema.tipe/schema-06282018
   {:txes [schema]}})
