(ns blueprints.models.promote
  (:require [blueprints.models.account :as account]
            [blueprints.models.approval :as approval]
            [blueprints.models.member-license :as member-license]
            [blueprints.models.property :as property]
            [blueprints.models.security-deposit :as deposit]
            [blueprints.models.unit :as unit]
            [clj-time.coerce :as c]
            [clj-time.core :as t]
            [clojure.spec.alpha :as s]
            [toolbelt.date :as date]
            [toolbelt.datomic :as td]))


(defn- security-deposit-due-date
  "Determine the due date of the security deposit by examining the move-in date."
  [account approval]
  (let [deposit (deposit/by-account account)
        move-in (approval/move-in approval)
        tz      (property/time-zone (approval/property approval))
        due     (-> (c/to-date-time (date/end-of-day move-in tz))
                    (t/plus (t/days 30))
                    c/to-date)]
    {:db/id       (:db/id deposit)
     :deposit/due due}))


(defn- approval->member-license
  [{:keys [approval/unit approval/license] :as approval}]
  (member-license/create
   license
   unit
   (approval/move-in approval)
   (unit/rate unit license)
   :member-license.status/active))


(defn promote
  "Promote `account` to membership status."
  [account]
  (assert (approval/by-account account)
          "`account` must be approved before it can be promoted!")
  (let [base           (account/change-role account :account.role/member)
        approval       (approval/by-account account)
        member-license (approval->member-license approval)]
    [(assoc base :account/license member-license)
     (security-deposit-due-date account approval)]))

(s/fdef promote
        :args (s/cat :account td/entity?)
        :ret vector?)
