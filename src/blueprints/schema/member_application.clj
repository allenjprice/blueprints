(ns blueprints.schema.member-application
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]))

(def ^{:added "1.0.0"} schema
  (s/generate-schema
   [(s/schema
     member-application
     (s/fields
      [desired-properties :ref :many
       "Properties that applicant is interested in."]

      [desired-license :ref :component
       "License that this applicant wants."]

      [desired-availability :instant
       "Date that applicant would like to move in."]

      [pet :ref :component]

      [community-fitness :ref :component
       "The community fitness questionnaire."]

      [current-address :ref :component
       "Applicant's current address."]

      [locked :boolean
       "Indicates whether or not the application is locked for edits."]

      [approved :boolean
       "Indicates whether or not the application has been approved or not by an administrator."]

      [submitted-at :instant
       "The time at which the application was submitted."]))]))

(def ^{:added "1.0.0"} pet
  (s/generate-schema
   [(s/schema
     pet
     (s/fields
      [type :keyword "The type of pet."]
      [breed :string "The pet's breed."]
      [weight :long "The weight of the pet."]))]))

(def ^{:added "1.0.0"} community-fitness
  (s/generate-schema
   [(s/schema
     community-fitness
     (s/fields
      [prior-community-housing :string :fulltext
       "Response to: 'Have you ever lived in community housing?'"]

      [skills :string :fulltext
       "Response to: 'What skills or traits do you hope to share with the community?'"]

      [why-interested :string :fulltext
       "Response to: 'Why are you interested in Starcity?'"]

      [free-time :string :fulltext
       "Response to: 'How do you spend your free time'"]

      [dealbreakers :string :fulltext
       "Response to: 'Do you have an dealbreakers?'"]))]))

(def ^{:added "1.0.x"} add-has-pet
  (s/generate-schema
   [(s/schema
     member-application
     (s/fields
      [has-pet :boolean
       "Whether or not applicant has a pet."]))]))

(defn- statuses [part]
  [{:db/id    (d/tempid part)
    :db/ident :member-application.status/in-progress}
   {:db/id    (d/tempid part)
    :db/ident :member-application.status/submitted}
   {:db/id    (d/tempid part)
    :db/ident :member-application.status/approved}
   {:db/id    (d/tempid part)
    :db/ident :member-application.status/rejected}])

(defn- ^{:added "1.1.3"} add-status [part]
  (->> (s/generate-schema
        [(s/schema
          member-application
          (s/fields
           [status :ref
            "The status of this member's application."]))])
       (concat (statuses part))))

(def ^{:added "1.1.4"} improvements
  "A variety of improvements to the application schema that add indices to all
  attributes, improve naming, and fix some mistakes."
  [{:db/id               :member-application/desired-properties
    :db/ident            :member-application/properties
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :member-application/desired-license
    :db/ident            :member-application/license
    :db/index            true
    :db/isComponent      false
    :db.alter/_attribute :db.part/db}
   {:db/id               :member-application/desired-availability
    :db/ident            :member-application/move-in
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id  :member-application/locked
    :db/doc "DEPRECATED in favor of :member-application/status, 11/20/16"}
   {:db/id  :member-application/approved
    :db/doc "DEPRECATED in favor of :member-application/status, 11/20/16"}
   {:db/id  :member-application/submitted-at
    :db/doc "DEPRECATED 11/20/16"}])

(defn- rename-attr [[from to]]
  {:db/id               from
   :db/ident            to
   :db.alter/_attribute :db.part/db})

(defn- rename-attrs [& pairs]
  (assert (even? (count pairs)))
  (mapv rename-attr (partition 2 pairs)))

(def ^{:added "1.3.0"} rename-member-application
  (rename-attrs
   :member-application/desired-properties :application/communities
   :member-application/desired-license :application/license
   :member-application/move-in :application/move-in
   :member-application/pet :application/pet
   :member-application/community-fitness :application/fitness
   :member-application/current-address :application/address
   :member-application/has-pet :application/has-pet
   :member-application/status :application/status))

