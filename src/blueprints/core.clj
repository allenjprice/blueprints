(ns blueprints.core
  (:require [blueprints
             [migrations :as migrations]
             [schema :as schema]
             [seed :as seed]]
            [io.rkn.conformity :as c]))

(def default-partition :db.part/user)

;; =============================================================================
;; It would be nice to have a single function that provided the functionality of
;; `conform-schema` /and/ `conform-migrations` (e.g. `conform-db`); however,
;; this is not currently possible (without significant effort) due to the
;; historic way in which our database has evolved. I'm not going to elaborate on
;; why this is to myself at this time because the conclusion can be again
;; arrived upon with a small amount of effort.
;;
;; This issue should be fixed at a later date when I can devote some time to it.
;; Two potential solutions that come to mind are:
;;
;; 1. Create some dummy conformity entries to trick production/staging dbs into
;; thinking that it has been "properly" constructed, or (more robustly)
;;
;; 2. Create a brand new database without any of the cruft of the old one and
;; write migration logic for everything. This would be a sizeable amount of
;; effort and will be tricky to get right.
;; =============================================================================

(defn ^{:deprecated "1.5.0"} conform-schema
  "DEPRECATEED: prefer `conform-db`.

  Adds any schema changes to the db over `conn`."
  [conn & [part]]
  (schema/conform conn (or part default-partition)))

(defn ^{:deprecated "1.5.0"} conform-migrations
  "DEPRECATED: prefer `conform-db`.

  Applies all data migrations to the db over `conn`."
  [conn & [part]]
  (let [part (or part default-partition)]
    (c/ensure-conforms conn (migrations/norms conn part))))

(defn conform-db
  "Conform novelty to db over `conn`."
  [conn & [part]]
  (let [part (or part default-partition)]
    (schema/conform conn part)
    (seed/conform conn part)
    (migrations/conform conn part)))
