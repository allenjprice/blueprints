(ns blueprints.models.license-transition
  (:refer-clojure :exclude [type])
  (:require [blueprints.models.member-license :as member-license]
            [clojure.spec.alpha :as s]
            [datomic.api :as d]
            [toolbelt.datomic :as td]
            [toolbelt.core :as tb]))

;; NOTE - the schema definitions for `license-transition`s can be found in the `blueprints.schema.member-license`

(comment
  @(d/q
    '[:find ?a
      :in [$ ?license-id]
      :where
      [?a :account/licenses ?license-id]]
    db 285873023223127)

  )


;; ==============================================================================
;; Selectors ====================================================================
;; ==============================================================================

;;TODO - write some great selectors
(defn type
  [transition]
  (:license-transition/type transition))


(defn uuid
  [transition]
  (:license-transition/uuid transition))


(defn date
  [transition]
  (:license-transition/date transition))


(defn current-license
  [transition]
  (:license-transition/current-license transition))


;; ==============================================================================
;; Predicates ===================================================================
;; ==============================================================================

;; TODO - maybe you need some predicates?



;; ==============================================================================
;; Queries ======================================================================
;; ==============================================================================

;;TODO - write some helpful queries

(defn by-license-id
  [db license-id]
  (->> (d/q '[:find ?e .
              :in $ ?id
              :where [?e :license-transition/current-license ?id]]
            db license-id)
       (d/entity db)))


(defn by-uuid
  [db uuid]
  (d/entity db [:license-transition/uuid uuid]))


(defn by-type
  [db transition-type]
  (->> (d/q '[:find [?e ...]
              :in $ ?pending
              :where [?e :license-transition/type ?pending]]
            db (keyword "license-transition.type" (name transition-type)))
       (map (partial d/entity db))))


(comment

  (d/q '[:find [?e ...]
         :in $
         :where [?e :license-transition/type _]]
       db)


  )


;; ==============================================================================
;; Transactions =================================================================
;; ==============================================================================

(defn create
  [license type date deposit-refund]
  (tb/assoc-when
   {:db/id                              (d/tempid :db.part/starcity)
    :license-transition/current-license (td/id license)
    :license-transition/type            (keyword "license-transition.type" (name type))
    :license-transition/date            date
    :license-transition/uuid            (d/squuid)}
   :license-transition/deposit-refund (when-let [dr deposit-refund] (float dr))))
