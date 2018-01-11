(ns blueprints.models.charge
  (:refer-clojure :exclude [type])
  (:require [clojure.spec.alpha :as s]
            [datomic.api :as d]
            [toolbelt.core :as tb]
            [toolbelt.datomic :as td]))


;; =============================================================================
;; Specs
;; =============================================================================


(s/def ::status #{:charge.status/succeeded
                  :charge.status/failed
                  :charge.status/pending})


(s/def ::type #{:security-deposit
                :service
                :rent
                :unknown})


;; =============================================================================
;; Selectors
;; =============================================================================


(def account
  "The account this charge is associated with."
  :charge/account)

(s/fdef account
        :args (s/cat :charge td/entity?)
        :ret td/entity?)

(def status
  :charge/status)

(s/fdef status
        :args (s/cat :charge td/entity?)
        :ret ::status)


(def amount
  :charge/amount)

(s/fdef amount
        :args (s/cat :charge td/entity?)
        :ret float?)

(def id
  "The Stripe id of this charge."
  :charge/stripe-id)

(s/fdef id
        :args (s/cat :charge td/entity?)
        :ret string?)


(def invoice-id
  "The Stripe id of the invoice this charge belongs to."
  :stripe/invoice-id)

(s/fdef invoice-id
        :args (s/cat :charge td/entity?)
        :ret (s/or :id string? :nothing nil?))


(declare is-security-deposit-charge? is-rent-ach-charge? is-service-charge?)

(defn type
  "The type of charge."
  [db charge]
  (cond
    (is-security-deposit-charge? db charge) :security-deposit
    (is-rent-ach-charge? db charge)         :rent
    (is-service-charge? db charge)          :service
    :otherwise                              :unknown))

(s/fdef type
        :args (s/cat :db td/db? :charge td/entity?)
        :ret ::type)


;; =============================================================================
;; Predicates
;; =============================================================================


(defn- status? [status charge]
  (= (:charge/status charge) status))

(s/fdef status?
        :args (s/cat :status ::status :charge td/entity?)
        :ret boolean?)


(def succeeded? (partial status? :charge.status/succeeded))

(def failed? (partial status? :charge.status/failed))

(def pending? (partial status? :charge.status/pending))


(defn is-rent-ach-charge?
  "Is this charge associated with a rent payment (that was made via ACH)?"
  [db charge]
  (boolean
   (d/q '[:find ?e .
          :in $ ?c
          :where
          [?e :rent-payment/charge ?c]
          [?e :rent-payment/method :rent-payment.method/ach]]
        db (td/id charge))))

(s/fdef is-rent-ach-charge?
        :args (s/cat :db td/db? :charge td/entity?)
        :ret boolean?)


(defn is-security-deposit-charge?
  "Is this charge associated with a security deposit?"
  [db charge]
  (boolean
   (d/q '[:find ?e .
          :in $ ?c
          :where
          [?e :security-deposit/charges ?c]]
        db (td/id charge))))

(s/fdef is-security-deposit-charge?
        :args (s/cat :db td/db? :charge td/entity?)
        :ret boolean?)


;; NOTE: [6/29/17] This is a sort of patch while we migrate over to exclusively
;; using the new `payment` entity.
(defn is-service-charge?
  "Is this charge associated with a premium service order?"
  [db charge]
  (boolean
   (d/q '[:find ?e .
          :in $ ?c
          :where
          [?c :charge/stripe-id ?sid]   ; get id of charge
          [?e :payment/id _]            ; there's a payment...
          [?e :stripe/charge-id ?sid]]  ; ...that also references this charge's id
        db (td/id charge))))

(s/fdef is-service-charge?
        :args (s/cat :db td/db? :charge td/entity?)
        :ret boolean?)


;; =============================================================================
;; Transactions
;; =============================================================================


(defn create
  "Create a new charge."
  [account stripe-id amount & {:keys [purpose status invoice-id]
                               :or   {status :charge.status/pending}}]
  (tb/assoc-when
   {:db/id            (d/tempid :db.part/starcity)
    :charge/account   (td/id account)
    :charge/stripe-id stripe-id
    :charge/amount    amount
    :charge/status    status}
   :charge/purpose purpose
   :stripe/invoice-id invoice-id))

(s/def ::purpose string?)
(s/def ::invoice-id string?)
(s/fdef create
        :args (s/cat :account td/entity?
                     :stripe-id string?
                     :amount float?
                     :opts (s/keys* :opt-un [::purpose ::status ::invoice-id]))
        :ret (s/keys :req [:db/id :charge/stripe-id :charge/status :charge/amount]
                     :opt [:charge/purpose :charge/account :stripe/invoice-id]))


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


;; =============================================================================
;; Queries
;; =============================================================================


(defn lookup
  "Look up a charge by the external `charge-id`."
  [db charge-id]
  (d/entity db [:charge/stripe-id charge-id]))

(s/fdef lookup
        :args (s/cat :db td/db? :charge-id string?)
        :ret (s/or :entity td/entity? :nothing nil?))


(def by-id
  "Look up a charge by the external `charge-id`."
  lookup)


(defn by-invoice-id
  "Look up a charge by its invoice id."
  [db invoice-id]
  (-> (d/q '[:find ?e .
             :in $ ?i
             :where
             [?e :stripe/invoice-id ?i]
             [?e :charge/stripe-id _]]
           db invoice-id)
      (td/entity db)))


(s/fdef by-invoice-id
        :args (s/cat :db td/db? :invoice-id string?)
        :ret (s/or :entity td/entity? :nothing nil?))
