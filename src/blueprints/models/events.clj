(ns blueprints.models.events
  (:require [blueprints.models.account :as account]
            [blueprints.models.event :as event]
            [blueprints.models.note :as note]
            [blueprints.models.payment :as payment]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [toolbelt.core :as tb]
            [toolbelt.datomic :as td]))


;; =============================================================================
;; Accounts
;; =============================================================================


(defn reset-password
  "Reset `account`'s password."
  [account]
  (event/job :account/reset-password {:params {:account-id (td/id account)}}))

(s/fdef reset-password
        :args (s/cat :account td/entity?)
        :ret map?)


(defn create-account
  "Create a new account."
  [email password first-name last-name & {:keys [middle-name]}]
  (event/job :account/create {:params (tb/assoc-when
                                       {:email      email
                                        :password   password
                                        :first-name first-name
                                        :last-name  last-name}
                                       :middle-name middle-name)}))

(s/fdef create-account
        :args (s/cat :email string?
                     :password string?
                     :first-name string?
                     :last-name string?
                     :opts (s/keys* :opt-un [::middle-name]))
        :ret map?)


(defn create-collaborator
  "Create a new collaborator."
  [email type message]
  (event/job :collaborator/create {:params {:email   email
                                            :type    type
                                            :message message}}))

(s/fdef create-collaborator
        :args (s/cat :email string?
                     :type #{"real-estate" "community-stakeholder" "vendor" "investor"}
                     :message string?)
        :ret map?)


(defn account-promoted
  "`account` has been promoted to membership."
  [account]
  (event/job :account/promoted {:params {:account-id (td/id account)}}))

(s/fdef account-promoted
        :args (s/cat :account td/entity?)
        :ret map?)


(defn account-approved
  "`account` has been approved for membership."
  [account]
  (event/job :account/approved {:params {:account-id (td/id account)}}))

(s/fdef account-approved
        :args (s/cat :account td/entity?)
        :ret map?)


;; =============================================================================
;; Deposit
;; =============================================================================


(defn deposit-payment-made
  [account payment-id]
  (event/job :deposit/payment-made {:params {:account-id (td/id account)
                                             :payment-id payment-id}}))

(s/fdef deposit-payment-made
        :args (s/cat :account td/entity? :payment-id uuid?)
        :ret map?)


(defn remainder-deposit-payment-made
  [account payment-id]
  (event/job :deposit.remainder/payment-made {:params {:account-id (td/id account)
                                                       :payment-id payment-id}}))

(s/fdef remainder-deposit-payment-made
        :args (s/cat :account td/entity? :payment-id string?)
        :ret map?)


(defn initiate-refund
  "Initiate the security deposit refund procedure."
  [deposit amount reasons]
  (event/job :deposit/refund {:params (tb/assoc-when
                                       {:deposit-id (td/id deposit)
                                        :amount     amount}
                                       :reasons reasons)}))

(s/fdef initiate-refund
        :args (s/cat :deposit td/entity?
                     :amount number?
                     :reasons (s/or :nothing nil? :string string?))
        :ret map?)


(defn alert-unpaid-deposits
  "Send alerts to indicate that `deposits` are unpaid as of as-of time `t`."
  [deposits t]
  (event/job :deposits/alert-unpaid
             {:params {:deposit-ids (map td/id deposits)
                       :as-of       t}}))

(s/fdef alert-unpaid-deposits
        :args (s/cat :deposits (s/+ td/entity?) :t inst?)
        :ret map?)


(defn alert-deposit-due
  "Send alerts to indicate that `deposit` is due soon relative to time `t`."
  [deposit t]
  (event/notify :deposit/due {:params {:deposit-id (td/id deposit)
                                       :as-of      t}}))

(s/fdef alert-deposit-due
        :args (s/cat :deposit td/entity? :t inst?)
        :ret map?)


;; =============================================================================
;; Newsletter
;; =============================================================================


(defn add-newsletter-subscriber
  "Add `email` address to our newsletter."
  [email]
  (event/job :newsletter/subscribe {:params {:email email}}))

(s/fdef add-newsletter-subscriber
        :args (s/cat :email string?)
        :ret map?)


;; =============================================================================
;; Notes
;; =============================================================================


(defn note-created
  "A `note` was created."
  [note]
  (event/job :note/created {:params {:uuid (note/uuid note)}}))

(s/fdef note-created
        :args (s/cat :note td/entity?)
        :ret map?)


(defn note-comment-created
  "A `comment` has been added to a `note`."
  [note comment]
  (event/job :note.comment/created {:params {:comment-uuid (note/uuid comment)
                                             :note-id      (td/id note)}}))

(s/fdef added-note-comment
        :args (s/cat :note td/entity? :comment td/entity?)
        :ret map?)


;; =============================================================================
;; Orders
;; =============================================================================


(defn process-order
  "Indicate that `account` has submitted `order` to be processed."
  [account order]
  (event/job :order/process {:params {:order-id   (td/id order)
                                      :account-id (td/id account)}}))

(s/fdef process-order
        :args (s/cat :account td/entity?
                     :order td/entity?)
        :ret map?)


