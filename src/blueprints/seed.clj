(ns blueprints.seed
  "Certain feature releases require accompanying data in our database to work
  properly (seeding). This namespace provides a function `conform` that applies
  all unapplied seed operations."
  (:require [blueprints.seed.norms
             [catalogues :as catalogues]
             [licenses :as licenses]
             [properties :as properties]
             [services :as services]]
            [io.rkn.conformity :as c]))

;; =============================================================================
;; Phases
;; =============================================================================

;; Seeding must be carried out in "phases", as certain data requires the
;; presence of other data. This is unavoidable in a relational system.

;; An alternative would be to use datomic tempids more effectively.

(defn- phase-0 [conn part]
  (merge (licenses/norms conn part)))

(defn- phase-1 [conn part]
  (merge (properties/norms conn part)))

(defn- phase-2 [conn part]
  (merge (services/norms conn part)))

(defn- phase-3 [conn part]
  (merge (catalogues/norms conn part)))

;; =============================================================================
;; API
;; =============================================================================

(defn conform
  "Conform all seed data to the db over `conn`."
  [conn part]
  (c/ensure-conforms conn (phase-0 conn part))
  (c/ensure-conforms conn (phase-1 conn part))
  (c/ensure-conforms conn (phase-2 conn part))
  (c/ensure-conforms conn (phase-3 conn part)))
