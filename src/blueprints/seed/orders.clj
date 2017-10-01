(ns blueprints.seed.orders
  (:require [blueprints.models.order :as order]
            [blueprints.seed.utils :as utils]
            [datomic.api :as d]))


(defn order
  [account-id service-id & {:keys [quantity desc variant status price]
                            :or   {status :order.status/pending}}]
  (toolbelt.core/assoc-when
   {:db/id         (utils/tempid)
    :order/uuid    (d/squuid)
    :order/service service-id
    :order/account account-id
    :order/status  status}
   :order/price (when-let [p price] (float p))
   :order/variant variant
   :order/quantity (when-let [q quantity] (float q))
   :order/desc desc))


(defn orders-for-account [db account-id]
  (let [services (d/q '[:find [?e ...] :where [?e :service/code _]] db)]
    (->> (shuffle services)
         (take (rand-int (count services)))
         (map (fn [e] (order account-id e))))))


(defn gen-orders [db account-ids]
  (mapcat (partial orders-for-account db) account-ids))
