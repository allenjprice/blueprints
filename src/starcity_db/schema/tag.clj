(ns starcity-db.schema.tag
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]))

(def ^{:added "1.4.0"} schema
  (s/generate-schema
   [(s/schema
     tag
     (s/fields
      [text :string :fulltext
       "The tag text itself."]

      [category :keyword :index
       "A keyword that categorizes this tag."]))]))

(defn norms [part]
  {:schema.tag/add-schema-02242017
   {:txes [schema]}})
