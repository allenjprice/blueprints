(ns starcity-db.schema.referral
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]))

(def ^{:added "1.4.1"} schema
  (s/generate-schema
   [(s/schema
     referral
     (s/fields
      [source :string :fulltext
       "The referral source, i.e. the way in which this referral came to us."]

      [account :ref :idnex
       "The account associated with this referral."]

      [from :ref :index
       "The place that this referral came from within our product(s) (enum)."]

      [tour-for :ref :index
       "The community that this tour referral was booked for."]))]))

(defn ^{:added "1.4.1"} referral-from [part]
  [{:db/id    (d/tempid part)
    :db/ident :referral.from/apply}
   {:db/id    (d/tempid part)
    :db/ident :referral.from/tour}])

(defn norms [part]
  {:schema.referral/add-referral-schema-03232017
   {:txes [schema (referral-from part)]}})
