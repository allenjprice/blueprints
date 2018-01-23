(ns blueprints.models.transaction
  (:require [clojure.spec.alpha :as s]
            [datomic.api :as d]
            [toolbelt.core :as tb]
            [toolbelt.datomic :as td]))


;; =============================================================================
;; Selectors
;; =============================================================================

(defn source-id
  "The id used by Stripe to represent the related source of the balance
  transaction entity."
  [transaction]
  (:transaction/source-id transaction))

(s/fdef source
        :args (s/cat :transaction td/entity?)
        :ret string?)


(defn payment
  "Payment is used by Stripe to represent this balance transaction entity, if
  there was a payment."
  [transaction]
  (:transaction/payment transaction))

(s/fdef payment
        :args (s/cat :transaction td/entity?)
        :ret (s/nilable td/entityd?))


(defn id
  "Id used by Stripe to represent this balance transaction."
  [transaction]
  (:transaction/id transaction))

(s/fdef id
        :args (s/cat :transaction td/entity?)
        :ret string?)


(defn payout-id
  "Payout-id used by Stripe to represent the payout of this balance transaction
  entity."
  [transaction]
  (:transaction/payout-id transaction))

(s/fdef payout-id
        :args (s/cat :transaction td/entity?)
        :ret string?)


;; =============================================================================
;; Queries
;; =============================================================================


(defn by-source-id
  "Look up a balance transaction entity by Stripe source-id"
  [db source-id]
  (d/entity db [:transaction/source-id source-id]))

(s/fdef by-source-id
        :args (s/cat :db td/db? :source-id string?)
        :ret (s/or :transaction td/entity? :nothing nil?))


(defn by-payment
  "Look up a balance transaction entity by `payment`."
  [db payment]
  (->> (d/q '[:find ?e .
              :in $ ?p
              :where
              [?e :transaction/payment ?p]]
            db (td/id payment))
       (d/entity db)))

(s/fdef by-payment
        :args (s/cat :db td/db? :payment string?)
        :ret (s/or :transaction td/entity? :nothing nil?))


(defn by-id
  "Look up a balance transaction entity by Stripe id"
  [db id]
  (d/entity db [:transaction/id id]))

(s/fdef by-id
        :args (s/cat :db td/db? :id string?)
        :ret  (s/or :transaction td/entity? :nothing nil?))


(defn by-payout-id
  "Look up a balance transaction entity by Stripe payout-id"
  [db payout-id]
  (d/entity db [:transaction/payout-id payout-id]))

(s/fdef by-payout-id
        :args (s/cat :db td/db? :payout-id string?)
        :ret (s/or :transaction td/entity? :nothing nil?))

;; =============================================================================
;; Transactions
;; =============================================================================


(s/def ::payment
  td/entity?)

(s/def ::payout-id
  string?)


(defn create
  "Create a new transaction entity."
  [id source-id & {:keys [payment payout-id]}]
  (tb/assoc-when
   {:transaction/id id
    :transaction/source-id source-id}
   :transaction/payment (when-some [p payment] (td/id p))
   :transaction/payout-id payout-id))

(s/fdef create
        :args (s/cat :id string?
                     :source-id string?
                     :opt (s/keys* :opt-un [::payment ::payout-id]))
        :ret (s/or :transaction td/entityd? :nothing nil?))
