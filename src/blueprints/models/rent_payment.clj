(ns blueprints.models.rent-payment
  (:require [clj-time
             [coerce :as c]
             [core :as t]]
            [clojure.spec :as s]
            [datomic.api :as d]
            [blueprints.models
             [check :as check]
             [member-license :as member-license]]
            [toolbelt
             [core :as tb]
             [date :as date]
             [predicates :as p]]))


;; =============================================================================
;; Constants
;; =============================================================================


(def max-autopay-failures
  "The maximum number of times that autopay payments will be tried before the
  subscription is canceled."
  3)


(def check
  "The check payment method."
  :rent-payment.method/check)


(def autopay
  "The autopay payment method."
  :rent-payment.method/autopay)


(def ach
  "The ACH payment method."
  :rent-payment.method/ach)


(def other
  "Some other payment method."
  :rent-payment.method/other)


;; =============================================================================
;; Specs
;; =============================================================================


(s/def ::method #{check ach autopay other})
(s/def ::status #{:rent-payment.status/due
                  :rent-payment.status/pending
                  :rent-payment.status/paid})



;; =============================================================================
;; Selectors
;; =============================================================================


(def member-license
  :member-license/_rent-payments)

(s/fdef member-license
        :args (s/cat :payment p/entity?)
        :ret p/entity?)


(def amount
  :rent-payment/amount)

(s/fdef amount
        :args (s/cat :payment p/entity?)
        :ret float?)


(def period-start
  :rent-payment/period-start)

(s/fdef period-start
        :args (s/cat :payment p/entity?)
        :ret inst?)


(def period-end
  :rent-payment/period-end)

(s/fdef period-end
        :args (s/cat :payment p/entity?)
        :ret inst?)


(def status
  :rent-payment/status)

(s/fdef status
        :args (s/cat :payment p/entity?)
        :ret ::status)


(def paid-on
  :rent-payment/paid-on)

(s/fdef paid-on
        :args (s/cat :payment p/entity?)
        :ret inst?)


(def due-date
  :rent-payment/due-date)

(s/fdef due-date
        :args (s/cat :payment p/entity?)
        :ret inst?)


(def invoice
  "The id of the Stripe invoice."
  :rent-payment/invoice-id)

(s/fdef invoice
        :args (s/cat :payment p/entity?)
        :ret (s/or :id string? :nothing nil?))


(def method
  "The method used to pay this payment."
  :rent-payment/method)

(s/fdef method
        :args (s/cat :payment p/entity?)
        :ret ::method)


(def charge
  "The charge associated with this payment."
  :rent-payment/charge)

(s/fdef charge
        :args (s/cat :payment p/entity?)
        :ret (s/or :nothing nil? :charge p/entity?))


(defn failures
  "The number of times autopay has failed."
  [payment]
  (get payment :rent-payment/autopay-failures 0))

(s/fdef failures
        :args (s/cat :payment p/entity?)
        :ret integer?)


(def autopay-failures failures)         ; alias


;; =============================================================================
;; Predicates
;; =============================================================================


