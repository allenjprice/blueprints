(ns blueprints.models.order
  (:refer-clojure :exclude [update])
  (:require [blueprints.models.service :as service]
            [clojure.spec :as s]
            [toolbelt.predicates :as p]
            [datomic.api :as d]
            [toolbelt.core :as tb]
            [clojure.string :as string]
            [toolbelt.datomic :as td]))


;; =============================================================================
;; Selectors
;; =============================================================================


(def account
  "The account that placed this order."
  :order/account)

(s/fdef account
        :args (s/cat :order p/entity?)
        :ret p/entity?)


(defn price
  "The price of this `order`."
  [order]
  (:order/price order))

(s/fdef price
        :args (s/cat :order p/entity?)
        :ret (s/or :nothing nil? :price float?))


(defn computed-price
  "The price of this `order`, taking into consideration possible variants and
  the price of the service."
  [order]
  (or (:order/price order)
      (-> order :order/variant :svc-variant/price)
      (service/price (:order/service order))))

(s/fdef computed-price
        :args (s/cat :order p/entity?)
        :ret (s/or :nothing nil? :price float?))


(defn quantity
  "The number of `service` ordered."
  [order]
  (:order/quantity order))

(s/fdef quantity
        :args (s/cat :order p/entity?)
        :ret (s/or :nothing nil? :quantity pos-int?))


(def desc
  "The description of the order."
  :order/desc)

(s/fdef desc
        :args (s/cat :order p/entity?)
        :ret (s/or :nothing nil? :desc string?))


(def variant
  "The variant of the service chosen with this order."
  :order/variant)

(s/fdef desc
        :args (s/cat :order p/entity?)
        :ret (s/or :nothing nil? :variant p/entity?))


;; TODO: Rethink this
(def ordered-at
  "Instant at which the order was placed."
  :order/ordered)

(s/fdef ordered-at
        :args (s/cat :order p/entity?)
        :ret (s/or :inst inst? :nothing nil?))


(def status
  "The status of this order."
  :order/status)

(s/fdef status
        :args (s/cat :order p/entity?)
        :ret #{:order.status/pending
               :order.status/canceled
               :order.status/placed
               :order.status/charged})


(def service
  "The service that this order is for."
  :order/service)

(s/fdef service
        :args (s/cat :order p/entity?)
        :ret p/entity?)


(def payments
  "This order's payments."
  :order/payments)

