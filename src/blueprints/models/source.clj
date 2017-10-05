(ns blueprints.models.source
  (:require [clojure.spec :as s]
            [toolbelt.predicates :as p]
            [datomic.api :as d]
            [toolbelt.datomic :as td]))


;; =============================================================================
;; Selectors
;; =============================================================================


(defn account [source]
  (:source/account source))

(s/fdef account
        :args (s/cat :source p/entity?)
        :ret (s/or :nothing nil? :entity p/entityd?))


;; =============================================================================
;; Transactions
;; =============================================================================


(defn create
  "Create a new source."
  [account]
  {:db/id          (d/tempid :db.part/tx)
   :source/account (td/id account)})

(s/fdef create
        :args (s/cat :account p/entity?)
        :ret map?)


;; =============================================================================
;; Queries
;; =============================================================================


(defn tx-account
  [db e a v]
  (:source/account (td/eav-tx db e a v)))
