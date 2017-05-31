(ns blueprints.models.charge
  (:refer-clojure :exclude [type])
  (:require [clojure.spec :as s]
            [datomic.api :as d]
            [plumbing.core :as plumbing]
            [toolbelt.predicates :as p]))

;; =============================================================================
;; Selectors
;; =============================================================================

(declare is-security-deposit-charge? is-rent-ach-charge?)

(s/def ::status #{:charge.status/succeeded
                  :charge.status/failed
                  :charge.status/pending})

(def account :charge/account)
(def status :charge/status)
(def amount :charge/amount)
(def id :charge/stripe-id)

(defn type
  "The type of charge."
  [db charge]
  (cond
    (is-security-deposit-charge? db charge) :security-deposit
    (is-rent-ach-charge? db charge)         :rent
    :otherwise                              :default))

(s/fdef type
        :args (s/cat :db p/db? :charge p/entity?)
        :ret #{:security-deposit :rent :default})

;; =============================================================================
;; Queries
;; =============================================================================

(defn lookup
  "Look up a charge by the external `charge-id`."
  [db charge-id]
  (d/entity db [:charge/stripe-id charge-id]))

;; =============================================================================
;; Predicates
;; =============================================================================

(defn- status? [status charge]
  (= (:charge/status charge) status))

(def succeeded? (partial status? :charge.status/succeeded))
(def failed? (partial status? :charge.status/failed))
(def pending? (partial status? :charge.status/pending))

(defn is-rent-ach-charge?
  "Returns `true` if `charge` is part of a rent payment."
  [db charge]
  (d/q '[:find ?e .
         :in $ ?c
         :where
         [?e :rent-payment/charge ?c]
         [?e :rent-payment/method :rent-payment.method/ach]]
       db (:db/id charge)))

(defn is-security-deposit-charge?
  "Returns `true` if `charge` is part of a security deposit."
  [db charge]
  (d/q '[:find ?security-deposit .
         :in $ ?charge
         :where
         [?security-deposit :security-deposit/charges ?charge]]
       db (:db/id charge)))

;; =============================================================================
;; Transactions
;; =============================================================================

(defn create
  "Create a new charge."
  [stripe-id amount & {:keys [purpose status account]
                       :or   {status :charge.status/pending}}]
  (plumbing/assoc-when
   {:db/id            (d/tempid :db.part/starcity)
    :charge/stripe-id stripe-id
    :charge/amount    amount
    :charge/status    status}
   :charge/account (:db/id account)
   :charge/purpose purpose))

(s/def ::account p/entity?)
(s/def ::purpose string?)
(s/fdef create
        :args (s/cat :stripe-id string?
                     :amount float?
                     :opts (s/keys* :opt-un [::purpose ::status ::account]))
        :ret (s/keys :req [:db/id :charge/stripe-id :charge/status :charge/amount]
                     :opt [:charge/purpose :charge/account]))

(defn- status-tx [status charge]
  {:db/id         (:db/id charge)
   :charge/status status})

(def succeeded
  "The transaction to mark `charge` as succeeded."
  (partial status-tx :charge.status/succeeded))

(def failed
  "The transaction to mark `charge` as failed."
  (partial status-tx :charge.status/failed))

(def pending
  "The transaction to mark `charge` as pending."
  (partial status-tx :charge.status/pending))
