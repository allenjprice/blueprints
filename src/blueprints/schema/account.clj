(ns blueprints.schema.account
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]))


(def ^{:added "1.0.0"} schema
  (s/generate-schema
   [(s/schema
     account
     (s/fields
      [first-name :string :fulltext]
      [middle-name :string :fulltext]
      [last-name :string :fulltext]
      [phone-number :string]
      [email :string :unique-identity :fulltext]

      [password :string
       "User's hashed password."]

      [member-application :ref
       "The rental application associated with this account."]

      [unit :ref
       "The unit that the person identified by this account is living in."]

      [license :ref
       "The user's license."]

      [activation-hash :string
       "The user's activation hash, generated at the time of signup."]

      [activated :boolean
       "Becomes true after account activation."]

      [dob :instant
       "User's date of birth."]

      [role :enum]))]))

(defn- ^{:added "1.0.0"} roles [part]
  [{:db/id    (d/tempid part)
    :db/ident :account.role/applicant}
   {:db/id    (d/tempid part)
    :db/ident :account.role/tenant}
   {:db/id    (d/tempid part)
    :db/ident :account.role/admin}])

(defn- ^{:added "1.1.x"} add-role-pending [part]
  [{:db/id    (d/tempid part)
    :db/ident :account.role/pending}])

(def ^{:added "1.1.4"} add-indexes
  "Add indexes to attribues that do not have them."
  [{:db/id               :account/first-name
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/middle-name
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/last-name
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/phone-number
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/member-application
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/unit
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/license
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/activation-hash
    :db/index            true
    :db.alter/_attribute :db.part/db}])

(def ^{:added "1.1.4"} rename-roles
  "Our original language for roles (tenant/pending) is inaccurate -- rename them
  to something more like the language we actually use (member/onboarding)."
  [{:db/id    :account.role/tenant
    :db/ident :account.role/member}
   {:db/id    :account.role/pending
    :db/ident :account.role/onboarding}])

(def ^{:added "1.2.0"} license-alterations
  "Change the `:account/license` attribute to have cardinality many and be a
  component."
  [{:db/id               :account/license
    :db/cardinality      :db.cardinality/many
    :db/isComponent      true
    :db.alter/_attribute :db.part/db}])

(def ^{:added "1.3.0"} rename-license-to-licenses
  "Since we changed `:account/license` to have cardinality many, the naming
  should reflect this."
  [{:db/id    :account/license
    :db/ident :account/licenses
    :db/doc   "All licenses that belong to this account."}])

(def ^{:added "1.3.0"} rename-member-application
  [{:db/id               :account/member-application
    :db/ident            :account/application
    :db.alter/_attribute :db.part/db}])

(def ^{:added "1.4.0"} add-notes-and-slack-handle
  (s/generate-schema
   [(s/schema
     account
     (s/fields
      [notes :ref :many :component
       "Any notes attached to this account."]

      [slack-handle :string :unique-identity
       "This account's Slack handle."]))]))

(defn- ^{:added "1.4.1"} add-collaborator-role [part]
  [{:db/id    (d/tempid part)
    :db/ident :account.role/collaborator}])

(def ^{:added "1.7.2"} rename-person-attrs
  [{:db/id               :account/first-name
    :db/ident            :person/first-name
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/middle-name
    :db/ident            :person/middle-name
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/last-name
    :db/ident            :person/last-name
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/phone-number
    :db/ident            :person/phone-number
    :db.alter/_attribute :db.part/db}])

(def ^{:added "1.7.2"} add-emergency-contact
  (s/generate-schema
   [(s/schema
     account
     (s/fields
      [emergency-contact :ref :component
       "Emergency contact information for this account."]))]))

(defn- ^{:added "2.7.0"} add-ptm-attrs [part]
  (concat
   (s/generate-schema
    [(s/schema
      account
      (s/fields
       [cooccupant :ref :indexed
        "The `account` of this applicant's co-occupant, if one exists."]

       [cosigner :ref :indexed
        "The `account` of this applicant's co-signer, if one exists."]))])

   [{:db/id    (d/tempid part)
     :db/ident :account.role/cosigner}
    {:db/id    (d/tempid part)
     :db/ident :account.role/sponsor}]))


(defn norms [part]
  {:starcity/add-account-schema
   {:txes [schema]}

   :starcity/add-account-roles
   {:txes [(roles part)]}

   :seed/add-account-role-pending-8-18-16
   {:txes [(add-role-pending part)]}

   :schema.account/add-indexes-11-20-16
   {:txes     [add-indexes]
    :requires [:starcity/add-account-schema]}

   :schema.account/rename-roles
   {:txes     [rename-roles]
    :requires [:starcity/add-account-roles]}

   :schema.account/license-alterations
   {:txes     [license-alterations]
    :requires [:starcity/add-account-schema]}

   :schema.account/rename-license-to-licenses
   {:txes     [rename-license-to-licenses]
    :requires [:starcity/add-account-schema]}

   :schema.account/rename-member-application
   {:txes     [rename-member-application]
    :requires [:starcity/add-account-schema]}

   :schema.account/schema-additions-02272017
   {:txes [add-notes-and-slack-handle]}

   :schema.account/add-collaborator-role-032217
   {:txes [(add-collaborator-role part)]}

   :schema.account/improvements-06142017
   {:txes     [rename-person-attrs
               add-emergency-contact]
    :requires [:starcity/add-account-schema]}

   :schema.account/add-ptm-attrs-06282018
   {:txes [(add-ptm-attrs part)]}})
