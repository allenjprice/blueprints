(ns blueprints.schema.order
  "The `order` entity represents a the purchase of a `service` by an `account`."
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]))


(def ^{:added "1.5.0"} schema
  (s/generate-schema
   [(s/schema
     order
     (s/fields
      [account :ref :indexed
       "The account that placed this order."]

      [uuid :uuid :unique-identity
       "UUID to identify this order."]

      [service :ref :indexed
       "The service that is being ordered."]

      [variant :ref :indexed
       "The service variant that was selected, if any."]

      [quantity :float :indexed
       "The number of `service` ordered."]

      [price :float :indexed
       "The price of a unit of this order. Used only in cases where a price cannot be specified on the service itself."]

      [desc :string :fulltext
       "Description of the order."]

      [ordered :instant :indexed
       "The instant in time at which this order was placed."]))]))


(def ^{:added "1.8.0"} order-improvements
  (s/generate-schema
   [(s/schema
     order
     (s/fields
      [payments :ref :many :indexed :component
       "Ref to payments associated with this order."]

      [status :ref :indexed
       "The status of this order."]))]))


;;; The way orders (ordering) should work:

;; Order begins life as "pending" when it is created.
;; An order is transitioned to "placed" when it is ready to be charged.

;; An event will be issued (e.g. `:order/place`) that creates the charge and, if
;; successful, transitions the order status to "charged". From that point on,
;; the charge's status will be used for status determination.


(defn- ^{:added "1.8.0"} add-order-statuses [part]
  [{:db/id    (d/tempid part)
    :db/ident :order.status/pending}
   {:db/id    (d/tempid part)
    :db/ident :order.status/placed}
   {:db/id    (d/tempid part)
    :db/ident :order.status/canceled}
   {:db/id    (d/tempid part)
    :db/ident :order.status/charged}])


(defn- ^{:added "1.11.0"} add-processing-status [part]
  [{:db/id    (d/tempid part)
    :db/ident :order.status/processing}])


(defn- ^{:added "1.13.0"} add-fulfilled-status [part]
  [{:db/id    (d/tempid part)
    :db/ident :order.status/fulfilled}])


(def ^{:added "1.13.0"} additions-10022017
  (s/generate-schema
   [(s/schema
     order
     (s/fields

      [billed-on :instant :indexed
       "The date at which this order was billed."]

      [fulfilled-on :instant :indexed
       "The date at which this order was fulfilled."]

      [projected-fulfillment :instant :indexed
       "The date at which this order is projected to be fulfilled."]

      [cost :float :indexed
       "The cost of this order--used in absence of service cost or to override service cost."]))]))


(defn norms [part]
  {:schema.order/add-schema-04132017
   {:txes [schema]}

   :schema.order/order-improvements-06292017
   {:txes [order-improvements (add-order-statuses part)]}

   :schema.order/add-processing-status-09132017
   {:txes [(add-processing-status part)]}

   :schema.order/additions-10022017
   {:txes [(add-fulfilled-status part)
           additions-10022017]}})
