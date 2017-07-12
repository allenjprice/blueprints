(ns blueprints.models.payment
  (:require [datomic.api :as d]
            [toolbelt
             [datomic :as td]
             [predicates :as p]]
            [clojure.spec :as s]
            [toolbelt.core :as tb]))


;; =============================================================================
;; Helpers
;; =============================================================================


(declare charge? invoice? method)


(defn- assert-stripe [payment]
  (assert (or (charge? payment) (invoice? payment))
          (format "Invalid state! Payment should have Stripe method; instead has %s"
                  (method payment))))


(defn- assert-invoice [payment]
  (assert (invoice? payment)
          (format "Invalid state! Payment should have Stripe invoice method; instead has %s"
                  (method payment))))


;; =============================================================================
;; Specs
;; =============================================================================


(s/def ::method
  #{:payment.method/stripe-charge
    :payment.method/stripe-invoice
    :payment.method/check})


(s/def ::status
  #{:payment.status/due
    :payment.status/canceled
    :payment.status/paid
    :payment.status/pending
    :payment.status/failed})


(s/def ::for
  #{:payment.for/rent
    :payment.for/deposit
    :payment.for/order})


;; =============================================================================
;; Selectors
;; =============================================================================


(def id
  "The payment's unique id."
  :payment/id)

(s/fdef id
        :args (s/cat :payment p/entity?)
        :ret uuid?)


(def method
  "The method of payment."
  :payment/method)

(s/fdef method
        :args (s/cat :payment p/entity?)
        :ret (s/or :method ::method :nothing nil?))


(def amount
  "The amount to be paid."
  :payment/amount)

(s/fdef amount
        :args (s/cat :payment p/entity?)
        :ret float?)


(def status
  "The payment status."
  :payment/status)

(s/fdef status
        :args (s/cat :payment p/entity?)
        :ret ::status)


(def due
  "The due date for this payment."
  :payment/due)

(s/fdef due
        :args (s/cat :payment p/entity?)
        :ret (s/or :inst inst? :nothing nil?))


(def account
  "The account that this payment pertains to."
  :payment/account)

(s/fdef account
        :args (s/cat :payment p/entity?)
        :ret (s/or :entity p/entity? :nothing nil?))


(def payment-for
  "What this payment is for."
  :payment/for)

(s/fdef payment-for
        :args (s/cat :payment p/entity?)
        :ret (s/or :for ::for :nothing nil?))


(defn charge-id
  "The id of the Stripe charge."
  [payment]
  (assert-stripe payment)
  (:stripe/charge-id payment))

(s/fdef charge-id
        :args (s/cat :payment p/entity?)
        :ret (s/or :string string? :nothing nil?))


(defn invoice-id
  "The id of the Stripe invoice."
  [payment]
  (assert-invoice payment)
  (:stripe/invoice-id payment))

(s/fdef invoice-id
        :args (s/cat :payment p/entity?)
        :ret (s/or :string string? :nothing nil?))


;; =============================================================================
;; Predicates
;; =============================================================================


(defn- of-method? [m payment]
  (= m (method payment)))

(s/fdef of-method?
        :args (s/cat :method ::method :payment p/entity?)
        :ret boolean?)


(defn charge?
  "Is this payment paid via a Stripe charge?"
  [payment]
  (of-method? :payment.method/stripe-charge payment))


(defn invoice?
  "Is this payment paid via a Stripe invoice?"
  [payment]
  (of-method? :payment.method/stripe-invoice payment))


(defn check?
  "Is this payment paid via a check?"
  [payment]
  (of-method? :payment.method/check payment))


(defn paid?
  "Has this payment been paid?"
  [payment]
  (= (status payment) :payment.status/paid))

(s/fdef paid?
        :args (s/cat :payment p/entity?)
        :ret boolean?)


(defn failed?
  "Has this payment failed to be charged?"
  [payment]
  (= (status payment) :payment.status/failed))

(s/fdef failed?
        :args (s/cat :payment p/entity?)
        :ret boolean?)


;; =============================================================================
;; Transactions
;; =============================================================================


(s/def ::account p/entity?)
(s/def ::uuid uuid?)
(s/def ::due inst?)


(defn create
  "Create a new payment."
  [amount & {:keys [uuid account due for status]
             :or   {uuid   (d/squuid)
                    status :payment.status/pending}}]
  (let [aid (when-some [a account] (td/id account))]
    (tb/assoc-when
     {:db/id          (d/tempid :db.part/starcity)
      :payment/id     uuid
      :payment/amount amount
      :payment/status status}
     :payment/account aid
     :payment/due due
     :payment/for for)))

(s/fdef create
        :args (s/cat :amount float?
                     :opts (s/keys* :opt-un [::uuid
                                             ::account
                                             ::due
                                             ::for
                                             ::status
                                             ::charge-id]))
        :ret (s/keys :req [:db/id
                           :payment/id
                           :payment/status]
                     :opt [:payment/account
                           :payment/due
                           :payment/for]))


(defn add-invoice
  "Add an invoice to this payment."
  [payment invoice-id]
  {:db/id             (td/id payment)
   :payment/method    :payment.method/stripe-invoice
   :stripe/invoice-id invoice-id})

(s/fdef add-invoice
        :args (s/cat :payment p/entity? :invoice-id string?)
        :ret map?)


(defn add-charge
  "Add a charge id to this payment."
  [payment charge-id]
  (let [m (when-not (invoice? payment) :payment.method/stripe-charge)]
    (tb/assoc-when
     {:db/id            (td/id payment)
      :stripe/charge-id charge-id}
     :payment/method m)))

(s/fdef add-charge
        :args (s/cat :payment p/entity? :charge-id string?)
        :ret map?)


(defn is-paid
  "The payment is now paid."
  [payment]
  {:db/id          (td/id payment)
   :payment/status :payment.status/paid})

(s/fdef is-paid
        :args (s/cat :payment p/entity?)
        :ret map?)


(defn is-failed
  "The payment failed to go through."
  [payment]
  {:db/id          (td/id payment)
   :payment/status :payment.status/failed})

(s/fdef is-failed
        :args (s/cat :payment p/entity?)
        :ret map?)


;; =============================================================================
;; Queries
;; =============================================================================


(defn by-id
  "Look up a payment by its `uuid`."
  [db uuid]
  (d/entity db [:payment/id uuid]))

(s/fdef by-id
        :args (s/cat :db p/db? :uuid uuid?)
        :ret p/entity?)


(defn by-charge-id
  "Look up a payment by its Stripe charge id. Payment must have method
  `:payment.type/stripe-charge` or `:payment.type/stripe-invoice`."
  [db charge-id]
  (let [py (d/entity db [:stripe/charge-id charge-id])]
    (assert-stripe py)
    py))

(s/fdef by-charge-id
        :args (s/cat :db p/db? :charge-id string?)
        :ret p/entity?)


(defn by-invoice-id
  "Look up a payment by its Stripe invoice id. Payment must have method
  `:payment.type/stripe-invoice`."
  [db invoice-id]
  (let [py (d/entity db [:stripe/invoice-id invoice-id])]
    (assert-invoice py)
    py))

(s/fdef by-charge-id
        :args (s/cat :db p/db? :invoice-id string?)
        :ret p/entity?)
