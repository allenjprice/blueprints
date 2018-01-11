(ns blueprints.models.catalogue
  (:require [clojure.spec.alpha :as s]
            [datomic.api :as d]
            [toolbelt.core :as tb]
            [toolbelt.datomic :as td]))


;; =============================================================================
;; Queries
;; =============================================================================


(defn by-code
  ([db code]
   (->> (d/q '[:find ?e .
               :in $ ?c
               :where
               [?e :catalogue/code ?c]]
             db code)
        (d/entity db)))
  ([db code property]
   (->> (d/q '[:find ?e .
               :in $ ?c ?p
               :where
               [?e :catalogue/code ?c]
               [?e :catalogue/properties ?p]]
             db code (:db/id property))
        (d/entity db))))

(s/fdef by-code
        :args (s/cat :db td/db?
                     :code keyword?
                     :property (s/? td/entity?))
        :ret td/entity?)


(defn storage
  "The storage catalogue for `property`."
  [db property]
  (by-code db :storage property))


(defn customization
  "The customizations catalogue."
  [db]
  (by-code db :room/customize))


(defn cleaning+laundry
  "Cleaning and laundry services."
  [db]
  (by-code db :cleaning+laundry))


(defn upgrades
  "The Room Upgrades catalogue for `property`."
  [db property]
  (by-code db :room/upgrades property))


;; =============================================================================
;; Formatting
;; =============================================================================


(defmulti clientize-field :cat-field/type)


(defmethod clientize-field :cat-field.type/desc [field]
  {:type  :desc
   :label (:cat-field/label field)
   :key   (or (:cat-field/key field) :desc)})


(defmethod clientize-field :cat-field.type/quantity [field]
  {:type  :quantity
   :label (:cat-field/label field)
   :key   (or (:cat-field/key field) :quantity)
   :min   (:cat-field/min field)
   :max   (:cat-field/max field)
   :step  (:cat-field/step field)})


(defn clientize
  "Produce a client-friendly representation of a catalogue."
  [catalogue]
  (letfn [(-clientize-variant [variant]
            (tb/assoc-when
             {:id   (:db/id variant)
              :name (:svc-variant/name variant)}
             :price (:svc-variant/price variant)))
          (-clientize-item [item]
            (let [variants (when-let [vs (-> item :cat-item/service :service/variants)]
                             (map -clientize-variant vs))]
              (tb/assoc-when
               {:service (-> item :cat-item/service :db/id)
                :name    (or (:cat-item/name item)
                             (-> item :cat-item/service :service/name))
                :desc    (or (:cat-item/desc item)
                             (-> item :cat-item/service :service/desc))}
               :variants variants
               :fields (map clientize-field (:cat-item/fields item))
               :price (-> item :cat-item/service :service/price)
               :billed (when-let [b (-> item :cat-item/service :service/billed)]
                         (keyword (name b)))
               :rental (-> item :cat-item/service :service/rental))))]
    {:name  (:catalogue/name catalogue)
     :items (->> (:catalogue/items catalogue)
                 (sort-by :cat-item/index)
                 (map -clientize-item))}))
