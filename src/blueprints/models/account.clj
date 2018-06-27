(ns blueprints.models.account
  (:require [blueprints.models.application :as application]
            [blueprints.models.approval :as approval]
            [clojure.spec.alpha :as s]
            [datomic.api :as d]
            [toolbelt.datomic :as td]
            [clojure.string :as string]))

;; =============================================================================
;; Spec
;; =============================================================================


(s/def ::role
  #{:account.role/applicant
    :account.role/onboarding
    :account.role/collaborator
    :account.role/member
    :account.role/admin
    :account.role/cosigner
    :account.role/cooccupant})


;; =============================================================================
;; Selectors
;; =============================================================================


(defn email
  "This account's email."
  [account]
  (:account/email account))

(s/fdef email
        :args (s/cat :account td/entity?)
        :ret string?)


(defn phone-number
  "This account's phone number."
  [account]
  (:account/phone-number account))

(s/fdef phone-number
        :args (s/cat :account td/entity?)
        :ret (s/or :phone string? :nothing nil?))


(def first-name
  :account/first-name)

(s/fdef first-name
        :args (s/cat :account td/entity?)
        :ret (s/or :phone string? :nothing nil?))


(def middle-name
  :account/middle-name)

(s/fdef middle-name
        :args (s/cat :account td/entity?)
        :ret (s/or :middle-name string? :nothing nil?))


(def last-name
  :account/last-name)

(s/fdef last-name
        :args (s/cat :account td/entity?)
        :ret (s/or :last-name string? :nothing nil?))


(def dob
  "Account's date of birth."
  :account/dob)

(s/fdef dob
        :args (s/cat :account td/entity?)
        :ret (s/or :dob inst? :nothing nil?))


(def activation-hash
  :account/activation-hash)

(s/fdef activation-hash
        :args (s/cat :account td/entity?)
        :ret (s/or :hash string? :nothing nil?))


(def member-application
  "The account's member application."
  :account/member-application)

(s/fdef member-application
        :args (s/cat :account td/entity?)
        :ret (s/or :application td/entity? :nothing nil?))


(def role
  "The account's role."
  :account/role)

(s/fdef role
        :args (s/cat :account td/entity?)
        :ret ::role)


(def security-deposit
  "Retrieve the `security-deposit` for `account`."
  (comp first :deposit/_account))

(s/fdef security-deposit
        :args (s/cat :account td/entity?)
        :ret (s/or :deposit td/entity? :nothing nil?))


(defn full-name
  "Full name of person identified by this account, or when no name exists, the
  `email`."
  [{:keys [:account/first-name :account/last-name :account/middle-name]
    :as   account}]
  (cond
    (or (nil? first-name) (nil? last-name))
    (:account/email account)

    (not (empty? middle-name))
    (format "%s %s %s" first-name middle-name last-name)

    :otherwise
    (format "%s %s" first-name last-name)))

(s/fdef full-name
        :args (s/cat :account td/entity?)
        :ret string?)


(defn short-name
  "Name of person sans middle name."
  [account]
  (format "%s %s" (first-name account) (last-name account)))

(s/fdef short-name
        :args (s/cat :account td/entity?)
        :ret string?)


(defn ^{:deprecated "1.6.0"} stripe-customer
  "Retrieve the `stripe-customer` that belongs to this account. Produces the
  customer that is on the Stripe master account, NOT the managed one -- the
  customer on the managed account will be used *only* for autopay.

  DEPRECATED: Prefer `blueprints.models.customer/by-account`"
  [db account]
  (->> (d/q '[:find ?e .
              :in $ ?a
              :where
              [?e :stripe-customer/account ?a]
              [(missing? $ ?e :stripe-customer/managed)]]
            db (:db/id account))
       (d/entity db)))


(defn approval
  "Produces the `approval` entity for `account`."
  [account]
  (-> account :approval/_account first))

(s/fdef approval
        :args (s/cat :account td/entity?)
        :ret (s/or :approval td/entity? :nothing nil?))


(def slack-handle
  "Produces the slack handle for this account."
  :account/slack-handle)

(s/fdef slack-handle
        :args (s/cat :account td/entity?)
        :ret (s/or :nothing nil? :handle string?))


(defn emergency-contact
  "The emergency contact entity for this `account`."
  [account]
  (:account/emergency-contact account))

(s/fdef emergency-contact
        :args (s/cat :account td/entity?)
        :ret (s/or :nothing nil? :contact td/entityd?))


(defmulti current-property
  "Produce the property associated with `account` in `db`."
  (fn [db account] (role account)))

(s/fdef current-property
        :args (s/cat :db td/db? :account td/entity?)
        :ret (s/nilable td/entityd?))


(defmethod current-property :default [_ _] nil)


(defn by-member-license [db account]
  (->> (d/q '[:find ?p .
              :in $ ?a
              :where
              [?a :account/licenses ?l]
              [?l :member-license/unit ?u]
              [?l :member-license/status :member-license.status/active]
              [?p :property/units ?u]]
            db (td/id account))
       (d/entity db)))


(defmethod current-property :account.role/admin [db account]
  (by-member-license db account))


(defmethod current-property :account.role/member [db account]
  (by-member-license db account))


(defmethod current-property :account.role/onboarding
  [db account]
  (->> (d/q '[:find ?p .
              :in $ ?a
              :where
              [?approval :approval/account ?a]
              [?approval :approval/property ?p]]
            db (td/id account))
       (d/entity db)))


;; =============================================================================
;; Predicates
;; =============================================================================


(defn exists?
  "Does an account exist for this email?"
  [db email]
  (boolean (d/entity db [:account/email email])))

