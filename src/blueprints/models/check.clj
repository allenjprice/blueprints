(ns blueprints.models.check
  (:refer-clojure :exclude [update])
  (:require [clojure.spec :as s]
            [datomic.api :as d]
            [toolbelt
             [core :as tb]
             [predicates :as p]]))


;; =============================================================================
;; Selectors
;; =============================================================================

(def received :check.status/received)
(def cleared :check.status/cleared)
(def ^{:deprecated "1.10.0"} cancelled :check.status/canceled)
(def canceled :check.status/canceled)
(def bounced :check.status/bounced)
(def deposited :check.status/deposited)


(def statuses
  "All available statuses that a check may have."
  #{received cleared canceled bounced deposited})


(def amount
  "The dollar amount on this check."
  :check/amount)

(s/fdef amount
        :args (s/cat :check p/entity?)
        :ret float?)


(def status
  "The status of this check."
  :check/status)

(s/fdef status
        :args (s/cat :check p/entity?)
        :ret statuses)


(def received-on
  "Date that this check was received."
  :check/received-on)

(s/fdef received-on
        :args (s/cat :check p/entity?)
        :ret inst?)


(defn ^{:deprecated "1.10.0"} security-deposit
  "Produce the security deposit that references this `check`, if any."
  [check]
  (:security-deposit/_checks check))

(s/fdef security-deposit
        :args (s/cat :check p/entity?)
        :ret (s/or :nothing nil? :deposit p/entity?))


(defn ^{:deprecated "1.12.0"} rent-payment
  "Produce the rent payment that references this `check`, if any."
  [check]
  (:rent-payment/_check check))

(s/fdef rent-payment
        :args (s/cat :check p/entityd?)
        :ret (s/or :nothing nil? :payment p/entityd?))


(defn payment
  "The payment that this check is part of."
  [check]
  (:payment/_check check))

(s/fdef payment
        :args (s/cat :check p/entityd?)
        :ret p/entityd?)


;; =============================================================================
;; Specs
;; =============================================================================


(s/def :check/name string?)
(s/def :check/amount float?)
(s/def :check/date inst?)
(s/def :check/number int?)
(s/def :check/status #{received cleared cancelled bounced deposited})
(s/def :check/received-on inst?)
(s/def :check/bank string?)
(s/def ::check
  (s/keys :req [:check/name :check/amount :check/date :check/number :check/status]
          :opt [:check/received-on :check/bank :db/id]))


;; =============================================================================
;; Predicates
;; =============================================================================


(defn updated? [c]
  (s/valid? ::updated-check c))


(defn check? [c]
  (s/valid? ::check c))


;; =============================================================================
;; Transactions
;; =============================================================================


(defn create
  "Produce the tx-data required to create a `check` entity."
  [name amount date number & {:keys [status received-on bank]}]
  (tb/assoc-when
   {:db/id        (d/tempid :db.part/starcity)
    :check/name   name
    :check/amount amount
    :check/date   date
    :check/number number
    :check/status (or status received)}
   :check/received-on received-on
   :check/bank bank))

(s/fdef create
        :args (s/cat :name :check/name
                     :amount :check/amount
                     :date :check/date
                     :number :check/number
                     :opts (s/keys* :opt-un [:check/status :check/received-on :check/bank]))
        :ret check?)


(s/def ::updated-check
  (s/keys :req [:db/id]
          :opt [:check/name
                :check/amount
                :check/date
                :check/number
                :check/status
                :check/received-on
                :check/bank]))


(defn update
  "Produce the tx-data required to update a `check` entity."
  [check {:keys [amount name number status date received-on bank]}]
  (tb/assoc-when
   {:db/id (:db/id check)}
   :check/amount amount
   :check/bank bank
   :check/name   name
   :check/number number
   :check/status status
   :check/date date
   :check/received-on received-on))

(s/fdef update
        :args (s/cat :check p/entity?
                     :updates (s/keys :opt-un [:check/name
                                               :check/amount
                                               :check/date
                                               :check/number
                                               :check/status
                                               :check/received-on
                                               :check/bank]))
        :ret ::updated-check)