(s/fdef payments
        :args (s/cat :order p/entity?)
        :ret (s/* p/entity?))


;; =============================================================================
;; Predicates
;; =============================================================================


(defn ordered?
  "An order is considered /ordered/ when it has both an order placement time
  AND a subscription or non-failed charge."
  [order]
  (boolean
   (and (some? (ordered-at order))
        (or (:stripe/subs-id order)
            (when-let [c (:stripe/charge order)]
              (not= (:charge/status c) :charge.status/failed))))))

(s/fdef ordered?
        :args (s/cat :order p/entity?)
        :ret boolean?)


(defn placed?
  "Has the order been placed?"
  [order]
  (= (status order) :order.status/placed))

(s/fdef placed?
        :args (s/cat :order p/entity?)
        :ret boolean?)


;; =============================================================================
;; Queries
;; =============================================================================


(defn by-account
  "Find an order given the `account` and `service`."
  [db account service]
  (->> (d/q '[:find ?o .
              :in $ ?a ?s
              :where
              [?o :order/account ?a]
              [?o :order/service ?s]]
            db (td/id account) (td/id service))
       (d/entity db)))

(s/fdef by-account
        :args (s/cat :db p/db?
                     :account p/entity?
                     :service p/entity?)
        :ret (s/or :entity p/entity? :nothing nil?))


(def exists?
  "Does `account` have an order for `service`?"
  (comp p/entity? by-account))

(s/fdef exists?
        :args (s/cat :db p/db? :account p/entity? :service p/entity?)
        :ret boolean?)


(defn orders
  "All of `account`'s orders."
  [db account]
  (->> (d/q '[:find [?o ...]
              :in $ ?a
              :where
              [?o :order/account ?a]]
            db (td/id account))
       (map (partial d/entity db))))

(s/fdef orders
        :args (s/cat :db p/entity? :account p/entity?)
        :ret (s/* p/entityd?))


(defn by-payment
  "Find an order given the payment associated with it."
  [db payment]
  (->> (d/q '[:find ?e .
              :in $ ?p
              :where
              [?e :order/payments ?p]]
            db (td/id payment))
       (d/entity db)))

(s/fdef by-payment
        :args (s/cat :db p/db? :payment p/entity?)
        :ret (s/or :entity p/entityd? :nothing nil?))


(defn by-subscription-id
  "Find an order given the id of a Stripe subscription."
  [db sub-id]
  (->> (d/q '[:find ?e .
              :in $ ?s
              :where
              [?e :stripe/subs-id ?s]
              [?e :order/account _]]
            db sub-id)
       (d/entity db)))

(s/fdef by-subscription-id
        :args (s/cat :db p/db? :sub-id string?)
        :ret (s/or :entity p/entityd? :nothing nil?))


;; =============================================================================
;; Transactions
;; =============================================================================


(s/def ::quantity (s/and pos? float?))
(s/def ::desc string?)
(s/def ::variant integer?)
(s/def ::price (s/and pos? float?))
(s/def ::opts (s/keys :opt-un [::quantity ::desc ::variant ::price]))


(defn create
  "Create a new order."
  ([account service]
   (create account service {}))
  ([account service {:keys [quantity desc variant status price]
                     :or   {status :order.status/pending}}]
   (tb/assoc-when
    {:db/id         (d/tempid :db.part/starcity)
     :order/uuid    (d/squuid)
     :order/service (td/id service)
     :order/account (td/id account)
     :order/status  status}
    :order/price price
    :order/variant variant
    :order/quantity quantity
    :order/desc desc)))

(s/fdef create
        :args (s/cat :account p/entity?

                     :service p/entity?
                     :opts    (s/? ::opts))
        :ret map?)


(defn update
  [order {:keys [quantity desc variant]}]
  (tb/assoc-when
   {:db/id (td/id order)}
   :order/quantity quantity
   :order/desc desc
   :order/variant variant))

(s/fdef update
        :args (s/cat :order p/entity?
                     :opts  ::opts)
        :ret map?)


(defn remove-existing
  [db account service]
  (when-let [order (by-account db account service)]
    [:db/retractEntity (td/id order)]))

(s/fdef remove-existing
        :args (s/cat :db p/db? :account p/entity? :service p/entity?))


(defn add-payment
  "Add a payment to this order, changing the order's status to `:order.status/placed`."
  [order payment]
  {:db/id          (td/id order)
   :order/status   :order.status/placed
   :order/payments (td/id payment)})

(s/fdef add-charge
        :args (s/cat :order p/entity? :charge p/entity?)
        :ret (s/keys :req [:db/id :order/status :stripe/charge]))


;; The `charged` status doesn't really seem necessary. This is information we
;; can obtain from inspecting the payments.

#_(defn is-charged
  "The order has been charged."
  [order]
  {:db/id        order
   :order/status :order.status/charged})


;; =============================================================================
;; Clientize
;; =============================================================================


(defn- variant-name [order]
  (-> order :order/variant :svc-variant/name))


(defn clientize
  [order]
  (let [service (:order/service order)
        desc    (if (string/blank? (desc order)) (service/desc service) (desc order))
        name    (if-let [vn (variant-name order)]
                  (str (service/name service) " - " (string/capitalize vn))
                  (service/name service))]
    {:id       (td/id order)
     :name     name
     :desc     desc
     :price    (computed-price order)
     :rental   (service/rental service)
     :quantity (quantity order)
     :billed   (-> service :service/billed clojure.core/name keyword)}))
