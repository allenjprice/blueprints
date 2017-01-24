(ns starcity-db.migrations
  (:require [datomic.api :as d]
            [io.rkn.conformity :as c]
            [starcity-db.migrations
             [member-application :as ma]
             [property :as property]]))

(defn- add-countries-to-addresses
  "Prior to adding international support, there was no `:address/country`
  attribute; however, due to lack of international support, all accounts must
  have been in the US."
  {:added "1.0.x"}
  [conn]
  (->> (d/q '[:find [?a ...]
              :where
              [?a :address/locality _]
              [(missing? $ ?a :address/country)]]
            (d/db conn))
       (mapv (fn [e] [:db/add e :address/country "US"]))))

(defn norms [conn part]
  (merge
   {:seed/add-countries-to-addresses-10-8-16
    {:txes [(add-countries-to-addresses conn)]}}
   (ma/norms conn)
   (property/norms conn)))
