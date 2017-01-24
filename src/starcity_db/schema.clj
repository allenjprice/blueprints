(ns starcity-db.schema
  (:require [starcity-db.schema
             [account :as account]
             [address :as address]
             [approval :as approval]
             [avatar :as avatar]
             [charge :as charge]
             [check :as check]
             [community-safety :as cs]
             [income-file :as if]
             [license :as license]
             [member-application :as ma]
             [member-license :as ml]
             [news :as news]
             [property :as property]
             [security-deposit :as sd]
             [session :as session]
             [stripe-customer :as sc]
             [stripe-event :as se]]))

(defn partition-norms [part]
  {part {:txes [[{:db/id                 #db/id[:db.part/db]
                  :db/ident              part
                  :db.install/_partition :db.part/db}]]}})

(defn norms [part]
  (let [gen-norms (->> [account/norms
                        address/norms
                        approval/norms
                        avatar/norms
                        charge/norms
                        check/norms
                        cs/norms
                        if/norms
                        license/norms
                        ma/norms
                        ml/norms
                        news/norms
                        property/norms
                        sd/norms
                        session/norms
                        sc/norms
                        se/norms]
                       (apply juxt))]
    (apply merge (gen-norms part))))
