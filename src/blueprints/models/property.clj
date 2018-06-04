(ns blueprints.models.property
  (:refer-clojure :exclude [name])
  (:require [blueprints.models.unit :as unit]
            [clj-time.core :as t]
            [clojure.spec.alpha :as s]
            [datomic.api :as d]
            [toolbelt.datomic :as td]
            [toolbelt.core :as tb]))

;; =============================================================================
;; Selectors
;; =============================================================================


(def name
  "The name of this property."
  :property/name)

(s/fdef name
        :args (s/cat :property td/entity?)
        :ret string?)


(def internal-name
  "The internal (name) of this property. Can be used as a lookup."
  :property/code)

(s/fdef internal-name
        :args (s/cat :property td/entity?)
        :ret string?)


(def code
  "Alias to `property/internal-name`"
  internal-name)


(def ^{:added "1.11.0"} cover-image-url
  "URL for the image representing this property in our UI."
  :property/cover-image-url)

(s/fdef cover-image-url
        :args (s/cat :property td/entity?)
        :ret string?)


(def ^{:deprecated "1.10.0"} managed-account-id
  "DEPRECATED: Use `property/rent-connect-id` instead."
  :property/managed-account-id)


(def ^{:added "1.10.0"} rent-connect-id
  "The id for the account to route rent payments to."
  :property/rent-connect-id)

(s/fdef rent-connect-id
        :args (s/cat :property td/entity?)
        :ret string?)

;; Because not only rent is subject to this now
(def ^{:added "1.17.0"} connect-id
  rent-connect-id)


(def ^{:added "1.10.0"} deposit-connect-id
  "The id for the account to route security deposit payments to."
  :property/deposit-connect-id)

(s/fdef deposit-connect-id
        :args (s/cat :property td/entity?)
        :ret string?)


(def ^{:deprecated "1.17.0"} ops-fee
  "The percentage of rent payments to route to the parent Stripe account."
  :property/ops-fee)

(s/fdef ops-fee
        :args (s/cat :property td/entity?)
        :ret float?)


(defn ops-fee-rent
  "The percentage of rent payments to route to the platform Stripe account."
  [property]
  (:property/ops-fee-rent property))

(s/fdef ops-fee-rent
        :args (s/cat :property td/entity?)
        :ret float?)


(defn ops-fee-orders
  "The percentage of premium service order payments to route to the platform
  Stripe account."
  [property]
  (:property/ops-fee-orders property))

(s/fdef ops-fee-orders
        :args (s/cat :property td/entity?)
        :ret float?)


(def units
  "Units in this property."
  :property/units)

(s/fdef units
        :args (s/cat :property td/entity?)
        :ret (s/+ td/entityd?))


;; TODO: Actually store this in the DB.
(defn time-zone
  "Produce the time zone that `property` is in."
  [property]
  (t/time-zone-for-id "America/Los_Angeles"))


(def available-on
  "Date that property is available on."
  :property/available-on)

(s/fdef available-on
        :args (s/cat :property td/entity?)
        :ret inst?)


(def accepting-tours?
  "Is this property currently accepting tours?"
  :property/tours)

(s/fdef accepting-tours?
        :args (s/cat :property td/entity?)
        :ret boolean?)


;; TODO: Schema
(defn llc [property]
  (get {"52gilbert"   "52 Gilbert LLC"
        "2072mission" "2072-2074 Mission LLC"}
       (code property)))


;; =============================================================================
;; Lookups
;; =============================================================================


(defn by-internal-name
  "Look up a property by its internal name."
  [db internal-name]
  (d/entity db [:property/internal-name internal-name]))

(s/fdef by-internal-name
        :args (s/cat :db td/db? :internal-name string?)
        :ret td/entity?)


(def by-code
  "Alias for `property/by-internal-name`."
  by-internal-name)


;; =============================================================================
;; Queries
;; =============================================================================


(defn occupied-units
  "Produce all units that are currently occupied."
  [db property]
  (filter (partial unit/occupied? db) (units property)))

(s/fdef occupied-units
        :args (s/cat :db td/db? :property td/entity?)
        :ret (s/* td/entity?))


(defn available-units
  "Produces all available units in `property`.

  (A unit is considered available if there is no active member license that
  references it.)"
  [db property]
  (remove (partial unit/occupied? db) (units property)))

(s/fdef available-units
        :args (s/cat :db td/db? :property td/entity?)
        :ret (s/* td/entity?))


(defn total-rent
  "The total rent that can be collected from the current active member
  licenses."
  [db property]
  (->> (d/q '[:find ?m (sum ?rate)
              :in $ ?p
              :where
              [?p :property/units ?u]
              [?m :member-license/unit ?u]
              [?m :member-license/status :member-license.status/active]
              [?m :member-license/rate ?rate]]
            db (:db/id property))
       (map second)
       (reduce + 0)))

(s/fdef total-rent
        :args (s/cat :db td/db? :property td/entity?)
        :ret number?)


(defn- amount-query
  [db property date status]
  (->> (d/q '[:find ?py ?amount
              :in $ ?p ?now ?status
              :where
              [?p :property/units ?u]
              [?m :member-license/unit ?u]
              [?m :member-license/status :member-license.status/active]
              [?m :member-license/rent-payments ?py]
              [?py :rent-payment/amount ?amount]
              [?py :rent-payment/status ?status]
              [?py :rent-payment/period-start ?start]
              [?py :rent-payment/period-end ?end]
              [(.after ^java.util.Date ?end ?now)]
              [(.before ^java.util.Date ?start ?now)]]
            db (:db/id property) date status)
       (reduce #(+ %1 (second %2)) 0)))


(defn amount-paid
  "The amount in dollars that has been collected in `property` for the month
  present within `date`."
  [db property date]
  (amount-query db property date :rent-payment.status/paid))


(defn amount-due
  "The amount in dollars that is still due in `property` for the month present
  within `date`."
  [db property date]
  (amount-query db property date :rent-payment.status/due))


(defn amount-pending
  "The amount in dollars that is pending in `property` for the month present
  within `date`."
  [db property date]
  (amount-query db property date :rent-payment.status/pending))


;; ==============================================================================
;; transactions =================================================================
;; ==============================================================================


(defn create-license-price
  [license price]
  {:license-price/license license
   :license-price/price   price})


(defn create-license-prices [lprices]
  (map
   (fn [{:keys [term price]}]
     (create-license-price term price))
   lprices))


(defn create
  "Create a new property"
  [name code units available-on license-prices & {:keys [cover-image-url address]}]
  (tb/assoc-when
   {:db/id                    (d/tempid :db.part/starcity)
    :property/name            name
    :property/code            code
    :property/units           units
    :property/available-on    available-on
    :property/licenses        license-prices}
   :property/cover-image-url cover-image-url
   :property/address         address))
