(ns blueprints.schema
  (:require [blueprints.schema
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
             [onboard :as onboard]
             [property :as property]
             [referral :as referral]
             [security-deposit :as sd]
             [services :as services]
             [session :as session]
             [stripe :as stripe]
             [stripe-customer :as sc]
             [stripe-event :as se]
             [tag :as tag]]
            [io.rkn.conformity :as c]))

(defn partition-norms [part]
  {part {:txes [[{:db/id                 #db/id[:db.part/db]
                  :db/ident              part
                  :db.install/_partition :db.part/db}]]}})

(defn- assemble-norms [part]
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
                        onboard/norms
                        property/norms
                        referral/norms
                        sd/norms
                        services/norms
                        session/norms
                        stripe/norms
                        sc/norms
                        se/norms
                        tag/norms]
                       (apply juxt))]
    (apply merge (gen-norms part))))

(defn conform
  "Install the schema by conforming all norms."
  [conn part]
  (c/ensure-conforms conn (partition-norms part))
  (c/ensure-conforms conn (assemble-norms part)))
