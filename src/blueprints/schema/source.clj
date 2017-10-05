(ns blueprints.schema.source
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]))


(def ^{:added "1.13.0"} schema
  (s/generate-schema
   [(s/schema
     source
     (s/fields
      [account :ref :indexed
       "The account that initiated this transaction."]))]))


(defn norms [part]
  {:schema.source/add-schema-10032017
   {:txes [schema]}})
