(ns blueprints.models.application
  (:require [blueprints.models.event :as event]
            [clojure.spec.alpha :as s]
            [datomic.api :as d]
            [toolbelt.datomic :as td]))


;; =============================================================================
;; Spec
;; =============================================================================


(s/def ::status
  #{:application.status/in-progress
    :application.status/submitted
    :application.status/approved
    :application.status/rejected
    ;; Legacy
    :member-application.status/in-progress
    :member-application.status/submitted
    :member-application.status/approved
    :member-application.status/rejected})


;; =============================================================================
;; Selectors
;; =============================================================================


(def account (comp first :account/_member-application))

(def desired-license :application/license)

(def move-in-date :application/move-in)

(def communities :application/communities)

(def community-fitness :application/fitness)

(def address :application/address)

(def has-pet? :application/has-pet)

(def pet :application/pet)

(def completed-at :application/submitted-at)

(def status :application/status)


(defn last-modified-at
  [db application]
  (let [app-inst (td/updated-at db application)
        cf-inst  (td/updated-at db (community-fitness application))
        app-ms   (inst-ms app-inst)
        cf-ms    (inst-ms cf-inst)]
    (get
     {app-ms app-inst
      cf-ms  cf-inst}
    (max app-ms cf-ms))))


;; =============================================================================
;; Predicates
;; =============================================================================


(defn in-progress?
  "Has this application been submitted?"
  [app]
  (= :application.status/in-progress (status app)))

(s/fdef in-progress?
        :args (s/cat :application td/entity?)
        :ret boolean?)


(defn submitted?
  "Has this application been submitted?"
  [app]
  (= :application.status/submitted (status app)))

(s/fdef submitted?
        :args (s/cat :application td/entity?)
        :ret boolean?)


(defn approved?
  "Is this application approved?"
  [app]
  (= :application.status/approved (status app)))

(s/fdef approved?
        :args (s/cat :application td/entity?)
        :ret boolean?)


(defn rejected?
  "Is this application rejected?"
  [app]
  (= :application.status/rejected (status app)))

(s/fdef rejected?
        :args (s/cat :application td/entity?)
        :ret boolean?)


;; alias for convenience
(def completed? submitted?)

;; =============================================================================
;; Transactions
;; =============================================================================


(defn change-status
  "Change the status of this application."
  [app new-status]
  {:db/id              (:db/id app)
   :application/status new-status})

(s/fdef change-status
        :args (s/cat :application td/entity?
                     :status ::status)
        :ret map?)


(defn submit
  "Submit the member application."
  [application]
  [(change-status application :application.status/submitted)
   (event/job :application/submit {:params {:application-id (td/id application)}})])

(s/fdef submit
        :args (s/cat :application td/entity?)
        :ret vector?)


;; =============================================================================
;; Queries
;; =============================================================================


(defn by-account
  "Retrieve an application by account."
  [db account]
  (->> (d/q '[:find ?e .
              :in $ ?a
              :where
              [?a :account/application ?e]]
            db (:db/id account))
       (d/entity db)))

(s/fdef by-account
        :args (s/cat :db td/db? :account td/entity?)
        :ret (s/or :nothing nil? :entity td/entity?))


;; =============================================================================
;; Metrics
;; =============================================================================


(defn total-created
  "Produce the number of applications created between `pstart` and `pend`."
  [db pstart pend]
  (or (d/q '[:find (count ?e) .
             :in $ ?pstart ?pend
             :where
             [_ :account/application ?e ?tx]
             [?tx :db/txInstant ?created]
             [(.after ^java.util.Date ?created ?pstart)]
             [(.before ^java.util.Date ?created ?pend)]]
           db pstart pend)
      0))

(s/fdef total-created
        :args (s/cat :db td/db?
                     :period-start inst?
                     :period-end inst?)
        :ret integer?)
