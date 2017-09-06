(ns blueprints.schema.suggestion
  (:require [datomic-schema.schema :as s]))


(def ^{:added "1.10.3"} schema
  (s/generate-schema
   [(s/schema
     suggestion
     (s/fields
      [city :string :indexed
       "The city that was suggested for Starcity to expand to."]

      [account :ref :indexed
       "The (optional) account of the suggester."]))]))


(defn norms [part]
  {:schema.suggestion/add-schema
   {:txes [schema]}})
