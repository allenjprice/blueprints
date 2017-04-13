(ns blueprints.schema.stripe-customer
  (:require [datomic-schema.schema :as s]))

(def ^{:added "1.1.x"} schema
  (s/generate-schema
   [(s/schema
     stripe-customer
     (s/fields
      [customer-id :string :unique-identity
       "The id used by Stripe to represent this customer."]
      [account :ref
       "Reference to the account with which this Stripe customer is associated."]
      [bank-account-token :string
       "The Stripe bank account token for this customer's bank account."]))]))

(def ^{:added "1.2.0"} add-managed
  (s/generate-schema
   [(s/schema
     stripe-customer
     (s/fields
      [managed :ref :index
       "Reference to the property that manages this customer."]))]))

(defn norms [part]
  {:schema/add-stripe-customer-schema-8-30-16
   {:txes [schema]}

   :schema.stripe-customer/add-managed-12-14-16
   {:txes [add-managed]}})
