(ns blueprints.schema
  (:require [blueprints.schema
             [account :as account]
             [address :as address]
             [approval :as approval]
             [avatar :as avatar]
             [catalogue :as catalogue]
             [charge :as charge]
             [check :as check]
             [community-safety :as cs]
             [cmd :as cmd]
             [event :as event]
             [income-file :as if]
             [license :as license]
             [member-application :as ma]
             [member-license :as ml]
             [msg :as msg]
             [news :as news]
             [note :as note]
             [onboard :as onboard]
             [order :as order]
             [payment :as payment]
             [property :as property]
             [referral :as referral]
             [security-deposit :as sd]
             [service :as service]
             [session :as session]
             [source :as source]
             [stripe :as stripe]
             [stripe-customer :as sc]
             [stripe-event :as se]
             [suggestion :as suggestion]
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
                        catalogue/norms
                        charge/norms
                        check/norms
                        cs/norms
                        cmd/norms
                        event/norms
                        if/norms
                        license/norms
                        ma/norms
                        ml/norms
                        msg/norms
                        news/norms
                        note/norms
                        onboard/norms
                        order/norms
                        payment/norms
                        property/norms
                        referral/norms
                        sd/norms
                        service/norms
                        session/norms
                        source/norms
                        stripe/norms
                        sc/norms
                        se/norms
                        suggestion/norms
                        tag/norms]
                       (apply juxt))]
    (apply merge (gen-norms part))))

(defn conform
  "Install the schema by conforming all norms."
  [conn part]
  (c/ensure-conforms conn (partition-norms part))
  (c/ensure-conforms conn (assemble-norms part)))
