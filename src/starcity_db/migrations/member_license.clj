(ns starcity-db.migrations.member-license
  (:require [datomic.api :as d]))

(defn- ^{:added "1.3.0"} use-member-license-status
  "Prior to 1.3.0, member licenses used a boolean flag to determine whether or
  not the license was active. Now we introduce a status attribute."
  [conn]
  (->> (d/q '[:find ?e ?active
              :where
              [?e :member-license/active ?active]]
            (d/db conn))
       (mapcat (fn [[e active]]
                 (let [s (if active
                           :member-license.status/active
                           :member-license.status/inactive)]
                   [[:db/add e :member-license/status s]
                    [:db/retract e :member-license/active active]])))
       (d/transact conn)
       deref))

(defn norms [conn]
  {:migration.member-license/use-member-license-status-02162017
   {:txes [(use-member-license-status conn)]}})
