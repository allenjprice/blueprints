(ns blueprints.schema.check
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]))

(defn- statuses [part]
  [{:db/id    (d/tempid part)
    :db/ident :check.status/deposited}
   {:db/id    (d/tempid part)
    :db/ident :check.status/cleared}
   {:db/id    (d/tempid part)
    :db/ident :check.status/bounced}
   {:db/id    (d/tempid part)
    :db/ident :check.status/cancelled}])


(defn- ^{:added "1.1.1"} schema [part]
  (->> (s/generate-schema
        [(s/schema
          check
          (s/fields
           [name :string
            "Name of person who wrote check."]
           [bank :string
            "Name of the bank that this check is associated with."]
           [amount :float
            "Amount of money that has been received for this check."]
           [number :long
            "The check number."]
           [date :instant
            "The date on the check."]
           [received-on :instant
            "Date that we received the check."]
           [status :ref
            "Status of the check wrt operations."]))])
       (concat (statuses part))))


(defn- ^{:added "1.2.0"} add-received-status [part]
  [{:db/id    (d/tempid part)
    :db/ident :check.status/received}])


(def ^{:added "1.10.0"} fix-canceled-spelling
  [{:db/id    :check.status/cancelled
    :db/ident :check.status/canceled}])


(defn norms [part]
  {:schema/add-check-schema-11-4-16
   {:txes [(schema part)]}

   :schema.check/add-received-status
   {:txes [(add-received-status part)]}

   :schema.check/fix-canceled-spelling-07202017
   {:txes [fix-canceled-spelling]
    :requires [:schema/add-check-schema-11-4-16]}})
