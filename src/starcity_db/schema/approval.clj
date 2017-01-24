(ns starcity-db.schema.approval
  (:require [datomic-schema.schema :as s]))

(def ^{:added "< 1.1.3"} schema
  (s/generate-schema
   [(s/schema
     approval
     (s/fields
      [account :ref
       "The account that is being approved."]
      [approved-by :ref
       "Administrator that approved this account."]
      [approved-on :instant
       "Instant at which this approval was made."]
      [property :ref
       "The property that this account is being approved for."]))]))

(defn norms [part]
  {:schema/add-approval-schema-9-8-16
   {:txes [schema]}})
