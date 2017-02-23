(ns starcity-db.schema.charge
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]))

(def ^{:added "1.0.0"} schema
  (s/generate-schema
   [(s/schema
     charge
     (s/fields
      [stripe-id :string :unique-identity
       "The Stripe ID for this charge."]

      [account :ref
       "The account with which this charge is associated."]

      [purpose :string :fulltext
       "Description of the purpose of this charge."]))]))

(defn- statuses [part]
  [{:db/id    (d/tempid part)
    :db/ident :charge.status/pending}
   {:db/id    (d/tempid part)
    :db/ident :charge.status/succeeded}
   {:db/id    (d/tempid part)
    :db/ident :charge.status/failed}])

(defn- ^{:added "1.1.0"} add-charge-status [part]
  (->> (s/generate-schema
        [(s/schema
          charge
          (s/fields
           [status :ref "The status of this charge."]))])
       (concat (statuses part))))

(def ^{:added "1.3.0"} add-charge-amount
  (s/generate-schema
   [(s/schema
     charge
     (s/fields
      [amount :float :index
       "The amount in dollars that the charge is for."]))]))

(defn norms [part]
  {:starcity/add-charge-schema
   {:txes [schema]}

   :schema/add-charge-status-11-1-16
   {:txes [(add-charge-status part)]}

   :schema.charge/add-charge-amount-2-1-17
   {:txes [add-charge-amount]}})
