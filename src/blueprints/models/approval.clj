(ns blueprints.models.approval
  (:require [blueprints.models.application :as application]
            [blueprints.models.onboard :as onboard]
            [blueprints.models.property :as property]
            [blueprints.models.security-deposit :as deposit]
            [blueprints.models.unit :as unit]
            [clojure.spec :as s]
            [datomic.api :as d]
            [toolbelt.date :as date]
            [toolbelt.predicates :as p]))

;; =============================================================================
;; Selectors
;; =============================================================================


(def approver
  "The admin `account` that did the approving."
  :approval/approver)

(s/fdef approver
        :args (s/cat :approval p/entity?)
        :ret p/entity?)


(def move-in
  "The move-in date."
  :approval/move-in)

(s/fdef move-in
        :args (s/cat :approval p/entity?)
        :ret inst?)


(def unit
  "The `unit` that `account` is approved to live in."
  :approval/unit)

(s/fdef unit
        :args (s/cat :approval p/entity?)
        :ret p/entity?)


(def license
  "The `license` (term) that `account` was approved for."
  :approval/license)

(s/fdef license
        :args (s/cat :approval p/entity?)
        :ret p/entity?)


(def property
  "The property that approval is for."
  (comp unit/property unit))

(s/fdef property
        :args (s/cat :approval p/entity?)
        :ret p/entity?)


;; =============================================================================
;; Transactions
;; =============================================================================


(defn create
  "Produce transaction data required to create an approval entity."
  [approver approvee unit license move-in]
  (let [tz (-> unit unit/property property/time-zone)]
    {:db/id             (d/tempid :db.part/starcity)
     :approval/account  (:db/id approvee)
     :approval/approver (:db/id approver)
     :approval/unit     (:db/id unit)
     :approval/license  (:db/id license)
     :approval/move-in  (date/beginning-of-day move-in tz) ; ensure beginning of day
     :approval/status   :approval.status/pending}))

(s/fdef create
        :args (s/cat :approver p/entity?
                     :approvee p/entity?
                     :unit p/entity?
                     :license p/entity?
                     :move-in inst?)
        :ret (s/keys :req [:db/id
                           :approval/account
                           :approval/approver
                           :approval/unit
                           :approval/license
                           :approval/move-in
                           :approval/status]))


(defn approve
  "Approve `approvee` by creating an `approval` entity and flipping the
  necessary bits elswhere in the database.

  More specifically, this means:
  - Change `account`'s role to onboarding
  - Create a security deposit stub
  - Mark the application as approved"
  [approver approvee unit license move-in]
  (let [tz      (-> unit unit/property property/time-zone)
        move-in (date/beginning-of-day move-in tz)] ; for msg
    [(create approver approvee unit license move-in)
     ;; Change role
     {:db/id (:db/id approvee) :account/role :account.role/onboarding}
     (deposit/create approvee (unit/rate unit license))
     (onboard/create approvee)
     (application/change-status (:account/application approvee)
                                :application.status/approved)]))

(s/fdef approve
        :args (s/cat :approver p/entity?
                     :approvee p/entity?
                     :unit p/entity?
                     :license p/entity?
                     :move-in inst?)
        :ret (s/and vector? (s/+ map?)))


;; =============================================================================
;; Queries
;; =============================================================================


(def by-account
  "Look up the `approval` entity by `account`."
  (comp first :approval/_account))

(s/fdef by-account
        :args (s/cat :account p/entity?)
        :ret p/entity?)