(s/fdef exists?
        :args (s/cat :db td/db? :email string?)
        :ret boolean?)


(def bank-linked?
  "Is there a bank account linked to this account?"
  (comp not empty? :stripe-customer/_account))

(s/fdef bank-linked?
        :args (s/cat :account td/entity?)
        :ret boolean?)


(defn- is-role? [role account]
  (= role (:account/role account)))

(s/fdef is-role?
        :args (s/cat :role ::role :account td/entity?)
        :ret boolean?)


(def applicant?
  (partial is-role? :account.role/applicant))

(def onboarding?
  (partial is-role? :account.role/onboarding))

(def collaborator?
  (partial is-role? :account.role/collaborator))

(def member?
  (partial is-role? :account.role/member))

(def admin?
  (partial is-role? :account.role/admin))

(def cosigner?
  (partial is-role? :account.role/cosigner))

(def cooccupant?
  (partial is-role? :account.role/cooccupant))

(defn can-approve?
  "An account can be *approved* if the application is submitted and the account
  has the `:account.status/applicant` role."
  [account]
  (let [application (:account/application account)]
    (and (application/submitted? application) (applicant? account))))

(s/fdef can-approve?
        :args (s/cat :account td/entity?)
        :ret boolean?)


;; =============================================================================
;; Queries
;; =============================================================================


(defn by-email
  "Look up an account by email."
  [db email]
  (d/entity db [:account/email email]))

(s/fdef by-email
        :args (s/cat :db td/db? :email string?)
        :ret (s/or :account td/entity? :nothing nil?))


(defn by-customer-id
  "Look up an account by Stripe customer id."
  [db customer-id]
  (:stripe-customer/account
   (d/entity db [:stripe-customer/customer-id customer-id])))

(s/fdef by-customer-id
        :args (s/cat :db td/db? :customer-id string?)
        :ret (s/or :account td/entity? :nothing nil?))


(defn- accounts-query
  [db {:keys [q properties roles]}]
  (let [init '{:find  [[?a ...]]
               :in    [$]
               :args  []
               :where []}]
    (cond-> init
      true
      (update :args conj db)

      (not (empty? properties))
      (-> (update :in conj '[?p ...])
          (update :args conj (map td/id properties))
          (update :where conj
                  '[?a :account/licenses ?license]
                  '[?license :member-license/unit ?unit]
                  '[?license :member-license/status :member-license.status/active]
                  '[?p :property/units ?unit]))

      (not (empty? roles))
      (-> (update :in conj '[?role ...])
          (update :args conj roles)
          (update :where conj '[?a :account/role ?role]))

      (and (some? q) (not (string/blank? q)))
      (-> (update :in conj '?q)
          (update :args (fn [args]
                          (if (= (last q) \space)
                            (conj args (string/trim q))
                            (conj args (str q "*")))))
          (update :where conj
                  '(or [(fulltext $ :account/email ?q) [[?a]]]
                       [(fulltext $ :account/first-name ?q) [[?a]]]
                       [(fulltext $ :account/middle-name ?q) [[?a]]]
                       [(fulltext $ :account/last-name ?q) [[?a]]])))

      true
      (update :where #(conj % '[?a :account/role _])))))


(defn query
  "Query accounts using `params`."
  [db & {:as params}]
  (->> (accounts-query db params)
       (td/remap-query)
       (d/query)
       (map (partial d/entity db))))

(s/def ::q string?)
(s/def ::properties (s/+ td/entity?))
(s/def ::roles (s/+ ::role))

(s/fdef query
        :args (s/cat :db td/db?
                     :opts (s/keys* :opt-un [::q ::properties ::roles]))
        :ret (s/* td/entityd?))


;; =============================================================================
;; Transactions
;; =============================================================================


(defn collaborator
  "Create a new collaborator account. This is currently created for the express
  purpose of "
  [email]
  {:db/id         (d/tempid :db.part/starcity)
   :account/email email
   :account/role  :account.role/collaborator})

(s/fdef collaborator
        :args (s/cat :email string?)
        :ret (s/keys :req [:db/id :account/email :account/role]))


(defn change-role
  "Produce transaction data to change `account`'s role to `role`."
  [account role]
  {:db/id        (:db/id account)
   :account/role role})

(s/fdef change-role
        :args (s/cat :account td/entity? :role ::role)
        :ret (s/keys :req [:db/id :account/role]))


(defn cooccupant
  "The account of this applicant's co-applicant."
  [account]
  (:account/cooccupant account))

(s/fdef cooccupant
        :args (s/cat :account td/entityd?)
        :ret (s/nilable td/entityd?))


(defn cosigner
  "The account of this applicant's co-signer."
  [account]
  (:account/cosigner account))

(s/fdef cosigner
        :arg (s/cat :account td/entityd?)
        :ret (s/nilable td/entityd?))



;; =============================================================================
;; Metrics
;; =============================================================================


(defn total-created
  "Produce the number of accounts created between `pstart` and `pend`."
  [db pstart pend]
  (or (d/q '[:find (count ?e) .
             :in $ ?pstart ?pend
             :where
             [?e :account/activation-hash _ ?tx] ; use for creation inst
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


;; =============================================================================
;; Transformations
;; =============================================================================


(defn clientize
  "Produce a client-suitable representation of an `account` entity."
  [account]
  {:db/id         (:db/id account)
   :account/name  (full-name account)
   :account/email (email account)})

(s/fdef clientize
        :args (s/cat :account td/entity?)
        :ret (s/keys :req [:db/id :account/name :account/email]))
