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

(defn norms [part]
  {:schema.order/add-schema-04132017
   {:txes [schema]}})
