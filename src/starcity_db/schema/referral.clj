(ns starcity-db.schema.referral
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]))

(def ^{:added "1.4.1"} schema
  (conj
   (s/generate-schema
    [(s/schema
      referral
      (s/fields
       [source :string :fulltext
        "The referral source, i.e. the way in which this referral came to us."]

       [account :ref :index
        "The account associated with this referral."]

       [from :ref :index
        "The place that this referral came from within our product(s) (enum)."]))])
   ;; NOTE: Can't use datomic-schema here because of the ident
   {:db/id                 #db/id[:db.part/db]
    :db/ident              :referral.tour/community
    :db/valueType          :db.type/ref
    :db/cardinality        :db.cardinality/one
    :db/index              true
    :db/doc                "The community that this tour referral was booked for."
    :db.install/_attribute :db.part/db}))

(defn ^{:added "1.4.1"} referral-from [part]
  [{:db/id    (d/tempid part)
    :db/ident :referral.from/apply}
   {:db/id    (d/tempid part)
    :db/ident :referral.from/tour}])

(defn norms [part]
  {:schema.referral/add-referral-schema-03232017
   {:txes [schema (referral-from part)]}})
