(ns blueprints.models.check
  (:refer-clojure :exclude [update])
  (:require [clojure.spec :as s]
            [toolbelt
             [core :as tb]
             [predicates :as p]]))


;; =============================================================================
;; Selectors
;; =============================================================================

;;; Statuses

(def received :check.status/received)
(def cleared :check.status/cleared)
(def cancelled :check.status/cancelled)
(def bounced :check.status/bounced)
(def deposited :check.status/deposited)


(def statuses
  "All available statuses that a check may have."
  #{received cleared cancelled bounced deposited})


(def amount
  :check/amount)


(def status
  :check/status)


(def received-on
  :check/received-on)


(defn security-deposit
  "Produce the security deposit that references this `check`, if any."
  [check]
  (:security-deposit/_checks check))

(s/fdef security-deposit
        :args (s/cat :check p/entity?)
        :ret (s/or :nothing nil? :deposit p/entity?))


(defn rent-payment
  "Produce the rent payment that references this `check`, if any."
  [check]
  (:rent-payment/_check check))

(s/fdef rent-payment
        :args (s/cat :check p/entity?)
        :ret (s/or :nothing nil? :payment p/entity?))


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
   {:check/name   name
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
