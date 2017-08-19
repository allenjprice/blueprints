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


(def ^{:added "1.7.3"} add-component-to-charge
  [{:db/id               :stripe/charge
    :db/isComponent      true
    :db.alter/_attribute :db.part/db}])


(def ^{:added "1.8.0"} schema-improvements
  (s/generate-schema
   [(s/schema
     stripe
     (s/fields
      [charge-id :string :unique-identity
       "The id of the Stripe charge."]

      [invoice-id :string :unique-identity
       "The id of the Stripe invoice."]))]))


(def ^{:added "1.11.0"} add-source-id
  (s/generate-schema
   [(s/schema
     stripe
     (s/fields
      [source-id :string :indexed
       "ID of a Stripe source."]))]))


(defn norms [part]
  {:schema.stripe/add-schema-04132017
   {:txes [schema]}

   :schema.stripe/add-component-to-charge-06152017
   {:txes     [add-component-to-charge]
    :requires [:schema.stripe/add-schema-04132017]}

   :schema.stripe/schema-improvements-06292017
   {:txes [schema-improvements]}

   :schema.stripe/add-source-id-08182017
   {:txes [add-source-id]}})