(defn order-created
  "Indicate that an `order` has been created."
  [account order-uuid & [notify]]
  (event/job :order/created {:params {:order-uuid order-uuid
                                      :account-id (td/id account)
                                      :notify     (boolean notify)}}))

(s/fdef order-created
        :args (s/cat :account td/entity?
                     :uuid uuid?
                     :notify (s/? boolean?))
        :ret map?)


(defn order-placed
  "Indicates that `order` has been placed by `account`. Can optionally specify
  whether or not to notify the order owner."
  [account order & [notify]]
  (event/job :order/placed {:params {:order-id   (td/id order)
                                     :account-id (td/id account)
                                     :notify     (boolean notify)}}))

(s/fdef order-placed
        :args (s/cat :account td/entity?
                     :order td/entity?
                     :notify (s/? boolean?))
        :ret map?)


(defn order-fulfilled
  "Indicates that `order` has been fulfilled by `account`. Can optionally specify
  whether or not to notify the order owner."
  [account order & [notify]]
  (event/job :order/fulfilled {:params {:order-id   (td/id order)
                                        :account-id (td/id account)
                                        :notify     (boolean notify)}}))

(s/fdef order-fulfilled
        :args (s/cat :account td/entity?
                     :order td/entity?
                     :notify (s/? boolean?))
        :ret map?)


(defn order-canceled
  "Indicates that `order` has been canceled by `account`. Can optionally specify
  whether or not to notify the order owner."
  [account order & [notify]]
  (event/job :order/canceled {:params {:order-id   (td/id order)
                                       :account-id (td/id account)
                                       :notify     (boolean notify)}}))

(s/fdef order-canceled
        :args (s/cat :account td/entity?
                     :order td/entity?
                     :notify (s/? boolean?))
        :ret map?)


;; =============================================================================
;; Payments
;; =============================================================================


(defn alert-payment-due
  "Send alerts to indicate that `payment` is due soon relative to time `t`."
  [payment t]
  (event/notify :payment/due {:params {:payment-id (td/id payment)
                                       :as-of      t}}))

(s/fdef alert-payment-due
        :args (s/cat :payment td/entity? :t inst?)
        :ret map?)



;; =============================================================================
;; Rent
;; =============================================================================


(defn create-monthly-rent-payments
  "Create rent payments for the current time `period` for members that are not
  on autopay."
  [period]
  (event/job :rent-payments/create-all {:params {:period period}}))

(s/fdef create-monthly-rent-payments
        :args (s/cat :period inst?)
        :ret map?)


(defn rent-payment-made
  "A member has paid his/her rent by ACH."
  [account payment]
  (event/job :rent-payment.payment/ach {:params {:account-id (td/id account)
                                                 :payment-id (td/id payment)}}))

(s/fdef rent-payment-made
        :args (s/cat :account td/entity? :payment td/entity?)
        :ret map?)


(defn alert-all-unpaid-rent
  "Send alerts to indicate that rent `payments` are unpaid as of as-of time `t`."
  [payments t]
  (event/job :rent-payments/alert-unpaid
             {:params {:payment-ids (map td/id payments)
                       :as-of       t}}))

(s/fdef alert-all-unpaid-rent
        :args (s/cat :payments (s/+ td/entity?) :date inst?)
        :ret map?)


;; =============================================================================
;; Scheduler
;; =============================================================================


(defn process-daily-tasks
  "Event that triggers daily tasks."
  [t]
  (event/job :scheduler/daily {:params {:as-of t}}))

(s/fdef process-daily-tasks
        :args (s/cat :t inst?)
        :ret map?)


;; =============================================================================
;; Session
;; =============================================================================


(defn revoke-session
  "Revoke `account`'s session."
  [account]
  (event/job :session/revoke {:params {:account-id (td/id account)}}))

(s/fdef revoke-session
        :args (s/cat :account td/entity?)
        :ret map?)


;; =============================================================================
;; Stripe
;; =============================================================================


(defn- snake->kebab [s]
  (string/replace s #"_" "-"))


(defn- event-type->key [et]
  (let [parts (->> (concat '("stripe" "event") (string/split et #"\."))
                   (map snake->kebab))]
    (keyword
     (string/join "." (butlast parts))
     (last parts))))

(s/fdef event-type->key
        :args (s/cat :type string?)
        :ret keyword?)


(defn stripe-event
  [event-id event-type connect-id]
  (let [meta (when-some [x connect-id] {:managed-account x})]
    (event/stripe (event-type->key event-type)
                  (tb/assoc-when {:id event-id} :meta meta))))

(s/fdef stripe-event
        :args (s/cat :event-id string?
                     :event-type string?
                     :connect-id (s/or :string string? :nothing nil?))
        :ret map?)


(defn delete-source
  "Delete `customer`'s source."
  [customer source-id & [triggered-by]]
  (event/job :stripe.customer.source/delete
             (tb/assoc-when
              {:params {:customer  customer
                        :source-id source-id}}
              :triggered-by triggered-by)))

(s/fdef delete-source
        :args (s/cat :customer string?
                     :source-id string?
                     :triggered-by (s/? td/entity?))
        :ret map?)
