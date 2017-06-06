(ns blueprints.models.referral
  (:refer-clojure :exclude [apply])
  (:require [clojure.spec :as s]
            [plumbing.core :as plumbing]
            [toolbelt.predicates :as p]
            [datomic.api :as d]))


;; =============================================================================
;; Constants
;; =============================================================================


(def sources
  "Referrals sources that are available for selection."
  ["word of mouth"
   "facebook"
   "instagram"
   "starcity member"
   "craigslist"
   "video"
   "search"
   "employer"
   "news"
   "other"])


;; =============================================================================
;; Specs
;; =============================================================================


(s/def ::from
  #{:referral.from/apply
    :referral.from/tour})

(s/def ::referral
  (s/keys :req [:referral/source
                :referral/from]
          :opt [:db/id
                :referral/account
                :referral/tour-for]))


;; =============================================================================
;; Transactions
;; =============================================================================


(defn create
  "Create a referral."
  [from source & [account]]
  (plumbing/assoc-when
   {:db/id           (d/tempid :db.part/starcity)
    :referral/source source
    :referral/from   from}
   :referral/account (:db/id account)))

(s/fdef create
        :args (s/cat :from ::from
                     :source string?
                     :account (s/? p/entity?)))


(defn tour
  "Create a tour referral."
  [source property & [account]]
  (let [tx (create :referral.from/tour source account)]
    (assoc tx :referral/tour-for (:db/id property))))

(s/fdef tour
        :args (s/cat :source string?
                     :property p/entity?
                     :account (s/? p/entity?))
        :ret ::referral)


(defn apply
  "Create an apply referral."
  [source account]
  (create :referral.from/apply source account))

(s/fdef tour
        :args (s/cat :source string?
                     :account p/entity?)
        :ret ::referral)
