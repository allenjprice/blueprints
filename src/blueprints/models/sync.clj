(ns blueprints.models.sync
  (:refer-clojure :exclude [ref])
  (:require [clojure.spec.alpha :as s]
            [datomic.api :as d]
            [toolbelt.datomic :as td]))

;; ==============================================================================
;; selectors ====================================================================
;; ==============================================================================


(defn ref
  "The entity being synced."
  [sync]
  (:sync/ref sync))

(s/fdef ref
        :args (s/cat :sync td/entity?)
        :ret td/entityd?)


(defn ext-id
  "The id of the external representation of the synced entity (`ref`)."
  [sync]
  (:sync/ext-id sync))

(s/fdef id
        :args (s/cat :sync td/entity?)
        :ret string?)


(defn last-synced
  "The date that the entity was last synced."
  [sync]
  (:sync/last-synced sync))

(s/fdef last-synced
        :args (s/cat :sync td/entity?)
        :ret inst?)


(defn service
  "The service that the entity is being synced with."
  [sync]
  (:sync/service sync))

(s/fdef service
        :args (s/cat :sync td/entity?)
        :ret keyword?)


;; ==============================================================================
;; transactions =================================================================
;; ==============================================================================


(defn create
  "Create a new `sync` entity given the entity (`ref`) to sync."
  [ref ext-id service]
  {:db/id            (d/tempid :db.part/starcity)
   :sync/ext-id      ext-id
   :sync/ref         (td/id ref)
   :sync/service     service
   :sync/last-synced (java.util.Date.)})

(s/fdef create
        :args (s/cat :ref td/entity?
                     :ext-id string?
                     :service keyword?)
        :ret map?)


(defn synced-now
  "Indicate that the `sync` entity has been synced as of now."
  [sync]
  {:db/id            (td/id sync)
   :sync/last-synced (java.util.Date.)})

(s/fdef synced-now
        :args (s/cat :sync td/entity?)
        :ret map?)


;; ==============================================================================
;; queries ======================================================================
;; ==============================================================================


(defn by-external-id
  "Look up a sync entity by `external-id`."
  [db external-id]
  (d/entity db [:sync/ext-id external-id]))

(s/fdef by-external-id
        :args (s/cat :db td/db? :external-id string?)
        :ret (s/or :nothing nil? :entity td/entityd?))


(defn by-entity
  "Look up a sync entity by entity being synced."
  [db entity]
  (->> (d/q '[:find ?e .
              :in $ ?ref
              :where
              [?e :sync/ref ?ref]]
            db (td/id entity))
       (d/entity db)))

(s/fdef by-entity
        :args (s/cat :db td/db? :entity td/entity?)
        :ret (s/or :nothing nil? :entity td/entityd?))
