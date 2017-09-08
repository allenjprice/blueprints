(ns blueprints.models.security-deposit
  (:refer-clojure :exclude [type])
  (:require [blueprints.models.charge :as charge]
            [blueprints.models.check :as check]
            [blueprints.models.payment :as payment]
            [clojure.spec :as s]
            [datomic.api :as d]
            [toolbelt.core :as tb]
            [toolbelt.datomic :as td]
            [toolbelt.predicates :as p]
            [blueprints.schema.stripe-customer :as sc]))


;; =============================================================================
;; Spec
;; =============================================================================


(s/def :deposit/method
  #{:deposit.method/ach
    :deposit.method/check})


(s/def :deposit/type
  #{:deposit.type/full
    :deposit.type/partial})


;; =============================================================================
;; Selectors
;; =============================================================================


(defn ^{:deprecated "1.10.0"} amount-received
  "The amount that we've received towards this deposit so far."
  [deposit]
  (get deposit :security-deposit/amount-received 0))

(s/fdef amount-received
        :args (s/cat :deposit p/entity?)
        :ret integer?)

(def ^{:deprecated "1.10.0"} received amount-received)


(defn ^{:deprecated "1.10.0"} amount-required
  "The amount required to consider this security deposit paid."
  [deposit]
  (get deposit :security-deposit/amount-required 0))

(s/fdef amount-required
        :args (s/cat :deposit p/entity?)
        :ret integer?)


(def ^{:deprecated "1.10.0"} required amount-required)


(defn ^{:added "1.10.0"} amount
  "The amount to be paid."
  [deposit]
  (:deposit/amount deposit))

(s/fdef amount
        :args (s/cat :deposit p/entity?)
        :ret float?)


(def due-by
  "The date that the security deposit is due on."
  :deposit/due)

(s/fdef due-by
        :args (s/cat :deposit p/entity?)
        :ret inst?)


(def due due-by)


(def account
  "The account that owns this deposit."
  :deposit/account)

(s/fdef account
        :args (s/cat :deposit p/entity?)
        :ret p/entity?)


(def ^{:deprecated "1.10.0"} checks
  "Check entities associated with this deposit."
  :security-deposit/checks)

