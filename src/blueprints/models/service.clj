(ns blueprints.models.service
  (:require [blueprints.models.property :as property]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [datomic.api :as d]
            [toolbelt.core :as tb]
            [toolbelt.datomic :as td]
            [toolbelt.datomic.schema :as tds]))

;; =============================================================================
;; Spec
;; =============================================================================


(s/def ::billed
  #{:service.billed/once :service.billed/monthly})


;; =============================================================================
;; Selectors
;; =============================================================================


(defn code
  "The code used to refer to this service."
  [service]
  (:service/code service))

(s/fdef code
        :args (s/cat :service td/entity?)
        :ret string?)


(defn fields
  [service]
  (:service/fields service))


(defn service-name
  "The human-friendly name of this service."
  [service]
  (:service/name service))

(s/fdef service-name
        :args (s/cat :service td/entity?)
        :ret string?)


(defn desc
  "The human-friendly description of this service."
  [service]
  (:service/desc service))

(s/fdef desc
        :args (s/cat :service td/entity?)
        :ret string?)


(defn price
  "The price of this service."
  [service]
  (:service/price service))

(s/fdef price
        :args (s/cat :service td/entity?)
        :ret (s/or :nothing nil? :price float?))


(defn cost
  "The cost of this service."
  [service]
  (:service/cost service))

(s/fdef cost
        :args (s/cat :service td/entity?)
        :ret (s/or :nothing nil? :cost float?))


(defn rental
  "Is this service a rental?"
  [service]
  (get service :service/rental false))

(s/fdef rental
        :args (s/cat :service td/entity?)
        :ret boolean?)


(defn billed
  "Billing method of this service."
  [service]
  (:service/billed service))

(s/fdef billed
        :args (s/cat :service td/entity?)
        :ret ::billed)


(defn name-internal
  "The internal name of this service."
  [service]
  (:service/name-internal service))

(s/fdef name-internal
        :args (s/cat :service td/entity?)
        :ret string?)


(defn desc-internal
  "The internal description of this service"
  [service]
  (:service/desc-internal service))

(s/fdef desc-internal
        :args (s/cat :service td/entity?)
        :ret string?)


(defn catalogs
  "The catalogs this service is part of."
  [service]
  (:service/catalogs service))


(defn properties
  "Properties this service is offered at."
  [service]
  (:service/properties service))


(defn variants
  "Variants of this service."
  [service]
  (:service/variants service))


(defn options
  "Options of the dropdown field in a service"
  [field]
  (:service-field/options field))


;; Might need an active selector



;; =============================================================================
;; Queries
;; =============================================================================


(defn ^{:deprecated "2.3.0"} by-code
  "Find the service identified by `code`, optionally restricted to `property`."
  ([db code]
   (->> (d/q '[:find ?e .
               :in $ ?c
               :where
               [?e :service/code ?c]]
             db code)
        (d/entity db)))
  ([db code property]
   (->> (d/q '[:find ?e .
               :in $ ?c ?p
               :where
               [?e :service/code ?c]
               [?e :service/properties ?p]]
             db code (:db/id property))
        (d/entity db))))

(s/fdef by-code
        :args (s/cat :db td/db?
                     :code string?
                     :property (s/? td/entity?))
        :ret (s/or :nothing nil? :service td/entity?))


(defn ^{:deprecated "2.3.0"} ordered-from-catalogue
  "Produce a list of service ids that have been ordered by `account` from
  `catalogue`."
  [db account catalogue]
  (d/q '[:find [?s ...]
         :in $ ?a ?ca
         :where
         [?o :order/account ?a]
         [?o :order/service ?s]
         [?ca :catalogue/items ?ci]
         [?ci :cat-item/service ?s]]
       db (:db/id account) (:db/id catalogue)))

