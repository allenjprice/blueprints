(ns blueprints.migrations.dates-06122017
  "Up until this point in time we'd been using times that were not UTC-corrected
  for the time zone that the entity corresponds to. Here's an example:

  We're creating a rent payment for a member for the month of July. The period
  is the beginning of the month to the end of the month. We want the period to
  correspond to the ABSOLUTE UTC time, which means that the time zone of the
  building needs to be taken into consideration.

  DATE          BEFORE                           AFTER
  Period Start: 2017-04-01T00:00:00.000-00:00 -> 2017-04-01T07:00:00.000-00:00
  Period End:   2017-04-30T00:00:00.000-00:00 -> 2017-05-01T06:59:59.000-00:00"
  (:require [datomic.api :as d]
            [toolbelt.date :as date]
            [clj-time.core :as t]))

(def tz
  "The only active timezone at this point is America/Los_Angeles."
  (t/time-zone-for-id "America/Los_Angeles"))


(defn- update-date-tx
  [entity & attrs]
  (reduce
   (fn [acc attr]
     (if-let [v (get entity attr)]
       (->> (date/to-utc-corrected-date v tz)
            (assoc acc attr))
       acc))
   {:db/id (:db/id entity)}
   attrs))


(defn- entities [query db]
  (->> (d/q query db)
       (map (partial d/entity db))))


(defn member-licenses [conn]
  (->> (entities '[:find [?e ...]
                   :where
                   [?e :member-license/status _]]
                 (d/db conn))
       (mapv #(update-date-tx % :member-license/commencement :member-license/ends))))


(defn security-deposits [conn]
  (->> (entities '[:find [?e ...]
                   :where
                   [?e :security-deposit/due-by _]]
                 (d/db conn))
       (mapv #(update-date-tx % :security-deposit/due-by))))


(defn approvals [conn]
  (->> (entities '[:find [?e ...]
                   :where
                   [?e :approval/move-in _]]
                 (d/db conn))
       (mapv #(let [move-in (:approval/move-in %)]
                {:db/id            (:db/id %)
                 :approval/move-in (date/beginning-of-day move-in tz)}))))


(defn rent-payments [conn]
  (->> (entities '[:find [?e ...]
                   :where
                   [?e :rent-payment/period-start _]]
                 (d/db conn))
       (mapv #(update-date-tx %
                              :rent-payment/period-start
                              :rent-payment/period-end
                              :rent-payment/due-date))))


(defn norms [conn]
  {:migration/utc-dates-06122017
   {:txes [(member-licenses conn)
           (security-deposits conn)
           (approvals conn)
           (rent-payments conn)]}})
