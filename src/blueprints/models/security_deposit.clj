(ns blueprints.models.security-deposit
  (:require [blueprints.models.charge :as charge]
            [blueprints.models.check :as check]
            [clojure.spec :as s]
            [datomic.api :as d]
            [toolbelt.core :as tb]
            [toolbelt.datomic :as td]
            [toolbelt.predicates :as p]))


;; =============================================================================
;; Spec
;; =============================================================================


(s/def :security-deposit/payment-method
  #{:security-deposit.payment-method/ach
    :security-deposit.payment-method/check})


;; =============================================================================
;; Selectors
;; =============================================================================


(defn amount-received
  "The amount that we've received towards this deposit so far."
  [deposit]
  (get deposit :security-deposit/amount-received 0))

(s/fdef amount-received
        :args (s/cat :deposit p/entity?)
        :ret integer?)

(def received amount-received)


(defn amount-required
  "The amount required to consider this security deposit paid."
  [deposit]
  (get deposit :security-deposit/amount-required 0))

(s/fdef amount-required
        :args (s/cat :deposit p/entity?)
        :ret integer?)


(def required amount-required)


(def due-by
  "The date that the security deposit is due on."
  :security-deposit/due-by)

(s/fdef due-by
        :args (s/cat :deposit p/entity?)
        :ret inst?)


(def account
  "The account that owns this deposit."
  :security-deposit/account)

(s/fdef account
        :args (s/cat :deposit p/entity?)
        :ret p/entity?)


(def checks
  "Check entities associated with this deposit."
  :security-deposit/checks)

