(ns blueprints.migrations.member-application
  (:require [datomic.api :as d]))

;; =============================================================================
;; :member-application/has-pet
;; =============================================================================

(defn- add-has-pet
  "Find member applications that have pets and member applications that do not
  have pets, and set `:member-application/has-pet` accordingly."
  {:added "1.0.x"}
  [conn]
  (let [with    (d/q '[:find [?ma ...]
                       :where
                       [?ma :member-application/pet _]]
                     (d/db conn))
        without (d/q '[:find [?ma ...]
                       :where
                       [_ :account/member-application ?ma]
                       [(missing? $ ?ma :member-application/pet)]]
                     (d/db conn))]
    (vec
     (concat
      (map (fn [e] [:db/add e :member-application/has-pet true]) with)
      (map (fn [e] [:db/add e :member-application/has-pet false]) without)))))

;; =============================================================================
;; :member-application/status
;; =============================================================================

(defn- in-progress
  "Find all applications that are not locked and not approved."
  [conn]
  (d/q '[:find [?e ...]
         :where
         [_ :account/member-application ?e]
         (or [?e :member-application/locked false]
             [(missing? $ ?e :member-application/locked)])
         (not [?e :member-application/approved true])]
       (d/db conn)))

(defn- submitted
  "Find all applications that are not approved and are locked."
  [conn]
  (d/q '[:find [?e ...]
         :where
         [?e :member-application/locked true]
         (not [?e :member-application/approved true])]
       (d/db conn)))

(defn- approved
  "Find all applications that are approved."
  [conn]
  (d/q '[:find [?e ...]
         :where
         [?e :member-application/approved true]]
       (d/db conn)))

(defn- add-member-application-statuses
  "Add the member application statuses to applications that didn't have them
  prior to creation of the `:member-application/status` attribute."
  {:added "1.1.3"}
  [conn]
  (concat
   (map (fn [e] [:db/add e :member-application/status :member-application.status/in-progress]) (in-progress conn))
   (map (fn [e] [:db/add e :member-application/status :member-application.status/submitted]) (submitted conn))
   (map (fn [e] [:db/add e :member-application/status :member-application.status/approved]) (approved conn))))

;; =============================================================================
;; Norms
;; =============================================================================

(defn norms [conn]
  {:seed/seed-has-pet-10-3-16 ; legacy naming
   {:txes [(add-has-pet conn)]}

   :seed/seed-member-application-statuses-11-15-16
   {:txes [(add-member-application-statuses conn)]}})
