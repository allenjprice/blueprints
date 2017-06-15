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


(defn norms [part]
  {:schema.stripe/add-schema-04132017
   {:txes [schema]}

   :schema.stripe/add-component-to-charge-06152017
   {:txes     [add-component-to-charge]
    :requires [:schema.stripe/add-schema-04132017]}})
