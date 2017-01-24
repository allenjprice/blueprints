(ns starcity-db.schema.member-application
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
      ;; TODO: enum?
      [type :keyword "The type of pet."]
      [breed :string "The pet's breed."]
      [weight :long "The weight of the pet."]))]))

(def ^{:added "1.0.0"} community-fitness
  (s/generate-schema
   [(s/schema
     community-fitness
     (s/fields
      ;; TODO: rename to `experience`
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

   :schema.account/improvements-11-20-16
   {:txes     [improvements]
    :requires [:starcity/add-member-application-schema
               :schema/add-has-pet-attr-10-3-16
               :schema/add-member-application-status-11-15-16]}})
