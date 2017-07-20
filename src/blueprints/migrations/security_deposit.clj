(ns blueprints.migrations.security-deposit
  (:require [blueprints.models.security-deposit :as deposit]
            [blueprints.models.charge :as charge]
            [datomic.api :as d]))


(defn deposits [db method]
  (->> (d/q '[:find [?e ...]
              :in $ ?m
              :where
              [?e :security-deposit/payment-method ?m]]
            db method)
       (map (partial d/entity db))))


(defn charge-status->payment-status [cs]
  (case cs
    :charge.status/succeeded :payment.status/paid
    :charge.status/failed    :payment.status/failed
    :charge.status/pending   :payment.status/pending
    :payment.status/due))


(defn charge->payment [deposit charge]
  {:db/id            (d/tempid :db.part/starcity)
   :payment/id       (d/squuid)
   :payment/method   :payment.method/stripe-charge
   :payment/status   (charge-status->payment-status (charge/status charge))
   :payment/amount   (charge/amount charge)
   :payment/for      :payment.for/deposit
   :payment/account  (:db/id (charge/account charge))
   :payment/due      (:security-deposit/due-by deposit)
   :stripe/charge-id (charge/id charge)})


(defn charge-deposits [conn]
  (let [deposits (deposits (d/db conn) :security-deposit.payment-method/ach)]
    (mapcat
     (fn [deposit]
       (let [payments (mapv (partial charge->payment deposit) (deposit/charges deposit))]
         (conj payments
               {:db/id            (:db/id deposit)
                :deposit/amount   (float (deposit/amount-required deposit))
                :deposit/payments (map :db/id payments)})))
     deposits)))


(defn migrate-to-payments [conn]
  ;; At time of writing (7/20/17) there are no deposits that have been paid by
  ;; check!
  (charge-deposits conn))


(defn norms [conn]
  {:migration.security-deposit/migrate-to-payments-07202017
   {:txes [(migrate-to-payments conn)]}})
