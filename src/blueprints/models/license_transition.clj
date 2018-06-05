(ns blueprints.models.license-transition
  (:refer-clojure :exclude [type])
  (:require [blueprints.models.member-license :as member-license]
            [clojure.spec.alpha :as s]
            [datomic.api :as d]
            [toolbelt.datomic :as td]
            [toolbelt.core :as tb]))


;; ==============================================================================
;; Selectors ====================================================================
;; ==============================================================================


(defn type
  "The type of transition (move-out, renewal, etc)."
  [transition]
  (:license-transition/type transition))


(defn uuid
  "The UUID for this transition."
  [transition]
  (:license-transition/uuid transition))


(defn date
  "The date at which this transition takes effect (move-out day or renewal day, for instance)."
  [transition]
  (:license-transition/date transition))


(defn current-license
  "The license from which the member is transitioning away."
  [transition]
  (:license-transition/current-license transition))


(defn new-license
  "The license that the member is transitioning to."
  [transition]
  (:license-transition/new-license transition))


;; ==============================================================================
;; Queries ======================================================================
;; ==============================================================================


(defn by-license
  [db license]
  (->> (d/q '[:find ?e .
              :in $ ?id
              :where [?e :license-transition/current-license ?id]]
            db (td/id license))
       (d/entity db)))

(s/fdef by-license
        :args (s/cat :db td/db? :license td/entity?)
        :ret td/entityd?)


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


;; ==============================================================================
;; Transactions =================================================================
;; ==============================================================================

(defn create
  [license type date asana-task deposit-refund]
  (tb/assoc-when
   {:db/id                              (d/tempid :db.part/starcity)
    :license-transition/current-license (td/id license)
    :license-transition/type            (keyword "license-transition.type" (name type))
    :license-transition/date            date
    :license-transition/uuid            (d/squuid)}
   :license-transition/deposit-refund (when-let [dr deposit-refund] (float dr))
   :asana/task asana-task))


(defn edit
  [id date deposit-refund room-walkthrough-doc asana-task]
  (tb/assoc-when
   {:db/id id}
   :license-transition/date date
   :license-transition/deposit-refund deposit-refund
   :license-transition/room-walkthrough-doc room-walkthrough-doc
   :asana/task asana-task))
