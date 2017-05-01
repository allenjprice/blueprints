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

(defn norms [part]
  {:schema.stripe/add-schema-04132017
   {:txes [schema]}})
