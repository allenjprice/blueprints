(ns starcity-db.schema.provenance
  (:require [datomic-schema.schema :as s]))

(def ^{:added "1.2.0"} schema
  (s/generate-schema
   [(s/schema
     provenance
     (s/fields
      [account :ref :index
       "Account associated with this transaction."]))]))
