(ns blueprints.models.scustomer
  (:require [clojure.spec :as s]
            [toolbelt.predicates :as p]
            [datomic.api :as d]
            [toolbelt.datomic :as td]))

;; NOTE: The name `scustomer` is to be used only while migration from the old
;; model to the new takes place. This file should be be renamed `customer` after
;; all uses of the existing one have been removed.


;; =============================================================================
;; Selectors
;; =============================================================================


(defn platform-id
  "The id used by Stripe to represent this `customer`."
  [customer]
  (:customer/platform-id customer))

(s/fdef platform-id
        :args (s/cat :customer p/entity?)
        :ret string?)


(defn account
  "The account with which this `customer` is associated."
  [customer]
  (:customer/account customer))

(s/fdef account
        :args (s/cat :customer p/entity?)
        :ret p/entityd?)


(defn connected-customers
  "The `connected-customer`s associated with this `customer`."
  [customer]
  (:customer/connected customer))

(s/fdef connected-customers
        :args (s/cat :customer p/entity?)
        :ret (s/* p/entityd?))


(defn connected-customer-id
  "The Stripe Connect customer id for `connected-customer`."
  [connected-customer]
  (:connected-customer/customer-id connected-customer))

(s/fdef connected-customer-id
        :args (s/cat :connected-customer p/entity?)
        :ret string?)


(defn connected-customer-property
  "The property associated with this `connected-customer`."
  [connected-customer]
  (:connected-customer/property connected-customer))

(s/fdef connected-customer-property
        :args (s/cat :connected-customer p/entity?)
        :ret p/entityd?)


;; =============================================================================
;; Queries
;; =============================================================================


(defn by-account
  "Look up the `customer` entity associated with `account`."
  [db account]
  (->> (d/q '[:find ?e .
              :in $ ?a
              :where
              [?e :customer/account ?a]]
            db (td/id account))
       (d/entity db)))

(s/fdef by-account
        :args (s/cat :db p/db? :account p/entity?)
        :ret (s/or :account p/entityd? :nothing nil?))


(defn by-customer-id
  "Look up the `customer` entity associated with the Stripe `customer-id`."
  [db customer-id]
  (->> (d/q '[:find ?e .
              :in $ ?customer-id
              :where
              (or [?e :customer/platform-id ?customer-id]
                  (and [?e :customer/connected ?c]
                       [?c :connected-customer/customer-id ?customer-id]))]
            db customer-id)
       (d/entity db)))

(s/fdef by-customer-id
        :args (s/cat :db p/db?
                     :customer-id string?)
        :ret (s/or :customer p/entityd? :nothing nil?))


(defn connected-by-property
  "Produce the `connected-customer` entity for `account` that corresponds to
  `property`."
  [db account property]
  (->> (d/q '[:find ?e .
              :in $ ?a ?p
              :where
              [?c :customer/account ?a]
              [?c :customer/connected ?e]
              [?e :connected-customer/property ?p]]
            db (td/id account) (td/id property))
       (d/entity db)))

(s/fdef connected-by-property
        :args (s/cat :db p/db?
                     :account p/entity?
                     :property p/entity?)
        :ret (s/or :connected-customer p/entityd? :nothing nil?))


;; =============================================================================
;; Transactions
;; =============================================================================


;; (defn )