(s/fdef checks
        :args (s/cat :deposit p/entity?)
        :ret (s/* p/entity?))


(def ^{:deprecated "1.10.0"} charges
  "Charge entities associated with this deposit."
  :security-deposit/charges)

(s/fdef charges
        :args (s/cat :deposit p/entity?)
        :ret (s/* p/entity?))


(defn ^{:added "1.10.0"} payments
  [deposit]
  (:deposit/payments deposit))

(s/fdef payments
        :args (s/cat :deposit p/entity?)
        :ret (s/* p/entity?))


(def method
  "The payment method chosen during the onboarding flow."
  :deposit/method)

(s/fdef method
        :args (s/cat :deposit p/entity?)
        :ret :deposit/method)


(defn type
  "The deposit type chosen during the onboarding flow."
  [deposit]
  (:deposit/type deposit))

(s/fdef type
        :args (s/cat :deposit p/entity?)
        :ret :deposit/type)


(defn amount-remaining
  "The amount still remaining to be paid."
  [deposit]
  (let [paid (reduce
              #(+ %1 (if (or (payment/paid? %2) (payment/pending? %2))
                       (payment/amount %2)
                       0))
              0
              (payments deposit))]
    (- (amount deposit) paid)))

(s/fdef amount-remaining
        :args (s/cat :deposit p/entity?)
        :ret integer?)


(defn amount-paid
  "The amount that has been paid."
  [deposit]
  (reduce
   #(+ %1 (if (payment/paid? %2) (payment/amount %2) 0))
   0
   (payments deposit)))

(s/fdef amount-paid
        :args (s/cat :deposit p/entity?)
        :ret integer?)


(defn amount-pending
  "The amount that is still pending, either in the form of charges or checks."
  [deposit]
  (reduce
   #(+ %1 (if (payment/pending? %2) (payment/amount %2) 0))
   0
   (payments deposit)))

(s/fdef amount-pending
        :args (s/cat :deposit p/entity?)
        :ret integer?)


(defn refund-status
  "The status of the refund. `nil` when not refunded, initiated, successful or
  failed."
  [deposit]
  (:deposit/refund-status deposit))

(s/fdef refund-status
        :args (s/cat :deposit p/entity?)
        :ret (s/or :nothing nil?
                   :status #{:deposit.refund-status/initiated
                             :deposit.refund-status/successful
                             :deposit.refund-status/failed}))


;; =============================================================================
;; Predicates
;; =============================================================================


(defn is-unpaid?
  "A deposit is considered /unpaid/ if we have received no payment towards
  it (pending payments excepted)."
  [deposit]
  (= (amount-remaining deposit) (amount deposit)))

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
  (<= (amount-remaining deposit) 0))

(s/fdef paid-in-full?
        :args (s/cat :deposit p/entity?)
        :ret boolean?)


(defn partially-paid?
  "Is this deposit partially paid and NOT fully paid?"
  [deposit]
  (and (not (paid-in-full? deposit))
       (is-paid? deposit)))

(s/fdef partially-paid?
        :args (s/cat :deposit p/entity?)
        :ret boolean?)


(defn is-refundable?
  "Can this security deposit be refunded via Stripe?"
  [deposit]
  (and (nil? (refund-status deposit))
       (seq (payments deposit))
       (let [charge-total (->> (payments deposit)
                               (filter #(and (payment/charge? %1) (payment/paid? %1)))
                               (reduce #(+ %1 (payment/amount %2)) 0))]
         (= (amount deposit) charge-total))))

(s/fdef is-refundable?
        :args (s/cat :deposit p/entity?)
        :ret boolean?)


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


(defn ^{:deprecated "1.10.0"} update-with-check
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


(defn ^{:deprecated "1.10.0"} add-check
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


(defn ^{:deprecated "1.10.0"} update-check
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



(defn ^{:deprecated "1.10.0"} add-charge
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


(defn ^{:added "1.10.0"} add-payment
  "Add a payment to this deposit."
  [deposit payment]
  {:db/id            (td/id deposit)
   :deposit/payments (td/id payment)})

(s/fdef add-payment
        :args (s/cat :deposit p/entity? :payment p/entity?)
        :ret (s/keys :req [:db/id :deposit/payments]))


(defn create
  "Produce transaction data to create a security deposit entity for `account`.

  Only requires an `amount` (and `account` of course), since other details are
  filled in by `account` during the onboarding flow."
  [account amount]
  {:db/id           (d/tempid :db.part/starcity)
   :deposit/account (td/id account)
   :deposit/amount  (float amount)})

(s/fdef create
        :args (s/cat :account p/entity?
                     :amount number?)
        :ret (s/keys :req [:db/id :deposit/account :deposit/amount]))


;; =============================================================================
;; Lookups
;; =============================================================================


(def by-account
  "Retrieve `security-deposit` given the owning `account`."
  (comp first :deposit/_account))

(s/fdef by-account
        :args (s/cat :account p/entityd?)
        :ret p/entityd?)


(def ^{:deprecated "1.10.0"} by-charge
  "Produce the security deposit given `charge`."
  :security-deposit/_charges)

(s/fdef by-charge
        :args (s/cat :charge p/entity?)
        :ret p/entity?)


(defn by-payment
  "Produce the security deposit given `payment`."
  [payment]
  (:deposit/_payments payment))

(s/fdef by-payment
        :args (s/cat :payment p/entityd?)
        :ret (s/or :entity p/entityd? :nothing nil?))


(defn by-charge-id
  "Look up a security deposit given a Stripe charge id."
  [db charge-id]
  (->> (d/q '[:find ?e .
              :in $ ?c
              :where
              [?e :deposit/payments ?p]
              [?p :stripe/charge-id ?c]]
            db charge-id)
       (d/entity db)))

(s/fdef by-charge-id
        :args (s/cat :db p/db? :charge-id string?)
        :ret (s/or :entity p/entity? :nothing nil?))
