(ns blueprints.schema.payment
  "The `payment` entity represents a unified schema for all types of payments,
  be they for rent, security deposits, premium services, or anything else
  as-of-yet undetermined."
  (:refer-clojure :exclude [methods])
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]))


(def ^{:added "1.8.0"} schema
  (s/generate-schema
   [(s/schema
     payment
     (s/fields
      [id :uuid :unique-identity
       "UUID to identify this payment internally."]

      [method :ref :indexed
       "The payment method."]

      [amount :float :indexed
       "The amount of money that this payment is for, in USD."]

      [status :ref :indexed
       "Status of this payment."]

      [due :instant :indexed
       "Due date of this payment."]

      [account :ref :indexed
       "The account that this payment pertains to."]

      [for :ref :indexed
       "What this payment is for. Used to tell the system what kind of entity references this payment."]))]))


(defn- ^{:added "1.8.0"} methods [part]
  [{:db/id    (d/tempid part)
    :db/ident :payment.method/stripe-charge}
   {:db/id    (d/tempid part)
    :db/ident :payment.method/stripe-invoice}
   {:db/id    (d/tempid part)
    :db/ident :payment.method/check}])


(defn- ^{:added "1.8.0"} statuses [part]
  [{:db/id    (d/tempid part)
    :db/ident :payment.status/due}
   {:db/id    (d/tempid part)
    :db/ident :payment.status/canceled}
   {:db/id    (d/tempid part)
    :db/ident :payment.status/paid}
   {:db/id    (d/tempid part)
    :db/ident :payment.status/pending}
   {:db/id    (d/tempid part)
    :db/ident :payment.status/failed}])


(defn- ^{:added "1.8.0"} fors [part]
  [{:db/id    (d/tempid part)
    :db/ident :payment.for/rent}
   {:db/id    (d/tempid part)
    :db/ident :payment.for/deposit}
   {:db/id    (d/tempid part)
    :db/ident :payment.for/order}])


(def ^{:added "1.10.0"} add-check-ref
  (s/generate-schema
   [(s/schema
     payment
     (s/fields
      [check :ref :component :indexed
       "The check that this payment was made with."]))]))


(defn- ^{:added "1.11.0"} add-refunded-status [part]
  [{:db/id    (d/tempid part)
    :db/ident :payment.status/refunded}])


(defn- ^{:added "1.11.0"} add-other-method [part]
  [{:db/id    (d/tempid part)
    :db/ident :payment.method/other}])


(def ^{:added "1.11.0"} add-date-fields
  (s/generate-schema
   [(s/schema
     payment
     (s/fields
      [paid-on :instant :indexed
       "The instant at which this payment was paid. Needed for legacy reasons w/ `rent-payment.`"]
      [pstart :instant :indexed
       "The start date of the period that this payment corresponds to."]
      [pend :instant :indexed
       "The end date of the period that this payment corresponds to."]))]))


(def ^{:added "1.17.0"} add-property
  (s/generate-schema
   [(s/schema
     payment
     (s/fields
      [property :ref :indexed
       "The property that this payment is affiliated with."]))]))


(defn norms [part]
  {:schema.payment/add-schema-06292017
   {:txes [schema (methods part) (statuses part) (fors part)]}

   :schema.payment/add-check-ref-07202017
   {:txes [add-check-ref]}

   :schema.payment/improvements-08162017
   {:txes [(add-refunded-status part) add-date-fields (add-other-method part)]}

   :schema.payment/add-property-11262017
   {:txes [add-property]}})
