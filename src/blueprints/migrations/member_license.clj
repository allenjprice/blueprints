(ns blueprints.migrations.member-license
  (:require [datomic.api :as d]
            [blueprints.models.member-license :as member-license]
            [toolbelt.core :as tb]))

(defn- ^{:added "1.3.0"} use-member-license-status
  "Prior to 1.3.0, member licenses used a boolean flag to determine whether or
  not the license was active. Now we introduce a status attribute."
  [conn]
  (->> (d/q '[:find ?e ?active
              :where
              [?e :member-license/active ?active]]
            (d/db conn))
       (mapcat (fn [[e active]]
                 (let [s (if active
                           :member-license.status/active
                           :member-license.status/inactive)]
                   [[:db/add e :member-license/status s]
                    [:db/retract e :member-license/active active]])))))


(defn- all-member-licenses [db]
  (->> (d/q '[:find [?m ...]
              :where
              [?m :member-license/status _]]
            db)
       (map (partial d/entity db))))



(defn- rent-payment->payment [license rent-payment]
  (let [method (case (:rent-payment/method rent-payment)
                 :rent-payment.method/check   :payment.method/check
                 :rent-payment.method/autopay :payment.method/stripe-invoice
                 :rent-payment.method/ach     :payment.method/stripe-charge
                 :payment.method/other)
        status (case (:rent-payment/status rent-payment)
                 :rent-payment.status/due     :payment.status/due
                 :rent-payment.status/pending :payment.status/pending
                 :rent-payment.status/paid    :payment.status/paid
                 :payment.status/due)
        id     (d/tempid :db.part/starcity)]
    [(tb/assoc-when
      {:db/id           id
       :payment/method  method
       :payment/status  status
       :payment/amount  (:rent-payment/amount rent-payment)
       :payment/due     (:rent-payment/due-date rent-payment)
       :payment/account (:db/id (member-license/account license))
       :payment/for     :payment.for/rent
       :payment/pstart  (:rent-payment/period-start rent-payment)
       :payment/pend    (:rent-payment/period-end rent-payment)
       :payment/paid-on (:rent-payment/paid-on rent-payment)}
      :payment/check (:db/id (:rent-payment/check rent-payment))
      :stripe/charge-id (:charge/stripe-id (:rent-payment/charge rent-payment))
      :stripe/invoice-id (:rent-payment/invoice-id rent-payment))
     {:db/id                        (:db/id license)
      :member-license/rent-payments id}
     [:db/retract (:db/id license) :member-license/rent-payments (:db/id rent-payment)]]))


(defn generic-payment-tx [license]
  (mapcat
   (partial rent-payment->payment license)
   (:member-license/rent-payments license)))


(defn- ^{:added "1.11.0"} use-generic-payments [conn]
  (let [licenses (all-member-licenses (d/db conn))]
    (mapcat generic-payment-tx licenses)))


(defn norms [conn]
  {:migration.member-license/use-member-license-status-02162017
   {:txes [(use-member-license-status conn)]}

   :migration.member-license/use-generic-payments-08162017
   {:txes [(use-generic-payments conn)]}})
