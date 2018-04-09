(ns blueprints.schema.service
  "The `service` entity represents a Starcity offering with monetary value that
  can be purchased."
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]
            [toolbelt.datomic :as td]))

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


(def ^{:added "1.13.0"} add-cost
  (s/generate-schema
   [(s/schema
     service
     (s/fields
      [cost :float :indexed
       "The cost of this service."]))

    (s/schema
     svc-variant
     (s/fields
      [cost :float :indexed "Cost override of the base service."]))]))



(defn- ^{:added "2.3.0"} add-fields-and-catalogs [part]
  (concat
   (s/generate-schema
    [(s/schema
      service
      (s/fields
       [catalogs :keyword :many :indexed
        "The catalogs in which this service should appear"]

       [fields :ref :many :component :indexed
        "A service's fields."]

       [active :boolean :indexed
        "`true` if the service is made available."]

       [name-internal :string :indexed
        "The staff-facing name for a service offering."]

       [fees :ref :many :indexed
        "One-time setup fees (like installation for furniture rentals) that
        are incurred when ordering a service. These are, themselves, services."]))

     (s/schema
      service-field
      (s/fields
       [index :long :indexed
        "The position of this field within the list"]


       [type :ref :indexed
        "The type of service field."]

       [label :string :indexed
        "The label for the input fields in the UI"]


       [required :boolean
        "`true` if the user is required to enter a value into this field when placing an order"]

       [options :ref :many :component :indexed
        "Options to choose from if the service field is of type `dropdown`"]))

     (s/schema
      service-field-option
      (s/fields
       [value :string :indexed
        "The actual value of the dropdown. Stored as a string."]
       [label :string :indexed
        "The label presented to the user."]
       [index :long :indexed
        "The position in which this option should appear within the dropdown menu."]))])


   [{:db/id    (d/tempid part)
     :db/ident :service-field.type/time}
    {:db/id    (d/tempid part)
     :db/ident :service-field.type/date}
    {:db/id    (d/tempid part)
     :db/ident :service-field.type/text}
    {:db/id    (d/tempid part)
     :db/ident :service-field.type/number}
    {:db/id    (d/tempid part)
     :db/ident :service-field.type/dropdown}]


   [{:db/id          (d/tempid :db.part/db)
     :db/ident       :service-field.time/range-start
     :db/valueType   :db.type/instant
     :db/cardinality :db.cardinality/one
     :db/index       true
     :db/doc         "The starting range of a time field."}
    {:db/id          (d/tempid :db.part/db)
     :db/ident       :service-field.time/range-end
     :db/valueType   :db.type/instant
     :db/cardinality :db.cardinality/one
     :db/index       true
     :db/doc         "The ending range of a time field."}
    {:db/id          (d/tempid :db.part/db)
     :db/ident       :service-field.time/interval
     :db/valueType   :db.type/long
     :db/cardinality :db.cardinality/one
     :db/index       true

     :db/doc "The interval of a time field in minutes."}
    {:db/id     :service/code
     :db/unique :db.unique/identity}
    ]))


(defn- service-types [part]
  [{:db/id    (d/tempid part)
    :db/ident :service.type/service}
   {:db/id    (d/tempid part)
    :db/ident :service.type/fee}])


(def ^{:added "4.2.1"} add-types
  (s/generate-schema
   [(s/schema
     service
     (s/fields
      [type :ref :indexed
       "Indicates the type of service (service, fee, event ticket, etc.)"]))]))


(defn norms [part]
  {:schema.services/add-schema-04132017
   {:txes [schema (billing-types part)]}

   :schema.service/add-cost-10202017
   {:txes [add-cost]}

   :schema.service/add-fields-and-catalogs-03012018
   {:txes [(add-fields-and-catalogs part)]}

   :schema.service/add-types-04092018
   {:txes [(service-types part)
           add-types]}})
