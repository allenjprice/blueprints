(ns blueprints.schema.member-license
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]))

(def ^{:added "1.0.0"} schema
  (s/generate-schema
   [(s/schema
     member-license
     (s/fields
      [license :ref
       "Reference to the license that this member has agreed to."]

      [price :float
       "The price of the member's license per month. This includes the base price
     of the license plus any additional fees, e.g. for pets."]

      [commencement-date :instant
       "The date that this license takes effect."]

      [end-date :instant
       "The date that this license ends."]))]))

;;; Historical comment:

;; A number of changes introduced in December of 2016 now that we're actually
;; implementing a rent payment system on Stripe.

;; To summarize:
;; - adds indexes on existing attributes
;; - renames some existing attributes
;; - adds several new attributes

(def ^{:added "1.2.0"} add-autopay
  (s/generate-schema
   [(s/schema
     member-license
     (s/fields
      [active :boolean :index
       "Indicates whether or not this license is active. This is necessary
       because renewal results in creation of a new license."]

      [customer :ref :index
       "Reference to the managed :stripe-customer entity."]

      [plan-id :string :unique-identity
       "The id of the plan."]

      [subscription-id :string :unique-identity
       "The id of the subscription in Stripe that does the rent billing."]

      [unit :ref :index
       "Reference to the unit that the holder of this member license lives in."]))]))

(def ^{:added "1.2.0"} schema-improvements
  [{:db/id               :member-license/commencement-date
    :db/ident            :member-license/commencement ; rename
    :db/index            true                         ; index it
    :db.alter/_attribute :db.part/db}
   {:db/id               :member-license/license
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :member-license/price
    :db/ident            :member-license/rate
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :member-license/end-date
    :db/ident            :member-license/ends
    :db/index            true
    :db.alter/_attribute :db.part/db}])

(def ^{:added "1.2.0"} add-rent-payments-to-license
  (s/generate-schema
   [(s/schema
     member-license
     (s/fields
      [rent-payments :ref :many :index :component
       "References the rent payments that have been made for this license by owner."]))]))

;; =====================================
;; Rent Payments

(defn- ^{:added "1.2.0"} rent-methods [part]
  [{:db/id    (d/tempid part)
    :db/ident :rent-payment.method/check}
   {:db/id    (d/tempid part)
    :db/ident :rent-payment.method/autopay}
   {:db/id    (d/tempid part)
    :db/ident :rent-payment.method/ach}
   {:db/id    (d/tempid part)
    :db/ident :rent-payment.method/other}])

(defn- ^{:added "1.2.0"} rent-statuses [part]
  [{:db/id    (d/tempid part)
    :db/ident :rent-payment.status/due}
   {:db/id    (d/tempid part)
    :db/ident :rent-payment.status/pending}
   {:db/id    (d/tempid part)
    :db/ident :rent-payment.status/paid}])

(def ^{:added "1.2.0"} rent-payment-schema
  (s/generate-schema
   [(s/schema
     rent-payment
     (s/fields
      [method :ref :index
       "The method of payment."]

      [status :ref :index
       "The status of this payment."]

      [amount :float :index
       "The amount in dollars that was paid."]

      [period-start :instant :index
       "The start date of this payment period."]

      [period-end :instant :index
       "The end date of this payment period."]

      [due-date :instant :index
       "The due date for this payment."]

      [paid-on :instant :index
       "Date that this payment was successfully paid on."]

      [check :ref :component
       "The associated check entity, in the event that `method` is `check`."]

      [charge :ref :component
       "The associated charge entity, in the event that `method` is `ach`."]

      [method-desc :string :fulltext
       "Description of the payment method, in the event that `method` is `other`."]

      [notes :ref :fulltext :many :component
       "Reference to any notes that have been added to this payment."]

      [invoice-id :string :unique-identity
       "The id of the Stripe Invoice if this payment is made with autopay."]

      [autopay-failures :long :index
       "The number of times that this payment has failed through autopay."]))]))

(def ^{:added "1.3.0"} member-license-improvements-02162017
  (s/generate-schema
   [(s/schema
     member-license
     (s/fields
      [status :ref :index
       "The status of this member license: active, inactive, renewal, canceled"]

      [move-out :boolean :index
       "Is this member moving out? Its presence (= true) tells us NOT to auto-renew the license."]))]))

(defn- ^{:added "1.3.0"} member-license-statuses [part]
  [{:db/id    (d/tempid part)
    :db/ident :member-license.status/active}
   {:db/id    (d/tempid part)
    :db/ident :member-license.status/inactive}
   {:db/id    (d/tempid part)
    :db/ident :member-license.status/renewal}
   {:db/id    (d/tempid part)
    :db/ident :member-license.status/canceled}])

(def ^{:added "1.3.0"} deprecations-02162017
  [{:db/id               :member-license/active
    :db/doc              "DEPRECATED 2/16/17: Use `:member-license.status/active` instead."
    :db.alter/_attribute :db.part/db}])


;; TODO: check version
(defn ^{:added "2.6.0"} license-transitions [part]
  (concat
   (s/generate-schema
    [(s/schema
      license-transition
      (s/fields

       [current-license :ref :indexed
        "The license which is coming to an end soon"]

       [type :ref :indexed
        "Indicates the type of transition for this license"]

       [uuid :uuid :unique-identity
        "A uuid to identify this transition"]

       ;; are refs to teller *these payments*, not *blueprints payments*
       [fees :ref :indexed :many
        "A list of any fees that may be incurred in this transition, like Early Termination or Room Reassignment"]

       [early-termination-fee :float :indexed
        "A dollar amount that should be paid by the member for ending their license early."]

       [deposit-refund :float :indexed
        "How much of the security deposit for this license should be refunded to the member at the end of this transition?"]

       [room-walkthrough-doc :string :indexed
        "A link to the Google Doc report of the final room walkthrough, generated by the community team"]

       [new-license :ref :indexed
        "A ref to a new `member-license` with status of `pending`, or a `nil` value if the membership is coming to an end"]

       [date :instant :indexed
        "The date on which this license transition will become inactive"]))])

   [
    ;; Add the "pending" member license status for licenses that exist in our system which will become active at a later date
    {:db/id (d/tempid part)
     :db/ident :member-license.status/pending}

    ;;transition types
    {:db/id (d/tempid part)
     :db/ident :license-transition.type/move-out}
    {:db/id (d/tempid part)
     :db/ident :license-transition.type/intra-xfer}
    {:db/id (d/tempid part)
     :db/ident :license-transition.type/inter-xfer}
    {:db/id (d/tempid part)
     :db/ident :license-transition.type/renewal}]))


;;=============================================================================
;; Norms
;; =============================================================================


(defn norms [part]
  {:starcity/add-member-license-schema
   {:txes [schema]}

   :schema.member-license/rent-alterations
   {:txes     [add-autopay schema-improvements]
    :requires [:starcity/add-member-license-schema]}

   :schema.member-license/add-rent-payments
   {:txes [(rent-statuses part)
           (rent-methods part)
           rent-payment-schema
           add-rent-payments-to-license]}

   :schema.member-license/improvements-02162017
   {:txes     [member-license-improvements-02162017
               (member-license-statuses part)
               deprecations-02162017]
    :requires [:schema.member-license/rent-alterations]}


   :schema.member-license/license-transitions-051018
   {:txes [(license-transitions part)]}})
