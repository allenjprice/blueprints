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
;; Spec
;; =============================================================================


(s/def :order/status
  #{:order.status/pending
    :order.status/placed
    :order.status/processing
    :order.status/canceled
    :order.status/charged})

(s/def ::status :order/status)


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


(defn computed-name
  "A human-readable name for this service that takes variants into
  consideration."
  [order]
  (let [service (service order)]
    (if-let [vn (-> order variant :svc-variant/name)]
     (str (service/name service) " - " (string/capitalize vn))
     (service/name service))))0

(s/fdef computed-name
        :args (s/cat :order p/entity?)
        :ret string?)


;; =============================================================================
;; Predicates
;; =============================================================================


(defn ordered?
  "An order is considered /ordered/ when it has both an order placement time
  AND a subscription or non-failed charge."
  [order]
  (boolean
   (and (some? (ordered-at order))
        ;; TODO: Is this function needed any longer? If so, change the below
        ;; code to inspect payments
        (or (:stripe/subs-id order)
            (when-let [c (:stripe/charge order)]
              (not= (:charge/status c) :charge.status/failed))))))

(s/fdef ordered?
        :args (s/cat :order p/entity?)
        :ret boolean?)


(defn pending?
  "Is the order pending?"
  [order]
  (= (status order) :order.status/pending))

(s/fdef pending?
        :args (s/cat :order p/entity?)
        :ret boolean?)


(defn placed?
  "Has the order been placed?"
  [order]
  (= (status order) :order.status/placed))

(s/fdef placed?
        :args (s/cat :order p/entity?)
        :ret boolean?)


(defn processing?
  "Is the order processing?"
  [order]
  (= (status order) :order.status/processing))

(s/fdef processing?
        :args (s/cat :order p/entity?)
        :ret boolean?)


(defn canceled?
  "Is the order canceled?"
  [order]
  (= (status order) :order.status/canceled))

(s/fdef canceled?
        :args (s/cat :order p/entity?)
        :ret boolean?)


(defn charged?
  "Is the order charged?"
  [order]
  (= (status order) :order.status/charged))

(s/fdef charged?
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


(defn ^{:deprecated "1.11.0"} orders
  "DEPRECATED: use `orders2`.

  All of `account`'s orders."
  [db account]
  (->> (d/q '[:find [?o ...]
              :in $ ?a
              :where
              [?o :order/account ?a]]
            db (td/id account))
       (map (partial d/entity db))))

(s/fdef orders
        :args (s/cat :db p/db? :account p/entity?)
        :ret (s/* p/entityd?))


(defn- datekey->where-clauses [key]
  (case key
    :charged '[[?o :order/status :order.status/charged ?tx]
               [?tx :db/txInstant ?date]]
    '[[?o :order/account _ ?tx]
      [?tx :db/txInstant ?date]]))


(defn- orders-query
  [db {:keys [accounts billed services properties statuses datekey from to]
       :or   {datekey :created}}]
  (let [update clojure.core/update
        init   '{:find  [[?o ...]]
                 :in    [$]
                 :args  []
                 :where []}]
    (cond-> init
      true
      (update :args conj db)

      (not (empty? properties))
      (-> (update :in conj '[?p ...])
          (update :args conj (map td/id properties))
          (update :where conj
                  '[?o :order/account ?a]
                  '[?a :account/licenses ?license]
                  '[?license :member-license/unit ?unit]
                  '[?license :member-license/status :member-license.status/active]
                  '[?p :property/units ?unit]))

      (not (empty? accounts))
      (-> (update :in conj '[?a ...])
          (update :args conj (map td/id accounts))
          (update :where conj '[?o :order/account ?a]))

      (not (empty? services))
      (-> (update :in conj '[?s ...])
          (update :args conj (map td/id services))
          (update :where conj '[?o :order/service ?s]))

      (not (empty? billed))
      (-> (update :in conj '[?b ...])
          (update :args conj billed)
          (update :where conj
                  '[?o :order/service ?s]
                  '[?s :service/billed ?b]))

      (not (empty? statuses))
      (-> (update :in conj '[?status ...])
          (update :args conj statuses)
          (update :where conj '[?o :order/status ?status]))

      ;;; dates

      (or (some? from) (some? to))
      (update :where #(apply conj % (datekey->where-clauses datekey)))

      (some? from)
      (-> (update :in conj '?from)
          (update :args conj from)
          (update :where conj '[(.after ^java.util.Date ?date ?from)]))

      (some? to)
      (-> (update :in conj '?to)
          (update :args conj to)
          (update :where conj '[(.before ^java.util.Date ?date ?to)]))

      true
      (update :where #(if (empty? %) (conj % '[?o :order/account _]) %)))))


(defn orders2
  "Query orders with `params`."
  [db & {:as params}]
  (->> (orders-query db params)
       (td/remap-query)
       (d/query)
       (map (partial d/entity db))))

(s/def ::entities (s/+ p/entity?))
(s/def ::accounts ::entities)
(s/def ::billed #{:service.billed/monthly
                  :service.billed/once})
(s/def ::services ::entities)
(s/def ::properties ::entities)
(s/def ::statuses (s/+ :order/status))
(s/def ::datekey #{:created :charged})
(s/def ::from inst?)
(s/def ::to inst?)
(s/fdef orders2
        :args (s/cat :db p/db?
                     :params (s/keys* :opt-un [::accounts
                                               ::billed
                                               ::services
                                               ::properties
                                               ::statuses
                                               ::datekey
                                               ::from
                                               ::to]))
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


(s/def ::quantity (s/and pos? number?))
(s/def ::desc string?)
(s/def ::variant integer?)
(s/def ::price (s/and pos? float?))
(s/def ::opts (s/keys :opt-un [::quantity ::desc ::variant ::price ::status]))


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
    :order/quantity (when-let [q quantity] (float q))
    :order/desc desc)))

(s/fdef create
        :args (s/cat :account p/entity?

                     :service p/entity?
                     :opts    (s/? ::opts))
        :ret map?)


(defn update
  [order {:keys [quantity desc variant status]}]
  (tb/assoc-when
   {:db/id (td/id order)}
   :order/status status
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
  "Add a payment to this order."
  [order payment]
  {:db/id          (td/id order)
   :order/payments (td/id payment)})

(s/fdef add-charge
        :args (s/cat :order p/entity? :charge p/entity?)
        :ret (s/keys :req [:db/id :order/status :stripe/charge]))


(defn is-placed
  "The order has been placed."
  [order]
  {:db/id        (td/id order)
   :order/status :order.status/placed})


(defn is-processing
  "The order is being processed."
  [order]
  {:db/id        (td/id order)
   :order/status :order.status/processing})


(defn is-canceled
  "The order has been canceled."
  [order]
  {:db/id        (td/id order)
   :order/status :order.status/canceled})


(defn is-charged
  "The order has been charged."
  [order]
  {:db/id        (td/id order)
   :order/status :order.status/charged})


;; =============================================================================
;; Clientize
;; =============================================================================


(defn clientize
  [order]
  (let [service (:order/service order)
        desc    (if (string/blank? (desc order)) (service/desc service) (desc order))]
    {:id       (td/id order)
     :name     (computed-name order)
     :desc     desc
     :price    (computed-price order)
     :rental   (service/rental service)
     :quantity (quantity order)
     :billed   (-> service :service/billed clojure.core/name keyword)}))
