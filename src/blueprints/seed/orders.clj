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
  (-> "Doggo ipsum floofs long woofer smol borking doggo with a long snoot for pats borking doggo heck boofers, pats you are doing me a frighten wow very biscit dat tungg tho, adorable doggo waggy wags shibe borking doggo.  Smol borking doggo with a long snoot for pats clouds corgo, clouds.  Most angery pupper I have ever seen very taste wow wrinkler long bois super chub bork the neighborhood pupper woofer, smol long water shoob aqua doggo many pats h*ck.  Yapper big ol pupperino the neighborhood pupper super chub you are doin me a concern, boof heckin good boys and girls he made many woofs sub woofer.  Bork I am bekom fat he made many woofs porgo borking doggo super chub clouds, woofer most angery pupper I have ever seen extremely cuuuuuute fat boi.  You are doing me the shock very hand that feed shibe doing me a frighten dat tungg tho, borking doggo wow very biscit.  Very jealous pupper long bois the neighborhood pupper stop it fren heckin, much ruin diet pats mlem.  Borkf clouds lotsa pats heckin good boys and girls, doggorino doggo.  Sub woofer the neighborhood pupper fluffer floofs noodle horse smol borking doggo with a long snoot for pats, puggorino the neighborhood pupper pats.  Length boy lotsa pats much ruin diet borkdrive, noodle horse.  The neighborhood pupper maximum borkdrive clouds fat boi extremely cuuuuuute ur givin me a spook, doggorino borking doggo long woofer long water shoob, very jealous pupper maximum borkdrive pupperino floofs.  Mlem shibe lotsa pats cck heckin good boys and girls, clouds vvv heck.  pupper you are doing me the shock heck.  Yapper shoober pats doing me a frighten, borking doggo.  Thicc big ol heckin good boys and girls extremely cuuuuuute, such treat wrinkler shoob puggo, such treat clouds.  Very good spot wow such tempt pupperino woofer wrinkler such treat, wow very biscit shibe floofs fat boi.  Snoot heckin angery woofer super chub you are doin me a concern such treat, pats shooberino pupper.  Heckin tungg bork wrinkler pats, borkf he made many woofs.  "
      (string/split #".  ")
      (rand-nth)))


(defn rand-inst
  "random instance between today and three weeks from today"
  []
  (utils/days-from-now (inc (rand-int 21))))


(defn rand-option
  "randomly choose an option from a list of options"
  [options]
  (:service-field-option/value (rand-nth (vec options))))


(defn rand-service-field-by-type
  [field]
  (let [order-field {:order-field/service-field (:db/id field)}]
    (->> (case (:service-field/type field)
           :service-field.type/number   (float (rand-int-to-5))
           :service-field.type/time     (rand-inst)
           :service-field.type/date     (rand-inst)
           :service-field.type/dropdown (rand-option (:service-field/options field))
           (rand-string))
         (assoc order-field (order/order-field-key field)))))


(defn rand-service-fields
  "fill in a service's fields randomly"
  [fields]
  (map #(rand-service-field-by-type %) fields))


(defn rand-order-status
  "generate random order status"
  []
  (let [statuses [:pending :placed :canceled :charged :failed :processing :fulfilled]]
    (-> (map #(keyword "order.status" (name %)) statuses)
        (rand-nth))))


(defn order
  [account-id service-id service & {:keys [quantity desc variant price cost fields]}]
  (toolbelt.core/assoc-when
   {:db/id         (utils/tempid)
    :order/uuid    (d/squuid)
    :order/service service-id
    :order/account account-id
    :order/status  (rand-order-status)}
   :order/price (when-let [p price] (float p))
   :order/cost (when-let [c cost] (float c))
   :order/variant variant
   :order/quantity (when-let [q quantity] (float q))
   :order/fields (rand-service-fields (:service/fields service))
   :order/desc desc))


(defn orders-for-account [db account-id]
  (let [services (d/q '[:find [?e ...] :where [?e :service/code _]] db)]
    (->> (shuffle services)
         (take (rand-int (count services)))
         (map (fn [e] (order account-id e (d/entity db e)))))))


(defn gen-orders [db account-ids]
  (mapcat (partial orders-for-account db) account-ids))
