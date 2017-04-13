(ns blueprints.schema.address
  (:require [datomic-schema.schema :as s]))

(def ^{:added "1.0.0"} schema
  (s/generate-schema
   [(s/schema
     address
     (s/fields
      [lines :string "Address lines, separated by newlines."]
      [state :string]
      [city :string]
      [postal-code :string]))]))

(def ^{:added "1.1.0"} add-international-support
  [{:db/id    :address/city
    :db/ident :address/locality
    :db/doc   "City/town"}
   {:db/id    :address/state
    :db/ident :address/region
    :db/doc   "State/province/region."}
   {:db/id                 #db/id[:db.part/db]
    :db/ident              :address/country
    :db/valueType          :db.type/string
    :db/cardinality        :db.cardinality/one
    :db/doc                "Country"
    :db.install/_attribute :db.part/db}])

(defn norms [part]
  {:starcity/add-address-schema
   {:txes [schema]}

   :schema/alter-address-schema-10-8-16
   {:txes     [add-international-support]
    :requires [:starcity/add-address-schema]}})
