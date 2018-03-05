(ns blueprints.models.customer
  (:require [clojure.spec.alpha :as s]
            [datomic.api :as d]
            [toolbelt.core :as tb]
            [toolbelt.datomic :as td]))


;; =============================================================================
;; Selectors
;; =============================================================================


(def ^{:deprecated "1.17.0"} id
  "The id of the Stripe customer."
  :stripe-customer/customer-id)

(s/fdef id
        :args (s/cat :customer td/entity?)
        :ret string?)


(def ^{:deprecated "1.17.0"} account
  "The account that this customer belongs to."
  :stripe-customer/account)

(s/fdef account
        :args (s/cat :customer td/entity?)
        :ret td/entity?)


(def ^{:deprecated "1.17.0"} bank-token
  "The customer's bank account token, if any."
  :stripe-customer/bank-account-token)

(s/fdef bank-token
        :args (s/cat :customer td/entity?)
        :ret (s/? string?))


(def ^{:deprecated "1.17.0"} managing-property
  "The property that this Stripe customer belongs to, if any."
  :stripe-customer/managed)

(s/fdef managing-property
        :args (s/cat :customer td/entity?)
        :ret (s/? td/entity?))


(defn statement-name
  [customer]
  (:customer/statement-name customer))

(s/fdef statement-name
        :args (s/cat :customer td/entity?)
        :ret (s/nilable string?))


;; =============================================================================
;; Predicates
;; =============================================================================


(defn ^{:deprecated "1.17.0"} has-verified-bank-account?
  [customer]
  (some? (bank-token customer)))

(s/fdef has-verified-bank-account?
        :args (s/cat :customer td/entity?)
        :ret boolean?)


;; =============================================================================
;; Transactions
;; =============================================================================


(defn ^{:deprecated "1.17.0"} create
  "Create a new Stripe customer."
  [customer-id account & {:keys [bank-token managing-property]}]
  (tb/assoc-when
   {:db/id                       (d/tempid :db.part/starcity)
    :stripe-customer/customer-id customer-id
    :stripe-customer/account     (when-some [a account] (td/id a))}
   :stripe-customer/bank-account-token bank-token
   :stripe-customer/managed (when-some [p managing-property] (td/id p))))

(s/def ::bank-token string?)
(s/def ::managing-property td/entity?)
(s/fdef create
        :args (s/cat :customer-id string?
                     :account td/entity?
                     :opts (s/keys* :opt-un [::bank-token
                                             ::managing-property]))
        :ret (s/keys :req [:db/id
                           :stripe-customer/customer-id
                           :stripe-customer/account]
                     :opt [:stripe-customer/bank-account-token
                           :stripe-customer/managed]))


(defn ^{:deprecated "1.17.0"} add-bank-token
  "Add `bank-token` to the `customer` entity."
  [customer bank-token]
  {:db/id                              (td/id customer)
   :stripe-customer/bank-account-token bank-token})

(s/fdef add-bank-token
        :args (s/cat :customer td/entity? :bank-token string?)
        :ret (s/keys :req [:db/id :stripe-customer/bank-account-token]))


;; =============================================================================
;; Queries
;; =============================================================================


(defn ^{:deprecated "1.17.0"} by-account
  "Retrieve the `stripe-customer` that belongs to this account. Produces the
  customer that is on the Stripe master account, NOT the managed one -- the
  customer on the managed account will be used *only* for autopay."
  [db account]
  (->> (d/q '[:find ?e .
              :in $ ?a
              :where
              [?e :stripe-customer/account ?a]
              [(missing? $ ?e :stripe-customer/managed)]]
            db (td/id account))
       (d/entity db)))

(s/fdef by-account
        :args (s/cat :db td/db? :account td/entity?)
        :ret (s/or :entity td/entityd? :nothing nil?))


(defn ^{:deprecated "1.17.0"} by-customer-id
  "Look up a Stripe customer by `customer-id` (Stripe key)."
  [db customer-id]
  (d/entity db [:stripe-customer/customer-id customer-id]))

(s/fdef by-customer-id
        :args (s/cat :db td/db? :customer-id string?)
        :ret (s/or :entity td/entityd? :nothing nil?))


(defn ^{:deprecated "1.17.0"} autopay
  "Retrieve the customer that lives on the connected account for autopay
  payments--the inverse of `by-account`."
  [db account]
  (->> (d/q '[:find ?e .
              :in $ ?a
              :where
              [?e :stripe-customer/account ?a]
              [?e :stripe-customer/managed _]]
            db (td/id account))
       (d/entity db)))

(s/fdef autopay
        :args (s/cat :db td/db? :account td/entity?)
        :ret (s/or :entity td/entityd? :nothing nil?))


;; because we now also create customers on the connected account for order
;; subscriptions.
(def ^{:added "1.17.0"} connect
  autopay)
