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

    ;; A note can be treated as a ticket by giving it a status and other optional attributes.
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

(defn norms [part]
  {:schema.note/add-schema-02242017
   {:txes [schema (ticket-statuses part)]}})
