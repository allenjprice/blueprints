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


(defn account
  "The account that owns this application."
  [application]
  (-> application :account/_member-application first))

(s/fdef account
        :args (s/cat :application td/entityd?)
        :ret td/entityd?)


(defn desired-license
  "The desired license term."
  [application]
  (:application/license application))

(s/fdef desired-license
        :args (s/cat :application td/entity?)
        :ret (s/nilable td/entityd?))


(defn move-in-date
  "The desired move in date."
  [application]
  (:application/move-in application))

(s/fdef move-in-date
        :args (s/cat :application td/entity?)
        :ret (s/nilable inst?))


(defn communities
  "The desired communities that this applicant would like to live in."
  [application]
  (:application/communities application))

(s/fdef communities
        :args (s/cat :application td/entity?)
        :ret (s/* td/entityd?))


(defn community-fitness
  "The community fitness questionnaire."
  [application]
  (:application/fitness application))

(s/fdef community-fitness
        :args (s/cat :application td/entity?)
        :ret (s/nilable td/entityd?))


(defn address
  "The current address of this applicant."
  [application]
  (:application/address application))

(s/fdef address
        :args (s/cat :application td/entity?)
        :ret (s/nilable td/entityd?))


(defn has-pet?
  "Does this applicant have a pet?"
  [application]
  (:application/has-pet application))

(s/fdef has-pet?
        :args (s/cat :application td/entity?)
        :ret (s/nilable boolean?))


(defn pet
  "Information about this applicant's pet."
  [application]
  (:application/pet application))

(s/fdef pet
        :args (s/cat :application td/entity?)
        :ret (s/nilable td/entityd?))


(defn completed-at
  [application]
  (:application/submitted-at application))

(s/fdef completed-at
        :args (s/cat :application td/entity?)
        :ret (s/nilable inst?))


(defn status
  "The status of this application."
  [application]
  (:application/status application))

(s/fdef status
        :args (s/cat :application td/entity?)
        :ret (s/nilable ::status))


(def ^:private community-fitness-labels
  {:fitness/skills
   "How will you contribute to the community?"
   :fitness/free-time
   "What do you like to do in your free time?"
   :fitness/dealbreakers
   "Do you have any dealbreakers?"
   :fitness/experience
   "Describe your past experience(s) living in shared spaces."
   :fitness/interested
   "Please tell the members why you want to join their community."
   :fitness/conflicts
   "Please describe how you would resolve a conflict between yourself and another member of the home."})


(s/def :fitness/label
  string?)

(s/def :fitness/key
  #{:fitness/skills :fitness/free-time :fitness/dealbreakers
    :fitness/experience :fitness/interested :fitness/conflicts})

(s/def :fitness/value
  (s/nilable string?))


(defn community-fitness-labeled
  "Produce a vector of community fitness answersw with their associated
  questions."
  [application]
  (let [cf (community-fitness application)]
    (reduce
     (fn [acc k]
       (conj acc {:label (get community-fitness-labels k)
                  :key   k
                  :value (get cf k)}))
     []
     [:fitness/experience :fitness/skills :fitness/free-time
      :fitness/dealbreakers :fitness/interested :fitness/conflicts])))

(s/fdef community-fitness-labeled
        :args (s/cat :application td/entity?)
        :ret (s/+ (s/keys :req-un [:fitness/label :fitness/key :fitness/value])))


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
        :ret (s/nilable td/entityd?))


(defn last-modified-at
  "The date that this application was last modified at."
  [db application]
  (let [app-inst (td/updated-at db application)
        cf-inst  (when-let [cf (community-fitness application)]
                   (td/updated-at db cf))
        app-ms   (inst-ms app-inst)
        cf-ms    (when (some? cf-inst) (inst-ms cf-inst))]
    (cond
      (= app-ms cf-ms) app-inst

      (nil? cf-ms) app-inst

      :otherwise
      (get
       {app-ms app-inst
        cf-ms  cf-inst}
       (if (nil? cf-ms)
         app-ms
         (max app-ms cf-ms))))))

(s/fdef last-modified-at
        :args (s/cat :db td/db? :application td/entity?)
        :ret inst?)


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
