(ns starcity-db.schema.license
  (:require [datomic-schema.schema :as s]))

(def ^{:added "1.0.0"} schema
  (s/generate-schema
   [(s/schema
     license
     (s/fields
      [term :long
       "The term of the license in months."]))]))

(def ^{:added "1.1.7"} add-available
  (s/generate-schema
   [(s/schema
     license
     (s/fields
      [available :boolean :index
       "Whether or not this license is available for new applicants."]))]))

(defn norms [part]
  {:starcity/add-license-schema
   {:txes [schema]}

   :license.schema/add-available-1-2-17
   {:txes [add-available]}})