(def ^{:added "1.3.0"} rename-statuses
  [{:db/id    :member-application.status/approved
    :db/ident :application.status/approved}
   {:db/id    :member-application.status/in-progress
    :db/ident :application.status/in-progress}
   {:db/id    :member-application.status/rejected
    :db/ident :application.status/rejected}
   {:db/id    :member-application.status/submitted
    :db/ident :application.status/submitted}])

(def ^{:added "1.3.0"} rename-community-fitness
  (rename-attrs
   :community-fitness/prior-community-housing :fitness/experience
   :community-fitness/skills :fitness/skills
   :community-fitness/free-time :fitness/free-time
   :community-fitness/why-interested :fitness/interested
   :community-fitness/dealbreakers :fitness/dealbreakers))

(def ^{:added "1.6.0"} add-conflicts-to-fitness
  (s/generate-schema
   [(s/schema
     fitness
     (s/fields
      [conflicts :string :fulltext
       "How is applicant at resolving conflicts?"]))]))

(defn- ^{:added "1.6.0"} add-application-submitted [part]
  [{:db/id    (d/tempid part)
    :db/ident :db.application/submit
    :db/doc   "Submit a new member application."
    :db/fn
    (datomic.function/construct
     {:lang     "clojure"
      :params   '[db application-id]
      :requires '[[datomic.api :as d]]
      :code     '[{:db/id              application-id
                   :application/status :application.status/submitted}
                  [:db.cmd/create :application/submit {:data {:application-id application-id}}]
                  [:db.msg/create :application/submitted {:application-id application-id}]]})}])

(def ^{:added "1.7.1"} add-pet-attrs-06132017
  (s/generate-schema
   [(s/schema
     pet
     (s/fields
      [sterile :boolean
       "Has the pet been sterilized (spayed/neutered)?"]
      [vaccines :boolean
       "Are the pet's vaccines, licenses and inoculations current?"]
      [bitten :boolean
       "Has the pet ever bitten a human?"]
      [demeanor :string
       "Description of the pet's demeanor."]
      [daytime-care :string
       "Description of how the pet will be taken care of during the day."]))]))


(defn- ^{:added "1.8.0"} change-application-submitted-to-events [part]
  [{:db/id    (d/tempid part)
    :db/ident :db.application/submit
    :db/doc   "Submit a new member application."
    :db/fn
    (datomic.function/construct
     {:lang     "clojure"
      :params   '[db application-id]
      :requires '[[datomic.api :as d]]
      :code     '[{:db/id              application-id
                   :application/status :application.status/submitted}
                  [:db.event/create :application/submit {:params {:application-id application-id}}]]})}])


(defn norms [part]
  {:starcity/add-member-application-schema
   {:txes [schema]}

   :starcity/add-community-fitness-schema
   {:txes [community-fitness]}

   :starcity/add-pet-schema
   {:txes [pet]}

   :schema/add-has-pet-attr-10-3-16
   {:txes [add-has-pet]}

   :schema/add-member-application-status-11-15-16
   {:txes [(add-status part)]}

   :schema.member-application/improvements-11-20-16
   {:txes     [improvements]
    :requires [:starcity/add-member-application-schema
               :schema/add-has-pet-attr-10-3-16
               :schema/add-member-application-status-11-15-16]}

   :schema.member-application/naming-improvements-020417
   {:txes     [rename-community-fitness
               rename-member-application
               rename-statuses]
    :requires [:starcity/add-member-application-schema
               :starcity/add-community-fitness-schema
               :schema/add-has-pet-attr-10-3-16
               :schema/add-member-application-status-11-15-16
               :schema.member-application/improvements-11-20-16]}

   :schema.member-application/add-conflicts-to-fitness-05182017
   {:txes [add-conflicts-to-fitness]}

   :schema.member-application/add-application-submitted-fn-05252017
   {:txes [(add-application-submitted part)]}

   :schema.member-application.pets/add-attrs-06132017
   {:txes [add-pet-attrs-06132017]}

   :schema.member-application/change-application-submitted-fn-06192017
   {:txes [(change-application-submitted-to-events part)]}})
