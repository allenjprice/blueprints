(ns blueprints.models.onboard
  "The `onboard` entity tracks an account's progress through our onboarding flow
  in cases where the progress cannot be preserved through other entities.

  The onboarding flow is where prospective members pay their security deposit,
  possibly sign up for premium services, and provide other preliminary
  information required to join the community."
  (:require [datomic.api :as d]
            [clojure.spec :as s]
            [toolbelt.predicates :as p]))


;; =============================================================================
;; Spec
;; =============================================================================


(s/def ::sel-args (s/cat :onboard p/entity?))


;; =============================================================================
;; Selectors
;; =============================================================================


(def account
  "The `account` associated with this `onboard`."
  :onboard/account)

(s/fdef account
        :args ::sel-args
        :ret p/entity?)


(def move-in
  "The date that this account will be moving in."
  :onboard/move-in)

(s/fdef move-in
        :args ::sel-args
        :ret (s/or :inst inst? :nothing nil?))


;; =============================================================================
;; Predicates
;; =============================================================================


(defn seen? [k]
  (fn [onboard]
    (contains? (:onboard/seen onboard) k)))


(def seen-moving?
  "Have moving options been seen?"
  (seen? :services/moving))

(s/fdef seen-moving?
        :args ::sel-args
        :ret boolean?)


(def seen-storage?
  "Have storage options been seen?"
  (seen? :services/storage))

(s/fdef seen-storage?
        :args ::sel-args
        :ret boolean?)


(def seen-customize?
  "Have room customization options been seen?"
  (seen? :services/customize))

(s/fdef seen-customize?
        :args ::sel-args
        :ret boolean?)


(def seen-cleaning?
  "Have cleaning and laundry services been seen?"
  (seen? :services/cleaning))

(s/fdef seen-cleaning?
        :args ::sel-args
        :ret boolean?)


(def seen-upgrades?
  "Have upgrade options been seen?"
  (seen? :services/upgrades))

(s/fdef seen-upgrades?
        :args ::sel-args
        :ret boolean?)


;; =============================================================================
;; Transactions
;; =============================================================================


(defn create
  "Create a new `onboard` entity given an `account`."
  [account]
  {:db/id           (d/tempid :db.part/starcity)
   :onboard/account (:db/id account)})

(s/fdef create
        :args (s/cat :account p/entity?)
        :ret map?)


(defn add-seen
  [onboard k]
  {:db/id        (:db/id onboard)
   :onboard/seen [k]})

(s/fdef add-seen
        :args (s/cat :onboard p/entity? :k keyword?)
        :ret map?)


(defn remove-seen
  [onboard k]
  [:db/retract (:db/id onboard) :onboard/seen k])

(s/fdef add-seen
        :args (s/cat :onboard p/entity? :k keyword?)
        :ret vector?)


;; =============================================================================
;; Queries
;; =============================================================================


(def by-account
  "Look up the `onboard` entity by `account`."
  (comp first :onboard/_account))

(s/fdef by-account
        :args (s/cat :account p/entity?)
        :ret p/entity?)
