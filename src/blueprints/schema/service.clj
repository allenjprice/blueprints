(ns blueprints.schema.service
  "The `service` entity represents a Starcity offering with monetary value that
  can be purchased."
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]))

(def ^{:added "1.5.0"} schema
  (s/generate-schema
   [(s/schema
     service
     (s/fields
      [code :string :fulltext
       "Internal indentifier for this service."]

      [name :string :fulltext
       "Override of service's name for display purposes."]

      [desc :string :fulltext
       "External human-friendy description of this service."]

      [desc-internal :string :fulltext
       "Human-friendly description of this service for internal record-keeping."]

      [variants :ref :many :component :indexed
       "Variants of this service."]

      [price :float :indexed
       "Price of a unit of this service. Can be omitted for quote-based services."]

      [rental :boolean :indexed
       "`true` if this service represents a rental."]

      [properties :ref :many :indexed
       "Properties that this service pertains to. If unspecified, assumed to apply to all."]

      [billed :ref :indexed
       "Specifies the method in which this service should be billed."]))

    (s/schema
     svc-variant
     (s/fields
      [name :string :indexed "Name of this variant."]
      [price :float :indexed "Price override of the base service."]))]))

(defn- billing-types [part]
  [{:db/id    (d/tempid part)
    :db/ident :service.billed/once}
   {:db/id    (d/tempid part)
    :db/ident :service.billed/monthly}])

(defn norms [part]
  {:schema.services/add-schema-04132017
   {:txes [schema (billing-types part)]}})
