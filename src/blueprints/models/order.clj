(ns blueprints.models.order
  (:refer-clojure :exclude [update])
  (:require [blueprints.models.service :as service]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [datomic.api :as d]
            [toolbelt.core :as tb]
            [toolbelt.datomic :as td]))


;; =============================================================================
;; Spec
;; =============================================================================


(s/def :order/status
  #{:order.status/pending
    :order.status/placed
    :order.status/fulfilled
    :order.status/processing
    :order.status/failed
    :order.status/charged
    :order.status/canceled})

(s/def ::status :order/status)


;; =============================================================================
;; Selectors
;; =============================================================================


(defn account
  "The account that placed this order."
  [order]
  (:order/account order))

(s/fdef account
        :args (s/cat :order td/entity?)
        :ret td/entity?)


(defn price
  "The price of this `order`."
  [order]
  (:order/price order))

(s/fdef price
        :args (s/cat :order td/entity?)
        :ret (s/or :nothing nil? :price float?))


(defn line-items
  "Any line items associated with this order."
  [order]
  (:order/lines order))

(s/fdef line-items
        :args (s/cat :order td/entity?)
        :ret (s/or :nothing nil? :lines (s/+ td/entityd?)))


(defn computed-price
  "The price of this `order`, taking into consideration possible variants, line
  items and the price of the service."
  [order]
  (or (when-some [ls (line-items order)]
        (->> ls (map :line-item/price) (apply +)))
      (price order)
      (-> order :order/variant :svc-variant/price)
      (service/price (:order/service order))))

(s/fdef computed-price
        :args (s/cat :order td/entity?)
        :ret (s/or :nothing nil? :price float?))


(defn cost
  "The cost of this order in dollars."
  [order]
  (:order/cost order))

(s/fdef cost
        :args (s/cat :order td/entity?)
        :ret (s/or :nothing nil? :cost float?))


