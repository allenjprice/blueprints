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


(def ^{:added "1.17.0"} schema-overhaul
  (concat
   [;; renamed attributes
    {:db/id    :stripe-customer/customer-id
     :db/ident :customer/platform-id
     :db/doc   "The Stripe id for the Stripe platform customer."}
    {:db/id    :stripe-customer/account
     :db/ident :customer/account
     :db/doc   "Reference to the account with which this customer is associated."
     :db/index true}
    ;; deprecations
    {:db/id  :stripe-customer/bank-account-token
     :db/doc "DEPRECATED 12042017: prefer direct access to Stripe API."}
    {:db/id  :stripe-customer/managed
     :db/doc "DEPRECATED 12042017: prefer queryies via `:customer/connected`"}]

   (s/generate-schema
    [(s/schema
      customer
      (s/fields
       [connected :ref :indexed :many :component
        "Reference to `:connected-customer` entities."]))

     (s/schema
      connected-customer
      (s/fields
       [customer-id :string :unique-identity
        "The id used by Stripe to represent this customer on a Connect account."]
       [property :ref :indexed
        "Reference to the property that this customer is connected to."]))])))


(defn norms [part]
  {:schema/add-stripe-customer-schema-8-30-16
   {:txes [schema]}

   :schema.stripe-customer/add-managed-12-14-16
   {:txes [add-managed]}

   :schema.customer/schema-overhaul-12042017
   {:txes [schema-overhaul]
    :requires [:schema/add-stripe-customer-schema-8-30-16
               :schema.stripe-customer/add-managed-12-14-16]}})