(s/fdef checks
        :args (s/cat :deposit p/entity?)
        :ret (s/* p/entity?))


(def charges
  "Charge entities associated with this deposit."
  :security-deposit/charges)

(s/fdef charges
        :args (s/cat :deposit p/entity?)
        :ret (s/* p/entity?))


(def method
  "The payment method chosen during the onboarding flow."
  :security-deposit/payment-method)

(s/fdef method
        :args (s/cat :deposit p/entity?)
        :ret :security-deposit/payment-method)


(defn amount-remaining
  "The amount still remaining to be paid."
  [deposit]
  (- (amount-required deposit) (amount-received deposit)))

(s/fdef amount-remaining
        :args (s/cat :deposit p/entity?)
        :ret integer?)


(defn- amount-pending-checks [deposit]
  (->> (:security-deposit/checks deposit)
       (filter #(or (= (:check/status %) :check.status/received)
                    (= (:check/status %) :check.status/deposited)))
       (reduce #(+ %1 (:check/amount %2)) 0)))


(defn- amount-pending-charges [deposit]
  (letfn [(-cents [amt] (float (/ amt 100)))]
    (->> (:security-deposit/charges deposit)
         (filter #(= (:charge/status %) :charge.status/pending))
         (reduce #(+ %1 (:charge/amount %2 0)) 0))))


(defn amount-pending
  "Using attached checks and charges, determine how much is in a pending state.
  This means a) checks that have not cleared and b) charges that are in a
  pending state."
  [deposit]
  (apply + ((juxt amount-pending-checks amount-pending-charges) deposit)))


;; =============================================================================
;; Predicates
;; =============================================================================


(defn is-unpaid?
  "A deposit is considered /unpaid/ if we have received no payment towards it,
  whether it has cleared or not."
  [deposit]
  (and (= 0 (amount-received deposit))
       (= 0 (amount-pending deposit))))

(s/fdef is-unpaid?
        :args (s/cat :deposit p/entity?)
        :ret boolean?)


(def is-paid?
  "Has the deposit been paid in any capacity? Because we allow partial payments,
  this will return true if /any/ amount of payment has been made."
  (comp not is-unpaid?))


(defn paid-in-full?
  "Is the deposit completely paid?"
  [deposit]
  (and (is-paid? deposit)
       (>= (amount-received deposit)
           (amount-required deposit))))


(defn partially-paid?
  "Is this deposit partially paid and NOT fully paid?"
  [deposit]
  (and (not (paid-in-full? deposit))
       (is-paid? deposit)))


;; =============================================================================
;; Transactions
;; =============================================================================


(defn- sum-charges
  [deposit]
  (or (->> (:security-deposit/charges deposit)
           (filter (comp #{:charge.status/succeeded} :charge/status))
           (map #(or (:charge/amount %) 0))
           (apply +))
      0))


;; =====================================
;; Checks


(defn- include-check-amount?
  "Based on `check`'s status, should its amount be included?"
  [check]
  (#{check/cleared check/received check/deposited} (check/status check)))


(defn- has-check?
  "Is `check` already part of `deposit`'s checks?"
  [deposit check]
  (some #(= (td/id check) (td/id %)) (checks deposit)))


(defn- resolve-checks
  [deposit new-or-updated-check]
  (if (has-check? deposit new-or-updated-check)
    ;; Replace
    (map #(if (= (td/id %) (td/id new-or-updated-check))
            new-or-updated-check
            %)
         (checks deposit))
    ;; Add
    (conj (checks deposit) new-or-updated-check)))


(defn- sum-checks
  "The sum of all amounts in existing checks on `security-deposit` including the
  effect of `new-or-updated-check`."
  [deposit new-or-updated-check]
  (->> (resolve-checks deposit new-or-updated-check)
       (reduce
        (fn [acc check]
          (if (include-check-amount? check)
            (+ acc (check/amount check))
            acc))
        0)))


(defn- new-amount-received
  [deposit check]
  (int (+ (sum-checks deposit check)
          (sum-charges deposit))))


(defn update-with-check
  "Update a security deposit's `amount-received` with an updated check."
  [deposit check updated-check]
  (let [;; To calculate the new amount, we need at least the check's id, amount and status
        params (merge (select-keys check [:db/id :check/amount :check/status])
                      updated-check)]
    {:db/id                            (td/id deposit)
     :security-deposit/amount-received (new-amount-received deposit params)}))

(s/fdef update-check
        :args (s/cat :security-deposit p/entity?
                     :check p/entity?
                     :updated-check check/updated?)
        :ret (s/keys :req [:db/id :security-deposit/amount-received]))


(defn add-check
  "Add a new `check` to the `security-deposit` entity, taking into consideration
  the check's contribution to the total amount received."
  [deposit check]
  (let [amount-received (new-amount-received deposit check)]
    {:db/id                            (td/id deposit)
     :security-deposit/payment-method  :security-deposit.payment-method/check
     :security-deposit/checks          check
     :security-deposit/amount-received amount-received}))

(s/fdef add-check
        :args (s/cat :security-deposit p/entity?
                     :check check/check?)
        :ret (s/keys :req [:db/id
                           :security-deposit/checks
                           :security-deposit/amount-received]))


(defn update-check
  [security-deposit check updated-check]
  (let [;; To calculate the new amount, we need at least the check's id, amount and status
        params (merge (select-keys check [:db/id :check/amount :check/status])
                      updated-check)]
    {:db/id                            (td/id security-deposit)
     :security-deposit/amount-received (new-amount-received security-deposit params)}))

(s/fdef update-check
        :args (s/cat :security-deposit p/entity?
                     :check p/entity?
                     :updated-check check/updated?)
        :ret (s/keys :req [:db/id :security-deposit/amount-received]))



(defn add-charge
  "Add a charge to the deposit, updating the `amount-received` when the charge
  has success status.."
  [deposit charge]
  (let [new-amount (when (charge/succeeded? charge)
                     (+ (amount-received deposit)
                        (int (charge/amount charge))))]
    (tb/assoc-when
     {:db/id                    (td/id deposit)
      :security-deposit/charges (td/id charge)}
     :security-deposit/amount-received new-amount)))

(s/fdef add-charge
        :args (s/cat :deposit p/entity? :charge p/entity?)
        :ret (s/keys :req [:db/id
                           :security-deposit/charges
                           :security-deposit/amount-received]))


(defn create
  "Produce transaction data to create a security deposit entity for `account`.

  Only requires an `amount` (and `account` of course), since other details are
  filled in by `account` during the onboarding flow."
  [account amount]
  {:db/id                            (d/tempid :db.part/starcity)
   :security-deposit/account         (td/id account)
   :security-deposit/amount-received 0
   :security-deposit/amount-required amount})

(s/fdef create
        :args (s/cat :account p/entity?
                     :amount integer?)
        :ret (s/keys :req [:db/id
                           :security-deposit/account
                           :security-deposit/amount-received
                           :security-deposit/amount-required]))


;; =============================================================================
;; Lookups
;; =============================================================================


(def by-account
  "Retrieve `security-deposit` given the owning `account`."
  (comp first :security-deposit/_account))

(s/fdef by-account
        :args (s/cat :account p/entity?)
        :ret p/entity?)


(def by-charge
  "Produce the security deposit given `charge`."
  :security-deposit/_charges)

(s/fdef by-charge
        :args (s/cat :charge p/entity?)
        :ret p/entity?)
