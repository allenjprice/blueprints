(ns blueprints.migrations
  (:require [datomic.api :as d]
            [io.rkn.conformity :as c]
            [blueprints.migrations
             [dates-06122017 :as dates-061222017]
             [member-application :as ma]
             [member-license :as ml]
             [property :as property]
             [security-deposit :as deposit]]))

(defn- ^{:added "1.0.x"} add-countries-to-addresses
  "Prior to adding international support, there was no `:address/country`
  attribute; however, due to lack of international support, all accounts must
  have been in the US."
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
   (property/norms conn)
   (ml/norms conn)
   (dates-061222017/norms conn)
   (deposit/norms conn)))

(defn conform [conn part]
  (c/ensure-conforms conn (norms conn part)))
