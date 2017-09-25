(ns blueprints.models.promote
  (:require [blueprints.models.account :as account]
            [blueprints.models.approval :as approval]
            [blueprints.models.member-license :as member-license]
            [blueprints.models.property :as property]
            [blueprints.models.payment :as payment]
            [blueprints.models.security-deposit :as deposit]
            [blueprints.models.unit :as unit]
            [clj-time.coerce :as c]
            [clj-time.core :as t]
            [clojure.spec :as s]
            [toolbelt.core :as tb]
            [toolbelt.date :as date]
            [toolbelt.predicates :as p]))


(defn- prorated-amount [commencement rate]
  (let [commencement   (c/to-date-time commencement)
        days-in-month  (t/day (t/last-day-of-the-month commencement))
        ;; We inc the days-remaining so that the move-in day is included in the calculation
        days-remaining (inc (- days-in-month (t/day commencement)))]
    (tb/round (* (/ rate days-in-month) days-remaining) 2)))


(defn- prorated-payment [property account start rate]
  (let [tz    (property/time-zone property)
        start (date/beginning-of-day start tz)]
    (payment/create (prorated-amount start rate) account
                    :for :payment.for/rent
                    :pstart start
                    :pend (date/end-of-month start tz)
                    :status :payment.status/due
                    :due start)))


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
        member-license (approval->member-license approval)
        payment        (prorated-payment (approval/property approval)
                                         account
                                         (approval/move-in approval)
                                         (member-license/rate member-license))]
    [(->> (tb/assoc-when
           member-license
           :member-license/rent-payments payment)
          (assoc base :account/license))
     (security-deposit-due-date account approval)]))

(s/fdef promote
        :args (s/cat :account p/entity?)
        :ret vector?)