(s/fdef ordered-from-catalogue
        :args (s/cat :db td/db?
                     :account td/entity?
                     :catalogue td/entity?)
        :ret (s/* integer?))


(defn ^{:deprecated "2.3.0"} list-all
  "List all services in a human-readable way."
  [db]
  (let [svcs (->> (d/q '[:find [?e ...]
                         :where
                         [?e :service/code _]]
                       db)
                  (map (partial d/entity db)))]
    (println "CODE :: NAME :: DESCRIPTION")
    (doseq [svc (sort-by :service/code svcs)]
      (println (format "%s: '%s' (%s)" (code svc) (name svc) (desc svc))))))


(defn- services-query
  [db {:keys [q properties catalogs]}]
  (let [init '{:find  [[?s ...]]
               :in    [$]
               :args  []
               :where []}]
    (cond-> init
      true
      (update :args conj db)

      (not (string/blank? q))
      (-> (update :in conj '?q)
          (update :args (td/safe-wildcard q))
          (update :where conj
                  '(or [(fulltext $ :service/code ?q) [[?s]]]
                       [(fulltext $ :service/name ?q) [[?s]]]
                       [(fulltext $ :service/desc ?q) [[?s]]])))

      (not (empty? properties))
      (-> (update :in conj '[?p ...])
          (update :where conj '[?s :service/properties ?p])
          (update :args conj properties))

      (not (empty? catalogs))
      (-> (update :in conj '[?c ...])
          (update :where conj '[?s :service/catalogs ?c])
          (update :args conj catalogs))

      true
      (update :where #(if (empty? %) (conj % '[?s :service/code _]) %)))))


;; TODO: billed
(defn query
  "Query services using `params`."
  [db params]
  (->> (services-query db params)
       (td/remap-query)
       (d/query)
       (map (partial d/entity db))))


(comment

  (->> (query (d/db user/conn)
              {:q "move"})
       (map d/touch))
  )


;; =============================================================================
;; Lookups


(defn moving-assistance [db]
  (by-code db "moving,move-in"))


(defn small-bin [db property]
  (by-code db "storage,bin,small" property))


(defn large-bin [db property]
  (by-code db "storage,bin,large" property))


(defn misc-storage [db]
  (by-code db "storage,misc"))


(defn customize-furniture [db]
  (by-code db "customize,furniture,quote"))


(defn customize-room [db]
  (by-code db "customize,room,quote"))


(defn weekly-cleaning [db]
  (by-code db "cleaning,weekly"))


;; =============================================================================
;; Transactions
;; =============================================================================


(defn create-field
  "Create a new field."
  ([label type]
   (create-field label type {}))
  ([label type {:keys [index] :or {index 0}}]
   {:service-field/index index
    :service-field/type  (keyword "service-field.type" (name type))
    :service-field/label label}))


(defn create-variant
  "Create a new variant"
  [name price cost]
  {:svc-variant/name  name
   :svc-variant/price (float price)
   :svc-variant/cost  (float cost)})


(defn create
  "Create a new service."
  ([code name desc]
   (create code name desc {}))
  ([code name desc {:keys [price cost rental billed fields catalogs
                           name-internal properties desc-internal variants]
                    :or   {rental        false
                           name-internal name
                           billed        :service.billed/once
                           desc-internal desc}}]
   (tb/assoc-when
    {:db/id          (tds/tempid)
     :service/code   code
     :service/name   name
     :service/desc   desc
     :service/billed billed
     :service/rental rental}
    :service/price (when-some [p price] (float p))
    :service/cost (when-some [c cost] (float c))
    :service/catalogs catalogs
    :service/variants variants
    :service/fields (when-some [fs fields]
                      (map-indexed
                       #(assoc %2 :service-field/index %1)
                       fs))
    :service/properties (when-some [ps properties]
                          (map td/id ps)))))


(defn edit
  [service {:keys [name desc billed rental name-internal desc-internal
                   cost price catalogs fields properties variants]}]
  (tb/assoc-when
   {:db/id (td/id service)}
   :service/name   name
   :service/desc   desc
   :service/billed billed
   :service/rental rental
   :service/name-internal name-internal
   :service/desc-internal desc-internal
   :service/cost (when-some [c cost] (float c))
   :service/price (when-some [p price] (float p))
   :service/fields (when-some [fs fields]
                     (map-indexed
                      #(assoc %2 :service-field/index %1)
                      fs))
   :service/catalogs catalogs
   :service/properties (when-some [ps properties]
                         (map td/id ps))
   :service/variants variants))


(defn edit-field
  "Edit one field within a service"
  [field {:keys [label type index]}]
  (tb/assoc-when
   {:db/id (td/id field)}
   :service-field/type type
   :service-field/label label
   :service-field/index index))


(defn create-option
  "Create a new option (to be associated with service fields of type `dropdown`"
  ([label value]
   (create-option label value {}))
  ([label value {:keys [index] :or {index 0}}]
   {:service-field-option/value value ;;TODO - maybe we shouldn't expect the community team/admins to supply this value?
    :service-field-option/label label
    :service-field-option/index index}))


(defn edit-option
  "Edit one option within a dropdown service field"
  [option {:keys [label value index]}]
  (tb/assoc-when
   {:db/id (td/id option)}
   :service-field-option/value value
   :service-field-option/label label
   :service-field-option/index index))


(defn remove-indexed-subentity
  "Removes an entity with an index attribute and preserves other indices"
  [entity subentity-attr subentity index-attr]
  (let [subentities (->> (subentity-attr entity)
                         (sort-by index-attr)
                         (remove #(= (td/id %) (td/id subentity))))]
    (conj
     (map-indexed
      (fn [i f]
        [:db/add (td/id f) index-attr i])
      subentities)
     [:db.fn/retractEntity (td/id subentity)])))


(defn remove-field
  "Remove `field` from `service`, keeping index attrs in order."
  [service field]
  (remove-indexed-subentity service :service/fields field :service-field/index))


(defn remove-option
  "Remove `option` from a dropdown service `field`, keeping index attrs in order"
  [field option]
  (remove-indexed-subentity field :service-field/options option :service-field-option/index))


(comment

  (d/transact user/conn [(create "code,code" "name" "this is a desc" {:fields [(create-field "something" :time)
                                                         (create-field "something2" :date)
                                                         (create-field "something3" :text)]})])

  (d/touch (d/entity (d/db user/conn) [:service/code "code,code"]))

  (d/transact user/conn (remove-field (d/entity (d/db user/conn) [:service/code "code,code"]) (d/entity (d/db user/conn) 17592186045799)))

  )
