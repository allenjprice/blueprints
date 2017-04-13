(ns blueprints.schema.security-deposit
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]))

(def ^{:added "1.1.x"} schema
  (s/generate-schema
   [(s/schema
     security-deposit
     (s/fields
      [account :ref
       "Account with which this security deposit is associated."]

      [amount-received :long
       "Amount of money that has been received for this security deposit in cents."]

      [amount-required :long
       "Amount of money that is needed for this security deposit in cents."]

      [payment-method :ref
       "Method of payment for security deposit."]

      [payment-type :ref
       "Method of payment for security deposit."]

      [due-by :instant
       "Datetime by which security deposit must be paid."]

      [charge :ref
       "Reference to the Stripe charge entity in event of ACH."]

      [check-cleared? :boolean
       "Flag for us to reflect successful clearance of check (when applicable)."]))]))

(defn- ^{:added "1.1.x"} payment-methods [part]
  [{:db/id    (d/tempid part)
    :db/ident :security-deposit.payment-method/ach}
   {:db/id    (d/tempid part)
    :db/ident :security-deposit.payment-method/check}])

(defn- ^{:added "1.1.x"} payment-types [part]
  [{:db/id    (d/tempid part)
    :db/ident :security-deposit.payment-type/partial}
   {:db/id    (d/tempid part)
    :db/ident :security-deposit.payment-type/full}])

(def ^{:added "1.1.1"} add-check-ref
  (s/generate-schema
   [(s/schema
     security-deposit
     (s/fields
      [checks :ref :many :component
       "Any checks that have been received to pay this security deposit."]))]))

(def ^{:added "< 1.1.3"} change-charge-to-charges
  [{:db/id               :security-deposit/charge
    :db/ident            :security-deposit/charges
    :db/cardinality      :db.cardinality/many
    :db.alter/_attribute :db.part/db}])

(def ^{:added "1.3.0"} improve-charges-attr
  [{:db/id               :security-deposit/charges
    :db/isComponent      true
    :db/index            true
    :db.alter/_attribute :db.part/db}])

;; TODO: Add `:security-deposit/required` and `:security-deposit/received` w/ type :float

(defn norms [part]
  {:schema/add-security-deposit-schema-8-18-16
   {:txes [(concat schema
                   (payment-methods part)
                   (payment-types part))]}

   :schema/add-checks-to-security-deposit-schema-11-4-16
   {:txes [add-check-ref]}

   :schema/alter-security-deposit-schema-11-2-16
   {:txes     [change-charge-to-charges]
    :requires [:schema/add-security-deposit-schema-8-18-16]}

   :schema.security-deposit/improve-charges-attr-02-14-17
   {:txes [improve-charges-attr]
    :requires [:schema/alter-security-deposit-schema-11-2-16]}})