(defn unpaid?
  "Is `payment` unpaid?"
  [payment]
  (#{:rent-payment.status/due} (status payment)))

(s/fdef unpaid?
        :args (s/cat :payment p/entity?)
        :ret boolean?)


(defn paid?
  "Is `payment` paid?"
  [payment]
  (#{:rent-payment.status/paid} (status payment)))

(s/fdef paid?
        :args (s/cat :payment p/entity?)
        :ret boolean?)


(defn past-due?
  "Is `payment` past due?"
  [payment]
  (let [due (-> payment due-date c/to-date-time)]
    (and (unpaid? payment) (t/after? (t/now) due))))

(s/fdef past-due?
        :args (s/cat :payment p/entity?)
        :ret boolean?)


;; =============================================================================
;; Transactions
;; =============================================================================


(defn- set-status [status payment]
  {:db/id               (:db/id payment)
   :rent-payment/status status})


(def set-due
  "Set `payment` status to due."
  (partial set-status :rent-payment.status/due))


(def set-pending
  "Set `payment` status to pending."
  (partial set-status :rent-payment.status/pending))


(def set-paid
  "Set `payment` status to paid."
  (partial set-status :rent-payment.status/paid))


(defn- default-due-date
  "The default due date is the fifth day of the same month as `start` date.
  Preserves the original year, month, hour, minute and second of `start` date."
  [start]
  (let [st (c/to-date-time start)]
    (c/to-date (t/date-time (t/year st)
                            (t/month st)
                            5
                            (t/hour st)
                            (t/minute st)
                            (t/second st)))))


(defn create
  "Create a rent payment."
  [amount period-start period-end status
   & {:keys [invoice-id method due-date check paid-on desc]}]
  (when-not (date/is-first-day-of-month? (c/to-date-time period-start))
    (assert due-date "Due date must be supplied when the period start is not the first day of the month."))
  (when (#{:rent-payment.status/paid} status)
    (assert method "If this payment has been `paid`, a `method` must be supplied."))
  (when (#{:rent-payment.method/check} method)
    (assert check "When paying by `check`, the `check` must be supplied."))
  (let [due-date (or due-date (default-due-date period-start))]
    (tb/assoc-when
     {:rent-payment/amount       amount
      :rent-payment/period-start period-start
      :rent-payment/period-end   period-end
      :rent-payment/status       status
      :rent-payment/due-date     due-date}
     :rent-payment/check check
     :rent-payment/method method
     :rent-payment/invoice-id invoice-id
     :rent-payment/paid-on paid-on
     :rent-payment/method-desc desc)))

(s/fdef create
        :args (s/cat :amount float?
                     :period-start inst?
                     :period-end inst?
                     :status ::status
                     :opts (s/keys* :opt-un [::invoice-id
                                             ::method
                                             ::due-date
                                             ::check
                                             ::paid-on
                                             ::desc]))
        :ret map?)


(defn autopay-payment
  "Create an autopay payment with status `:rent-payment.status/pending`."
  [member-license invoice-id period-start]
  (let [rate       (member-license/rate member-license)
        tz         (member-license/time-zone member-license)
        period-end (date/end-of-month period-start tz)
        due-date   (date/end-of-day (default-due-date period-start) tz)]
    (create rate (date/beginning-of-day period-start tz) period-end :rent-payment.status/pending
            :paid-on (c/to-date (t/now))
            :method autopay
            :invoice-id invoice-id
            :due-date due-date)))

(s/fdef autopay-payment
        :args (s/cat :member-license p/entity?
                     :invoice-id string?
                     :period-start inst?)
        :ret map?)


;; =============================================================================
;; Checks


(defn- check-status->status [check-status]
  (if (#{check/received check/deposited check/cleared} check-status)
    :rent-payment.status/paid
    :rent-payment.status/due))


(defn add-check
  "Add `check` to `payment`."
  [payment check]
  (let [new-status (check-status->status (check/status check))
        paid-on    (when (= :rent-payment.status/paid new-status)
                     (check/received-on check))]
    (tb/assoc-when
     {:db/id               (:db/id payment)
      :rent-payment/check  check
      :rent-payment/method :rent-payment.method/check
      :rent-payment/status new-status}
     :rent-payment/paid-on paid-on)))

(s/fdef add-check
        :args (s/cat :payment p/entity? :check check/check?)
        :ret (s/keys :req [:db/id
                           :rent-payment/check
                           :rent-payment/status
                           :rent-payment/method]
                     :opt [:rent-payment/paid-on]))


(defn- new-status [updated-check]
  (when-let [s (:check/status updated-check)]
    (check-status->status s)))


(defn- maybe-retract-paid-on [payment check updated-check]
  (let [paid-on (:rent-payment/paid-on payment)]
    (when (and (= (new-status updated-check) :rent-payment.status/due) paid-on)
      [:db/retract (:db/id payment) :rent-payment/paid-on paid-on])))


(defn- maybe-add-paid-on [payment check updated-check]
  (let [old-status (:rent-payment/status payment)]
    (when (and (= (new-status updated-check) :rent-payment.status/paid)
               (= old-status :rent-payment.status/due))
      [:db/add (:db/id payment) :rent-payment/paid-on
       (or (check/received-on updated-check)
           (check/received-on check))])))


(defn update-check
  [payment check updated-check]
  (->> [(tb/assoc-when
         {:db/id (:db/id payment)}
         :rent-payment/status (new-status updated-check))
        (maybe-retract-paid-on payment check updated-check)
        (maybe-add-paid-on payment check updated-check)]
       (remove nil?)))

(s/fdef update-check
        :args (s/cat :payment p/entity?
                     :check p/entity?
                     :updated-check check/updated?)
        :ret sequential?)


;; =============================================================================
;; Queries
;; =============================================================================


(defn by-invoice-id
  "Produce the rent payment for the given `invoice-id`."
  [db invoice-id]
  (d/entity db [:rent-payment/invoice-id invoice-id]))

(s/fdef by-invoice-id
        :args (s/cat :db p/db? :invoice-id string?)
        :ret p/entity?)


(defn by-charge
  "Produce the rent payment for the given `charge`."
  [db charge]
  (->> (d/q '[:find ?e .
              :in $ ?c
              :where
              [?e :rent-payment/charge ?c]]
            db (:db/id charge))
       (d/entity db)))

(s/fdef by-charge
        :args (s/cat :db p/db? :charge p/entity?)
        :ret p/entity?)
