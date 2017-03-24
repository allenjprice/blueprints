(ns starcity-db.schema
  (:require [starcity-db.schema
             [account :as account]
             [address :as address]
             [approval :as approval]
             [avatar :as avatar]
             [charge :as charge]
             [check :as check]
             [community-safety :as cs]
             [cmd :as cmd]
             [income-file :as if]
             [license :as license]
             [member-application :as ma]
             [member-license :as ml]
             [msg :as msg]
             [news :as news]
             [note :as note]
             [property :as property]
             [referral :as referral]
             [security-deposit :as sd]
             [session :as session]
             [stripe-customer :as sc]
             [stripe-event :as se]
             [tag :as tag]]))

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
                        cmd/norms
                        if/norms
                        license/norms
                        ma/norms
                        ml/norms
                        msg/norms
                        news/norms
                        note/norms
                        property/norms
                        referral/norms
                        sd/norms
                        session/norms
                        sc/norms
                        se/norms
                        tag/norms]
                       (apply juxt))]
    (apply merge (gen-norms part))))
