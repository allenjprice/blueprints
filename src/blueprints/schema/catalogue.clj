(ns blueprints.schema.catalogue
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]))

(def ^{:added "1.5.0"} schema
  (s/generate-schema
   [(s/schema
     catalogue
     (s/fields
      [name :string :fulltext
       "Human-readable name for this catalogue."]

      [code :keyword :indexed
       "Internal code for this catalogue."]

      [properties :ref :many :indexed
       "Properties that this catalogue pertains to. If unspecified, assumed to apply to all."]

      [items :ref :many :component :indexed
       "The service items that comprise this catalogue."]))

    (s/schema
     cat-item
     (s/fields
      [index :long :indexed
       "Placement of service within catalogue (ordering)."]

      [service :ref :indexed
       "The service."]

      [name :string :indexed
       "Override of service's name for display purposes."]

      [desc :string :indexed
       "Override of service's description for display purposes."]

      [fields :ref :many :component :indexed
       "Specifications for additional user input to collect."]))

    (s/schema
     cat-field
     (s/fields
      [label :string :indexed
       "Label for this field."]

      [type :ref :indexed "Type of field."]

      [key :keyword :indexed
       "Key for this field to identify its value by client."]

      [min :long "Minimum number."]
      [max :long "Maximum number."]
      [step :float "Number step."]))]))

(defn- field-types [part]
  [{:db/id    (d/tempid part)
    :db/ident :cat-field.type/desc}
   {:db/id    (d/tempid part)
    :db/ident :cat-field.type/quantity}])

(defn norms [part]
  {:schema.catalogue/add-schema-04182017
   {:txes [schema (field-types part)]}})
