(ns blueprints.seed.orders
  (:require [blueprints.models.order :as order]
            [blueprints.seed.utils :as utils]
            [datomic.api :as d]
            [clojure.string :as string]))


(defn rand-int-to-5
  "random number between 1 and 5 "
  []
  (inc (rand-int 5)))


(defn rand-string
  "random lorem ipsum sentences"
  []
  (-> "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.  Donec hendrerit tempor tellus.  Donec pretium posuere tellus.  Proin quam nisl, tincidunt et, mattis eget, convallis nec, purus.  Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus.  Nulla posuere.  Donec vitae dolor.  Nullam tristique diam non turpis.  Cras placerat accumsan nulla.  Nullam rutrum.  Nam vestibulum accumsan nisl. "
      (string/split #".  ")
      (rand-nth)))


(defn rand-inst
  "random instance between today and three weeks from today"
  []
  (utils/days-from-now (inc (rand-int 21))))


;; random option from a dropdown
(defn rand-option
  "randomly choose an option from a list of options"
  [options]
  (rand-nth options))

;; fill in a service's fields randomly

(defn order
  [account-id service-id & {:keys [quantity desc variant status price cost fields]
                            :or   {status :order.status/pending}}]
  (toolbelt.core/assoc-when
   {:db/id         (utils/tempid)
    :order/uuid    (d/squuid)
    :order/service service-id
    :order/account account-id
    :order/status  status}
   :order/price (when-let [p price] (float p))
   :order/cost (when-let [c cost] (float c))
   :order/variant variant
   :order/quantity (when-let [q quantity] (float q))
   :order/fields fields
   :order/desc desc))


(defn orders-for-account [db account-id]
  (let [services (d/q '[:find [?e ...] :where [?e :service/code _]] db)]
    (->> (shuffle services)
         (take (rand-int (count services)))
         (map (fn [e] (order account-id e))))))


(defn gen-orders [db account-ids]
  (mapcat (partial orders-for-account db) account-ids))
