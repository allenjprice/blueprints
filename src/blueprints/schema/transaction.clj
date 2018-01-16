(ns blueprints.schema.transaction
  (:require [datomic-schema.schema :as s]))


(def ^{:added "1.18.0"} transaction-schema
  (s/generate-schema
   [(s/schema
     transaction
     (s/fields
      [source-id :string :unique-identity
       "Source-id of the balance transaction object."]
      [payment :ref :indexed
       "Reference to payment, if it exists."]
      [id :string :unique-identity
       "Id of the balance transaction."]
      [payout-id :string :unique-identity
       "Payout-id of the payout once it's been created."]))]))


(defn norms [part]
  {:schema.transaction/transaction-schema-01092018
   {:txes [transaction-schema]}})

(comment

  transaction-schema

  )
