(ns blueprints.schema.asana
  (:require [datomic-schema.schema :as s]))

;; TODO: Version
(def ^{:added "2.6.0"} schema
  (s/generate-schema
   [(s/schema
     asana
     (s/fields
      [task :string :indexed
       "A link to an Asana task for tracking work that happens outside of our software"]
      [project :string :indexed
       "A link to an Asana project for tracking work that happpens outside of our software"]))]))


(defn norms [part]
  {:schema.asana/schema-05142018
   {:txes [schema]}})
