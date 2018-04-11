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


(def ^{:added "1.14.0"} rename-desc-attr
  [{:db/id               :order/desc
    :db/ident            :order/request
    :db/doc              "Accompanying text with the order request, provided by user."
    :db.alter/_attribute :db.part/db}])


(def ^{:added "1.14.0"} add-summary-and-line-items
  (s/generate-schema
   [(s/schema
     order
     (s/fields
      [summary :string :fulltext
       "Summary of the order provided by Starcity."]

      [lines :ref :many :indexed :component
       "Line-items attached to this order."]))

    (s/schema
     line-item
     (s/fields
      [desc :string :fulltext
       "Description of the line-item."]

      [cost :float :indexed
       "Cost of this line-item."]

      [price :float :indexed
       "Price of this line-item."]))]))


(defn- ^{:added "1.14.0"} add-failed-status [part]
  [{:db/id    (d/tempid part)
    :db/ident :order.status/failed}])


(def ^{:added "2.4.0"} add-order-fields
  (concat
   (s/generate-schema
    [(s/schema
      order
      (s/fields
       [fields :ref :indexed :many :component
        "Information collected from service fields while ordering"]))])

   [{:db/id                 (d/tempid :db.part/db)
     :db/ident              :order-field/service-field
     :db/valueType          :db.type/ref
     :db/cardinality        :db.cardinality/one
     :db/index              true
     :db/doc                "Reference to the service field that this order field is input for."
     :db.install/_attribute :db.part/db}
    {:db/id                 (d/tempid :db.part/db)
     :db/ident              :order-field.value/text
     :db/valueType          :db.type/string
     :db/cardinality        :db.cardinality/one
     :db/fulltext           true
     :db/doc                "A text value."
     :db.install/_attribute :db.part/db}
    {:db/id                 (d/tempid :db.part/db)
     :db/ident              :order-field.value/number
     :db/valueType          :db.type/float
     :db/cardinality        :db.cardinality/one
     :db/index              true
     :db/doc                "A float value."
     :db.install/_attribute :db.part/db}
    {:db/id                 (d/tempid :db.part/db)
     :db/ident              :order-field.value/date
     :db/valueType          :db.type/instant
     :db/cardinality        :db.cardinality/one
     :db/index              true
     :db/doc                "A date value."
     :db.install/_attribute :db.part/db}
    {:db/id                 (d/tempid :db.part/db)
     :db/ident              :order-field.value/option
     :db/valueType          :db.type/string
     :db/cardinality        :db.cardinality/one
     :db/index              true
     :db/doc                "A dropdown selection."
     :db.install/_attribute :db.part/db}]))


(def ^{:added "3.0.0"} add-subscription-reference
  (s/generate-schema
   [(s/schema
     order
     (s/fields
      [subscription-id :ref :indexed
       "Reference to a teller subscription."]))]))


(defn norms [part]
  {:schema.order/add-schema-04132017
   {:txes [schema]}

   :schema.order/order-improvements-06292017
   {:txes [order-improvements (add-order-statuses part)]}

   :schema.order/add-processing-status-09132017
   {:txes [(add-processing-status part)]}

   :schema.order/additions-10022017
   {:txes [(add-fulfilled-status part)
           additions-10022017]}

   :schema.order/additions-10222017
   {:txes [rename-desc-attr (add-failed-status part) add-summary-and-line-items]
    :requires [:schema.order/add-schema-04132017]}

   :schema.order/add-order-fields-03192018
   {:txes [add-order-fields]}

   :schema.order/add-subscriptions-reference-04102018
   {:txes [add-subscription-reference]}})
