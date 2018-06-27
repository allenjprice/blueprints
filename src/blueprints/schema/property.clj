(ns blueprints.schema.property
  (:require [datomic-schema.schema :as s]))

(def ^{:added "1.0.0"} schema
  (s/generate-schema
   [(s/schema
     property
     (s/fields
      [name :string :fulltext]
      [description :string :fulltext]

      [cover-image-url :string]

      [internal-name :string :unique-identity :fulltext]
      [address :ref :component]

      [units :ref :many
       "The units that exist in this property."]
      [licenses :ref :many
       "The licenses that are available for this property."]
      [available-on :instant
       "The date that this property will come online."]

      [upcoming :string
       "The date that this property will come online."]))]))


(def ^{:added "1.0.0"} property-license-schema
  (s/generate-schema
   [(s/schema
     property-license
     (s/fields
      [license :ref
       "Reference to a license for a specific property."]
      [base-price :float
       "The base price for this license at this property."]))]))


(def ^{:added "1.0.0"} unit-schema
  (s/generate-schema
   [(s/schema
     unit
     (s/fields
      [name :string :fulltext
       "Name of the unit."]
      [description :string :fulltext
       "Description of the unit."]
      [price :float
       "Additional per-month price of this unit on top of monthly lease."]
      [available-on :instant
       "The date that this unit is available for lease."]
      [floor :long
       "The floor that this unit is on."]
      [dimensions :ref :component
       "The dimensions of this unit."]))

    (s/schema
     unit-dimension
     (s/fields
      [height :float
       "Height of unit in feet."]
      [width :float
       "Width of unit in feet."]
      [length :float
       "Length/depth of unit in feet."]))]))


(def ^{:added "< 1.1.3"} add-managed-account-id
  (s/generate-schema
   [(s/schema
     property
     (s/fields
      [managed-account-id :string
       "The id of the managed Stripe account associated with this property."]))]))


(def ^{:added "1.1.4"} property-improvements
  [{:db/id               :property/units
    :db/isComponent      true
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :property/licenses
    :db/isComponent      true
    :db/index            true
    :db.alter/_attribute :db.part/db}])


(def ^{:added "1.2.0"} unit-improvements
  [{:db/id               :unit/name
    :db/unique           :db.unique/identity
    :db.alter/_attribute :db.part/db}])


(def ^{:added "1.2.0"} add-ops-fee
  (s/generate-schema
   [(s/schema
     property
     (s/fields
      [ops-fee :float
       "The percentage fee that Starcity Ops takes from payments to this property."]))]))


(def ^{:added "1.3.0"} add-unit-licenses-and-number
  (s/generate-schema
   [(s/schema
     unit
     (s/fields
      [licenses :ref :many :component
       "Reference to the priced licenses for this unit."]
      [number :long :index
       "This unit's number."]))]))


(def ^{:added "1.3.0"} make-property-licenses-generic
  [{:db/id               :property-license/license
    :db/ident            :license-price/license
    :db/index            true
    :db/doc              "The license."
    :db.alter/_attribute :db.part/db}
   {:db/id               :property-license/base-price
    :db/ident            :license-price/price
    :db/index            true
    :db/doc              "The price."
    :db.alter/_attribute :db.part/db}])


(def ^{:added "1.4.1"} add-tours-attr
  (s/generate-schema
   [(s/schema
     property
     (s/fields
      [tours :boolean
       "Indicates whether or not tours are currently being accepted for this property."]))]))


(def ^{:added "1.10.0"} schema-cleanup
  [{:db/id  :property/cover-image-url
    :db/doc "DEPRECATED: No longer used."}
   {:db/id    :property/internal-name
    :db/ident :property/code
    :db/doc   "Internal code for looking up the property."}
   {:db/id    :property/available-on
    :db/index true}
   {:db/id    :property/managed-account-id
    :db/ident :property/rent-connect-id
    :db/index true
    :db/doc   "The id of the Stripe Connect account used for rent payments."}
   {:db/id    :property/ops-fee
    :db/index true}
   {:db/id    :property/tours
    :db/index true}])


(def ^{:added "1.10.0"} add-deposit-connect-id
  (s/generate-schema
   [(s/schema
     property
     (s/fields
      [deposit-connect-id :string :indexed
       "The id of the Stripe Connect account used for security deposits."]))]))


(def ^{:added "1.17.0"} ops-fee-extensions
  (concat
   [{:db/id               :property/ops-fee
     :db/ident            :property/ops-fee-rent
     :db/doc              "The percent fee to apply to rent payments."
     :db.alter/_attribute :db.part/db}]
   (s/generate-schema
    [(s/schema
      property
      (s/fields
       [ops-fee-orders :float :indexed
        "The percent fee to apply to premium service order payments."]))])))


(def ^{:added "2.6.3"} add-region
  (s/generate-schema
   [(s/schema
     property
     (s/fields
      [region :ref :indexed
       "The region in which this community is located."]))]))


(defn norms [part]
  {:starcity/add-property-schema
   {:txes [schema]}

   :starcity/add-property-license-schema
   {:txes [property-license-schema]}

   :starcity/add-unit-schema
   {:txes [unit-schema]}

   :schema/add-stripe-credentials-to-property-schema-9-8-16
   {:txes [add-managed-account-id]}

   :schema/improvements-11-20-16
   {:txes     [property-improvements]
    :requires [:starcity/add-property-schema]}

   :schema.property/add-ops-fee-12-14-16
   {:txes [add-ops-fee]}

   :schema.property.unit/unit-improvements-1-13-17
   {:txes     [unit-improvements]
    :requires [:starcity/add-unit-schema]}

   :schema.property.unit/add-unit-licenses-and-number
   {:txes     [add-unit-licenses-and-number]
    :requires [:starcity/add-unit-schema]}

   :schema.property/make-property-licenses-generic
   {:txes     [make-property-licenses-generic]
    :requires [:starcity/add-property-license-schema]}

   :schema.property/add-tours-attr-03232017
   {:txes [add-tours-attr]}

   :schema.property/improvements-07192017
   {:txes     [schema-cleanup add-deposit-connect-id]
    :requires [:starcity/add-property-schema
               :schema/add-stripe-credentials-to-property-schema-9-8-16
               :schema.property/add-tours-attr-03232017]}

   :schema.property/ops-fee-extensions-11272017
   {:txes     [ops-fee-extensions]
    :requires [:schema.property/add-ops-fee-12-14-16]}})
