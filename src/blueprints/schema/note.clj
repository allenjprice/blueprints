(ns blueprints.schema.note
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]))

(def ^{:added "1.4.0"} schema
  (s/generate-schema
   [(s/schema
     note
     (s/fields
      [uuid :uuid :unique-identity
       "Unique identifier for this note."]

      [author :ref :index
       "The author (account) of this note."]

      [subject :string :fulltext
       "The subject line (i.e. title) of this note."]

      [content :string :fulltext
       "The content of this note."]

      [children :ref :many :index :component
       "Child notes of this note. Used to implement threads/comments."]

      [tags :ref :many :index
       "Tags used to categorize this note."]))

    ;; a note can be treated as a ticket by giving it a status and other
    ;; optional attributes
    (s/schema
     ticket
     (s/fields
      [status :ref :index
       "The status of this ticket."]

      [assigned-to :ref :index
       "The account that this ticket is assigned to."]))]))


(defn- ^{:added "1.4.0"} ticket-statuses [part]
  [{:db/id    (d/tempid part)
    :db/ident :ticket.status/open}
   {:db/id    (d/tempid part)
    :db/ident :ticket.status/closed}])


(def ^{:added "1.14.0" :private true} index-attributes
  [{:db/id               :note/author
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :note/children
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :note/tags
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :ticket/status
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :ticket/assigned-to
    :db/index            true
    :db.alter/_attribute :db.part/db}])


(def ^{:added "1.14.0" :private true} add-ref
  (s/generate-schema
   [(s/schema
     note
     (s/fields
      [ref :ref :indexed
       "Reference to another entity--assumed to be the topic of this note."]))]))


(def ^{:added "2.5.5"} change-references
  "Change `:note/ref` attribute to have cardinality many, and rename to reflect plurality"
  [{:db/id               :note/ref
    :db/ident            :note/refs
    :db/cardinality      :db.cardinality/many
    :db.alter/_attribute :db.part/db}])


(defn norms [part]
  {:schema.note/add-schema-02242017
   {:txes [schema (ticket-statuses part)]}

   :schema.note/index-attrs-and-add-ref-10222017
   {:txes     [index-attributes add-ref]
    :requires [:schema.note/add-schema-02242017]}

   :schema.note/change-references-05152018
   {:txes     [change-references]
    :requires [:schema.note/index-attrs-and-add-ref-10222017]}})