(defn line-item-cost [order]
  (->> order line-items (map #(:line-item/cost % 0)) (apply +)))


(defn computed-cost
  "The cost of this `order`, taking into consideration possible variants, line
  items and the cost of the service."
  [order]
  (or (let [lc (line-item-cost order)]
        (when-not (zero? lc) lc))
      (cost order)
      (-> order :order/variant :svc-variant/cost)
      (service/cost (:order/service order))))

(s/fdef computed-cost
        :args (s/cat :order td/entity?)
        :ret (s/or :nothing nil? :cost float?))


(defn quantity
  "The number of `service` ordered."
  [order]
  (:order/quantity order))

(s/fdef quantity
        :args (s/cat :order td/entity?)
        :ret (s/or :nothing nil? :quantity pos-int?))


(defn request
  "Accompanying text with the order request, provided by user."
  [order]
  (:order/request order))

(s/fdef request
        :args (s/cat :order td/entity?)
        :ret (s/or :nothing nil? :request string?))


(defn summary
  "Summary of the order provided by Starcity."
  [order]
  (:order/summary order))

(s/fdef summary
        :args (s/cat :order td/entity?)
        :ret (s/or :nothing nil? :summary string?))


(defn variant
  "The variant of the service chosen with this order."
  [order]
  (:order/variant order))

(s/fdef variant
        :args (s/cat :order td/entity?)
        :ret (s/or :nothing nil? :variant td/entity?))


(defn status
  "The status of this order."
  [order]
  (:order/status order))

(s/fdef status
        :args (s/cat :order td/entity?)
        :ret :order/status)


(defn service
  "The service that this order is for."
  [order]
  (:order/service order))

(s/fdef service
        :args (s/cat :order td/entity?)
        :ret td/entity?)


(defn payments
  "Payments made towards `order`."
  [order]
  (:order/payments order))

(s/fdef payments
        :args (s/cat :order td/entity?)
        :ret (s/* td/entity?))


(defn computed-name
  "A human-readable name for this service that takes variants into
  consideration."
  [order]
  (let [service (service order)]
    (if-let [vn (-> order variant :svc-variant/name)]
      (str (service/service-name service) " - " (string/capitalize vn))
      (service/service-name service))))0

(s/fdef computed-name
        :args (s/cat :order td/entity?)
        :ret string?)


(defn billed-on
  "The date at which this order was billed."
  [order]
  (:order/billed-on order))

(s/fdef billed-on
        :args (s/cat :order td/entity?)
        :ret (s/or :nothing nil? :date inst?))


(defn fulfilled-on
  "The date at which this order was fulfilled."
  [order]
  (:order/fulfilled-on order))

(s/fdef fulfilled
        :args (s/cat :order td/entity?)
        :ret (s/or :nothing nil? :date inst?))


(defn projected-fulfillment
  "The date at which this order will be fulfilled."
  [order]
  (:order/projected-fulfillment order))

(s/fdef projected-fulfillment
        :args (s/cat :order td/entity?)
        :ret (s/or :nothing nil? :date inst?))


;; =============================================================================
;; Predicates
;; =============================================================================


(defn pending?
  "Is the order pending?"
  [order]
  (= (status order) :order.status/pending))

(s/fdef pending?
        :args (s/cat :order td/entity?)
        :ret boolean?)


(defn placed?
  "Has the order been placed?"
  [order]
  (= (status order) :order.status/placed))

(s/fdef placed?
        :args (s/cat :order td/entity?)
        :ret boolean?)



(defn fulfilled?
  "Has the order been fulfilled?"
  [order]
  (= (status order) :order.status/fulfilled))

(s/fdef placed?
        :args (s/cat :order td/entity?)
        :ret boolean?)


(defn processing?
  "Is the order processing?"
  [order]
  (= (status order) :order.status/processing))

(s/fdef processing?
        :args (s/cat :order td/entity?)
        :ret boolean?)


(defn failed?
  "Has the order failed to charge?"
  [order]
  (= (status order) :order.status/failed))

(s/fdef failed?
        :args (s/cat :order td/entity?)
        :ret boolean?)


(defn charged?
  "Is the order charged?"
  [order]
  (= (status order) :order.status/charged))

(s/fdef charged?
        :args (s/cat :order td/entity?)
        :ret boolean?)


(defn canceled?
  "Is the order canceled?"
  [order]
  (= (status order) :order.status/canceled))

(s/fdef canceled?
        :args (s/cat :order td/entity?)
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
        :args (s/cat :db td/db?
                     :account td/entity?
                     :service td/entity?)
        :ret (s/or :entity td/entity? :nothing nil?))


(def exists?
  "Does `account` have an order for `service`?"
  (comp td/entity? by-account))

(s/fdef exists?
        :args (s/cat :db td/db? :account td/entity? :service td/entity?)
        :ret boolean?)


(defn- datekey->where-clauses [key]
  (case key
    :billed    '[[?o :order/billed-on ?date]]
    :fulfilled '[[?o :order/fulfilled-on ?date]]
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


(defn query
  "Query orders with `params`."
  [db & {:as params}]
  (->> (orders-query db params)
       (td/remap-query)
       (d/query)
       (map (partial d/entity db))))

(s/def ::entities (s/+ td/entity?))
(s/def ::accounts ::entities)
(s/def ::billed #{:service.billed/monthly
                  :service.billed/once})
(s/def ::services ::entities)
(s/def ::properties ::entities)
(s/def ::statuses (s/+ :order/status))
(s/def ::datekey #{:created :billed :fulfilled})
(s/def ::from inst?)
(s/def ::to inst?)

(s/fdef query
        :args (s/cat :db td/db?
                     :params (s/keys* :opt-un [::accounts
                                               ::billed
                                               ::services
                                               ::properties
                                               ::statuses
                                               ::datekey
                                               ::from
                                               ::to]))
        :ret (s/* td/entityd?))


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
        :args (s/cat :db td/db? :payment td/entity?)
        :ret (s/or :entity td/entityd? :nothing nil?))


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
        :args (s/cat :db td/db? :sub-id string?)
        :ret (s/or :entity td/entityd? :nothing nil?))


;; =============================================================================
;; Transactions
;; =============================================================================


(s/def :line-item/desc string?)
(s/def :line-item/price (s/and pos? float?))
(s/def :line-item/cost float?)
(s/def ::line
  (s/keys :req [:line-item/desc :line-item/price] :opt [:line-item/cost]))

(s/def ::quantity (s/and pos? number?))
(s/def ::request string?)
(s/def ::summary string?)
(s/def ::variant integer?)
(s/def ::price (s/and pos? float?))
(s/def ::cost (s/and pos? float?))
(s/def ::lines (s/+ ::line))
(s/def ::create-opts
  (s/keys :opt-un [::quantity ::desc ::request ::cost ::summary ::variant ::status ::price ::lines]))


(defn create
  "Create a new order."
  ([account service]
   (create account service {}))
  ([account service {:keys [quantity desc request cost summary variant status price lines]
                     :or   {status :order.status/pending}
                     :as   opts}]
   (tb/assoc-when
    {:db/id         (d/tempid :db.part/starcity)
     :order/uuid    (d/squuid)
     :order/service (td/id service)
     :order/account (td/id account)
     :order/status  status}
    :order/price (when-let [p price] (float price))
    :order/cost (when-let [c cost] (float cost))
    :order/variant variant
    :order/quantity (when-let [q quantity] (float q))
    :order/lines lines
    :order/summary summary
    :order/request (or request desc))))

(s/fdef create
        :args (s/cat :account td/entity?

                     :service td/entity?
                     :opts    (s/? ::create-opts))
        :ret map?)


(defn- update-or-retract
  [params entity params<->attrs]
  (reduce
   (fn [acc [param attr]]
     (let [pv (get params param)
           ev (get entity attr)]
       (->> (cond
              (and (some? ev) (contains? params param) (nil? pv))
              [:db/retract (:db/id entity) attr ev]

              (and (some? pv) (not= ev pv))
              [:db/add (:db/id entity) attr pv])
            (tb/conj-when acc))))
   []
   params<->attrs))


(defn update
  "Update `order` with `opts.`"
  [order {:keys [status lines variant] :as opts}]
  (let [m (tb/assoc-when
           {}
           :order/status status
           :order/variant variant
           :order/lines lines)]
    (-> opts
        (tb/update-in-some [:cost] float)
        (tb/update-in-some [:price] float)
        (update-or-retract order {:cost     :order/cost
                                  :price    :order/price
                                  :request  :order/request
                                  :summary  :order/summary
                                  :quantity :order/quantity})
        (tb/conj-when (when-not (empty? m) (assoc m :db/id (td/id order)))))))

(s/fdef update
        :args (s/cat :order td/entity?
                     :opts  map?)
        :ret vector?)


(defn remove-existing
  [db account service]
  (when-let [order (by-account db account service)]
    [:db/retractEntity (td/id order)]))

(s/fdef remove-existing
        :args (s/cat :db td/db? :account td/entity? :service td/entity?))


(defn add-payment
  "Add a payment to this order."
  [order payment]
  {:db/id          (td/id order)
   :order/payments (td/id payment)})

(s/fdef add-charge
        :args (s/cat :order td/entity? :charge td/entity?)
        :ret (s/keys :req [:db/id :order/status :stripe/charge]))


(defn is-placed
  "The order has been placed."
  [order]
  {:db/id        (td/id order)
   :order/status :order.status/placed})


(defn is-fulfilled
  "The order is fulfilled."
  [order fulfilled-on]
  {:db/id              (td/id order)
   :order/status       :order.status/fulfilled
   :order/fulfilled-on fulfilled-on})


(defn is-processing
  "The order is being processed."
  [order]
  {:db/id        (td/id order)
   :order/status :order.status/processing})


(defn is-charged
  "The order has been charged."
  [order]
  {:db/id        (td/id order)
   :order/status :order.status/charged})


(defn is-canceled
  "The order has been canceled."
  [order]
  {:db/id        (td/id order)
   :order/status :order.status/canceled})

(defn is-failed
  "The order has faile to process."
  [order]
  {:db/id        (td/id order)
   :order/status :order.status/failed})


(defn line-item
  "Create an order line-item."
  [desc price & [cost]]
  (tb/assoc-when
   {:line-item/desc  desc
    :line-item/price price}
   :line-item/cost (when-some [c cost] (float c))))

(s/fdef line-item
        :args (s/cat :desc string?
                     :price (s/and number? pos?)
                     :cost (s/? number?))
        :ret map?)


;; =============================================================================
;; Deprecated
;; =============================================================================


(def ^{:deprecated "1.13.0"} ordered-on
  "Instant at which the order was created."
  :order/ordered)

(s/fdef ordered-on
        :args (s/cat :order td/entity?)
        :ret (s/or :inst inst? :nothing nil?))


(def ^{:deprecated "1.14.0"} desc request)


(defn ^{:deprecated "1.14.0"} clientize
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


(defn ^{:deprecated "1.13.0"} ordered?
  "An order is considered /ordered/ when it has both an order placement time
  AND a subscription or non-failed charge."
  [order]
  (boolean
   (and (some? (ordered-on order))
        ;; TODO: Is this function needed any longer? If so, change the below
        ;; code to inspect payments
        (or (:stripe/subs-id order)
            (when-let [c (:stripe/charge order)]
              (not= (:charge/status c) :charge.status/failed))))))

(s/fdef ordered?
        :args (s/cat :order td/entity?)
        :ret boolean?)


(defn ^{:deprecated "1.11.0"} orders
  "DEPRECATED: use `query`.

  All of `account`'s orders."
  [db account]
  (->> (d/q '[:find [?o ...]
              :in $ ?a
              :where
              [?o :order/account ?a]]
            db (td/id account))
       (map (partial d/entity db))))

(s/fdef orders
        :args (s/cat :db td/db? :account td/entity?)
        :ret (s/* td/entityd?))
