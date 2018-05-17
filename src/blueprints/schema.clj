(ns blueprints.schema
  (:require [blueprints.schema.account :as account]
            [blueprints.schema.address :as address]
            [blueprints.schema.approval :as approval]
            [blueprints.schema.asana :as asana]
            [blueprints.schema.avatar :as avatar]
            [blueprints.schema.catalogue :as catalogue]
            [blueprints.schema.charge :as charge]
            [blueprints.schema.cmd :as cmd]
            [blueprints.schema.community-safety :as cs]
            [blueprints.schema.event :as event]
            [blueprints.schema.income-file :as if]
            [blueprints.schema.license :as license]
            [blueprints.schema.member-application :as ma]
            [blueprints.schema.member-license :as ml]
            [blueprints.schema.msg :as msg]
            [blueprints.schema.news :as news]
            [blueprints.schema.note :as note]
            [blueprints.schema.onboard :as onboard]
            [blueprints.schema.order :as order]
            [blueprints.schema.property :as property]
            [blueprints.schema.referral :as referral]
            [blueprints.schema.security-deposit :as sd]
            [blueprints.schema.service :as service]
            [blueprints.schema.session :as session]
            [blueprints.schema.source :as source]
            [blueprints.schema.stripe-event :as se]
            [blueprints.schema.suggestion :as suggestion]
            [blueprints.schema.sync :as sync]
            [blueprints.schema.tag :as tag]
            [blueprints.schema.transaction :as transaction]
            [io.rkn.conformity :as c]))

(defn partition-norms [part]
  {part {:txes [[{:db/id                 #db/id[:db.part/db]
                  :db/ident              part
                  :db.install/_partition :db.part/db}]]}})

(defn- assemble-norms [part]
  (let [gen-norms (->> [account/norms
                        address/norms
                        approval/norms
                        asana/norms
                        avatar/norms
                        catalogue/norms
                        charge/norms
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
                        property/norms
                        referral/norms
                        sd/norms
                        service/norms
                        session/norms
                        source/norms
                        se/norms
                        suggestion/norms
                        sync/norms
                        tag/norms
                        transaction/norms]
                       (apply juxt))]
    (apply merge (gen-norms part))))

(defn conform
  "Install the schema by conforming all norms."
  ([conn]
   (conform conn :db.part/user))
  ([conn part]
   (c/ensure-conforms conn (partition-norms part))
   (c/ensure-conforms conn (assemble-norms part))))
