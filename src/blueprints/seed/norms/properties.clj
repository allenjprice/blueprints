(ns blueprints.seed.norms.properties
  (:require [datomic.api :as d]
            [toolbelt.core :as tb]))

;; =============================================================================
;; Property TX Generation Helpers
;; =============================================================================

(defn license [conn term]
  (d/q '[:find ?e .
         :in $ ?term
         :where [?e :license/term ?term]]
       (d/db conn) term))

(defn property-licenses [conn & ls]
  (map
   (fn [[term price]]
     {:property-license/license    (license conn term)
      :property-license/base-price price})
   ls))

(defn address [lines]
  {:address/lines lines
   :address/city  "San Francisco"})

(defn units [property-name n]
  (for [i (range n)]
    {:unit/name (format "%s-%s" property-name (inc i))}))

(defn property
  [part name internal-name available-on address licenses units
   & {:keys [managed-account-id ops-fee tours]
      :or   {tours false}}]
  (tb/assoc-when
   {:db/id                  (d/tempid part)
    :property/name          name
    :property/internal-name internal-name
    :property/available-on  available-on
    :property/licenses      licenses
    :property/units         units
    :property/tours         tours}
   :property/managed-account-id managed-account-id
   :property/ops-fee ops-fee))

;; =============================================================================
;; Meat
;; =============================================================================

(defn ^{:added "1.5.0"} add-initial-properties
  "NOTE: Properties were added to production long before 1.5.0. This is to
  provide compatibility with `blueprints.core/conform-db`."
  [conn part]
  (let [licenses (partial property-licenses conn)]
    [(property part "West SoMa"
               "52gilbert"
               #inst "2016-12-01T00:00:00.000-00:00"
               (address "52 Gilbert St.")
               (licenses [1 2300.0] [3 2300.0] [6 2100.0] [12 2000.0])
               (units "52gilbert" 6))
     (property part "The Mission"
               "2072mission"
               #inst "2017-04-15T00:00:00.000-00:00"
               (address "2072 Mission St.")
               (licenses [1 2400.0] [3 2400.0] [6 2200.0] [12 2100.0])
               (units "2072mission" 20))]))


(def ^{:added "1.8.0"} add-managed-ids
  [{:db/id                       [:property/internal-name "52gilbert"]
    :property/managed-account-id "acct_191838JDow24Tc1a"}
   {:db/id                       [:property/internal-name "2072mission"]
    :property/managed-account-id "acct_191838JDow24Tc1a"}])


(def ^{:added "1.10.0"} add-deposit-connect-ids
  [{:db/id                       [:property/code "52gilbert"]
    :property/deposit-connect-id "acct_191838JDow24Tc1a"}
   {:db/id                       [:property/code "2072mission"]
    :property/deposit-connect-id "acct_191838JDow24Tc1a"}])


(defn properties-present?
  "Are the properties to be seeded already present in the database? This is
  needed because the production properties arrived in the db prior to use of
  conformity."
  [conn]
  (let [db (d/db conn)]
    (and (d/entity db [:property/internal-name "52gilbert"])
         (d/entity db [:property/internal-name "2072mission"]))))


(defn norms [conn part]
  (merge
   {}
   (when-not (properties-present? conn)
     {:blueprints.seed/add-initial-properties
      {:txes [(add-initial-properties conn part)]}
      :blueprints.seed/add-cover-image-urls-09262017
      {:txes [[{:db/id                    [:property/internal-name "52gilbert"]
                :property/cover-image-url "/assets/images/52gilbert.jpg"}
               {:db/id                    [:property/internal-name "2072mission"]
                :property/cover-image-url "/assets/images/2072mission.jpg"}]]}
      :blueprints.seed/add-managed-ids
      {:txes     [add-managed-ids]
       :requires [:blueprints.seed/add-initial-properties]}
      :blueprints.seed/add-deposit-connect-ids
      {:txes     [add-deposit-connect-ids]
       :requires [:blueprints.seed/add-initial-properties]}})))
