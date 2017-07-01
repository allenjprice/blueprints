(ns blueprints.schema.stripe
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]))


(def ^{:added "1.5.0"} schema
  "Generic attributes to be used across entities in that use Stripe."
  (s/generate-schema
   [(s/schema
     stripe
     (s/fields
      [plan-id :string :unique-identity
       "The external id of a Stripe plan."]

      [subs-id :string :unique-identity
       "The external id of a Stripe subscription."]

      [charge :ref :indexed
       "Reference to a Stripe `charge` entity."]))]))


(def ^{:added "1.8.0"} schema-improvements
  (s/generate-schema
   [(s/schema
     stripe
     (s/fields
      [charge-id :string :unique-identity
       "The id of the Stripe charge."]

      [invoice-id :string :unique-identity
       "The id of the Stripe invoice."]))]))


(defn norms [part]
  {:schema.stripe/add-schema-04132017
   {:txes [schema]}

   :schema.stripe/schema-improvements-06292017
   {:txes [schema-improvements]}})
