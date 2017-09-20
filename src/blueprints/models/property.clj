(ns blueprints.models.property
  (:refer-clojure :exclude [name])
  (:require [blueprints.models.unit :as unit]
            [clj-time.core :as t]
            [clojure.spec :as s]
            [datomic.api :as d]
            [toolbelt.predicates :as p]))

;; =============================================================================
;; Selectors
;; =============================================================================


(def name
  "The name of this property."
  :property/name)

(s/fdef name
        :args (s/cat :property p/entity?)
        :ret string?)


(def internal-name
  "The internal (name) of this property. Can be used as a lookup."
  :property/code)

(s/fdef internal-name
        :args (s/cat :property p/entity?)
        :ret string?)


(def code
  "Alias to `property/internal-name`"
  internal-name)


(def ^{:added "1.11.0"} cover-image-url
  "URL for the image representing this property in our UI."
  :property/cover-image-url)

(s/fdef cover-image-url
        :args (s/cat :property p/entity?)
        :ret string?)


(def ^{:deprecated "1.10.0"} managed-account-id
  "DEPRECATED: Use `property/rent-connect-id` instead."
  :property/managed-account-id)


(def ^{:added "1.10.0"} rent-connect-id
  "The id for the account to route rent payments to."
  :property/rent-connect-id)

(s/fdef rent-connect-id
        :args (s/cat :property p/entity?)
        :ret string?)


(def ^{:added "1.10.0"} deposit-connect-id
  "The id for the account to route security deposit payments to."
  :property/deposit-connect-id)

(s/fdef deposit-connect-id
        :args (s/cat :property p/entity?)
        :ret string?)


(def ops-fee
  "The percentage of rent payments to route to the parent Stripe account."
  :property/ops-fee)

(s/fdef ops-fee
        :args (s/cat :property p/entity?)
        :ret float?)


(def units
  "Units in this property."
  :property/units)

(s/fdef units
        :args (s/cat :property p/entity?)
        :ret (s/+ p/entityd?))


;; TODO: Actually store this in the DB.
(defn time-zone
  "Produce the time zone that `property` is in."
  [property]
  (t/time-zone-for-id "America/Los_Angeles"))


(def available-on
  "Date that property is available on."
  :property/available-on)

(s/fdef available-on
        :args (s/cat :property p/entity?)
        :ret inst?)


(def accepting-tours?
  "Is this property currently accepting tours?"
  :property/tours)

(s/fdef accepting-tours?
        :args (s/cat :property p/entity?)
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
        :args (s/cat :db p/db? :internal-name string?)
        :ret p/entity?)


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
        :args (s/cat :db p/db? :property p/entity?)
        :ret (s/* p/entity?))


(defn available-units
  "Produces all available units in `property`.

  (A unit is considered available if there is no active member license that
  references it.)"
  [db property]
  (remove (partial unit/occupied? db) (units property)))

(s/fdef available-units
        :args (s/cat :db p/db? :property p/entity?)
        :ret (s/* p/entity?))


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
        :args (s/cat :db p/db? :property p/entity?)
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
