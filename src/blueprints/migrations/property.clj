(ns blueprints.migrations.property
  (:require [datomic.api :as d]
            [toolbelt.core :as tb]))


;; =============================================================================
;; add ops fees
;; =============================================================================


(defn- ^{:added "1.2.0"} add-ops-fees
  "Add the Stripe account identifiers of the managed accounts we've created in
  our test environment to the properties they're associated with in our system."
  [_]
  [{:db/id            [:property/internal-name "52gilbert"]
    :property/ops-fee 30.0}
   {:db/id            [:property/internal-name "2072mission"]
    :property/ops-fee 30.0}])


(defn- units-by-property-name [conn internal-name]
  (d/q '[:find [?e ...]
         :in $ ?p
         :where
         [?p :property/units ?e]]
       (d/db conn) [:property/internal-name internal-name]))


(defn- name-units [conn internal-name]
  (->> (units-by-property-name conn internal-name)
       (map-indexed
        (fn [i e]
          [:db/add e :unit/name (format "%s-%s" internal-name (inc i))]))))


(defn- ^{:added "1.2.0"} add-unit-names [conn]
  (concat (name-units conn "52gilbert")
          (name-units conn "2072mission")))


;; =============================================================================
;; add tours
;; =============================================================================


(def ^{:added "1.4.1"} add-tours
  "For our new Schedule Tour page to work, each property needs to have a value
  set for `:property/tours`, which indicates whether or not the property is
  currently accepting tours. This is a flag that will be manipulated
  administratively from our dashboard."
  [{:db/id          [:property/internal-name "52gilbert"]
    :property/tours false}
   {:db/id          [:property/internal-name "2072mission"]
    :property/tours true}])


;; =============================================================================
;; add payment properties
;; =============================================================================


(defn ^{:added "1.17.0"} add-payment-properties [conn]
  (->> (d/q '[:find ?py ?pr
              :where
              [?py :payment/account ?a]
              [?a :account/licenses ?l]
              [?l :member-license/unit ?u]
              [?pr :property/units ?u]]
            (d/db conn))
       (tb/distinct-by first)
       (mapv
        (fn [[py pr]]
          [:db/add py :payment/property pr]))))


;; =============================================================================
;; add orders ops fees
;; =============================================================================


(def ^{:added "1.17.0"} add-orders-ops-fees
  [{:db/id                   [:property/internal-name "52gilbert"]
    :property/ops-fee-orders 100.0}
   {:db/id                   [:property/internal-name "2072mission"]
    :property/ops-fee-orders 0.0}])


(defn norms [conn]
  {:migration.property/add-ops-fees-12-15-16
   {:txes [(add-ops-fees conn)]}

   :migration.property.unit/add-unit-names-1-13-17
   {:txes [(add-unit-names conn)]}

   :migration.property/add-tours-03232017
   {:txes [add-tours]}

   :migration.property/add-payment-property-11262017
   {:txes [(add-payment-properties conn)]}

   :migration.property/add-orders-ops-fees
   {:txes [add-orders-ops-fees]}})
