(ns blueprints.models.account
  (:require [clojure.spec :as s]
            [datomic.api :as d]
            [toolbelt.predicates :as p]))

;; =============================================================================
;; Selectors
;; =============================================================================

(def email :account/email)
(def phone-number :account/phone-number)
(def first-name :account/first-name)
(def middle-name :account/middle-name)
(def last-name :account/last-name)
(def dob :account/dob)
(def activation-hash :account/activation-hash)
(def member-application :account/member-application)
(def role :account/role)

(def security-deposit
  "Retrieve the `security-deposit` for `account`."
  (comp first :security-deposit/_account))

(s/fdef security-deposit
        :args (s/cat :account p/entity?)
        :ret p/entity?)

(defn full-name
  "Full name of person identified by this account, or when no name exists, the
  `email`."
  [{:keys [:account/first-name :account/last-name :account/middle-name] :as a}]
  (cond
    (or (nil? first-name) (nil? last-name))
    (:account/email a)

    (not (empty? middle-name))
    (format "%s %s %s" first-name middle-name last-name)

    :otherwise
    (format "%s %s" first-name last-name)))

(defn stripe-customer
  "Retrieve the `stripe-customer` that belongs to this account. Produces the
  customer that is on the Stripe master account, NOT the managed one -- the
  customer on the managed account will be used *only* for autopay."
  [db account]
  (->> (d/q '[:find ?e .
              :in $ ?a
              :where
              [?e :stripe-customer/account ?a]
              [(missing? $ ?e :stripe-customer/managed)]]
            db (:db/id account))
       (d/entity db)))

(defn approval
  "Produces the `approval` entity for `account`."
  [account]
  (-> account :approval/_account first))

(s/fdef approval
        :args (s/cat :account p/entity?)
        :ret p/entity?)

(def slack-handle
  "Produces the slack handle for this account."
  :account/slack-handle)

(s/fdef slack-handle
        :args (s/cat :account p/entity?)
        :ret (s/or :nothing nil? :handle string?))

;; =============================================================================
;; Predicates
;; =============================================================================

(defn exists?
  [db email]
  (d/entity db [:account/email email]))

(def bank-linked?
  "Is there a bank account linked to this account?"
  (comp not empty? :stripe-customer/_account))

;; =============================================================================
;; Queries
;; =============================================================================

(defn by-email
  "Look up an account by email."
  [db email]
  (d/entity db [:account/email email]))

(defn by-customer-id
  "Look up an account by Stripe customer id."
  [db customer-id]
  (:stripe-customer/account
   (d/entity db [:stripe-customer/customer-id customer-id])))

;; =============================================================================
;; Transactions
;; =============================================================================

(defn collaborator
  "Create a new collaborator account. This is currently created for the express
  purpose of "
  [email]
  {:db/id         (d/tempid :db.part/starcity)
   :account/email email
   :account/role  :account.role/collaborator})

;; =============================================================================
;; Metrics
;; =============================================================================

(defn total-created
  "Produce the number of accounts created between `pstart` and `pend`."
  [db pstart pend]
  (or (d/q '[:find (count ?e) .
             :in $ ?pstart ?pend
             :where
             [?e :account/activation-hash _ ?tx] ; use for creation inst
             [?tx :db/txInstant ?created]
             [(.after ^java.util.Date ?created ?pstart)]
             [(.before ^java.util.Date ?created ?pend)]]
           db pstart pend)
      0))

(s/fdef total-created
        :args (s/cat :db p/db?
                     :period-start inst?
                     :period-end inst?)
        :ret integer?)
