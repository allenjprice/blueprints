(ns blueprints.models.note
  (:refer-clojure :exclude [update])
  (:require [blueprints.models.account :as account]
            [clojure.spec.alpha :as s]
            [datomic.api :as d]
            [toolbelt.core :as tb]
            [toolbelt.datomic :as td]))


;; =============================================================================
;; Specs
;; =============================================================================


(s/def :note/author (s/or :id integer? :entity td/entity?))           ; for return values
(s/def :note/subject string?)
(s/def :note/content string?)
(s/def :note/children (s/* td/entity?))
(s/def :note/tags (s/* td/entity?))

(s/def :ticket/status #{:ticket.status/open :ticket.status/closed})
(s/def :ticket/assigned-to (s/or :id integer? :entity td/entity?))    ; for return values


;; =============================================================================
;; Selectors
;; =============================================================================


(def uuid
  "The unique UUID that identifies this note."
  :note/uuid)

(s/fdef uuid
        :args (s/cat :note td/entity?)
        :ret uuid?)


(def author
  "The account that created this note."
  :note/author)

(s/fdef author
        :args (s/cat :note td/entity?)
        :ret td/entity?)


(def subject
  "The subject (title) of this note."
  :note/subject)

(s/fdef subject
        :args (s/cat :note td/entity?)
        :ret :note/subject)


(def content
  "The content of this note."
  :note/content)

(s/fdef content
        :args (s/cat :note td/entity?)
        :ret :note/content)


(def children
  "The children of this note, i.e. comments."
  :note/children)

(s/fdef children
        :args (s/cat :note td/entity?)
        :ret :note/children)


(def tags
  "Any tags associated with this note."
  :note/tags)

(s/fdef tags
        :args (s/cat :note td/entity?)
        :ret :note/tags)


(def status
  "The status of this note, if it's being treated as a ticket."
  :ticket/status)

(s/fdef status
        :args (s/cat :note td/entity?)
        :ret (s/or :nothing nil? :status :ticket/status))


(def assigned-to
  "The account that this ticket is assigned to."
  :ticket/assigned-to)

(s/fdef assigned-to
        :args (s/cat :note td/entity?)
        :ret (s/or :nothing nil? :account td/entity?))


(def account
  "The account that this note pertains to, if any."
  :account/_notes)

(s/fdef account
        :args (s/cat :note td/entity?)
        :ret (s/or :nothing nil? :account td/entity?))


(def parent
  "The parent note to this note, if any."
  :note/_children)

(s/fdef parent
        :args (s/cat :note td/entity?)
        :ret (s/or :nothing nil? :note td/entity?))


;; =============================================================================
;; Predicates
;; =============================================================================


(def ticket?
  "Is this note a ticket?"
  (comp boolean status))


(s/fdef ticket?
        :args (s/cat :note td/entity?)
        :ret boolean?)


;; =============================================================================
;; Queries
;; =============================================================================


(defn by-uuid [db uuid]
  (d/entity db [:note/uuid uuid]))

(s/fdef by-uuid
        :args (s/cat :db td/db? :uuid uuid?)
        :ret (s/or :nothing nil? :entity td/entity?))


;; =============================================================================
;; Transactions
;; =============================================================================


(defn create
  "Create a new note, optionally specifying whether or not this note should be a
  ticket (`ticket?`), and if this note should be `assigned-to` someone. Passing
  an account for `assigned-to` will result in this note being created as a
  ticket."
  [subject content & {:keys [author ticket? assigned-to]}]
  (let [status (when (or ticket? assigned-to) :ticket.status/open)]
    (tb/assoc-when
     {:db/id        (d/tempid :db.part/starcity)
      :note/subject subject
      :note/content content
      :note/uuid    (d/squuid)}
     :note/author  (:db/id author)
     :ticket/status status
     :ticket/assigned-to (:db/id assigned-to))))

(s/def ::author td/entity?)
(s/def ::assigned-to td/entity?)
(s/fdef create
        :args (s/cat :subject :note/subject
                     :content :note/content
                     :opts (s/keys* :opt-un [::ticket? ::assigned-to ::author]))
        :ret map?)


(defn update
  "Update `note`'s `subject` and/or `content`."
  [note & {:keys [subject content]}]
  (tb/assoc-when
   {:db/id (:db/id note)}
   :note/subject subject
   :note/content content))

(s/fdef update
        :args (s/cat :note td/entity?
                     :opts (s/keys* :opt-un [:note/subject
                                             :note/content]))
        :ret map?)


;; =============================================================================
;; Tickets


(defn assign-to
  "Assign `note` to `account`."
  [note account]
  {:db/id              (:db/id note)
   :ticket/assigned-to (:db/id account)})

(s/fdef assign-to
        :args (s/cat :note td/entity? :account td/entity?)
        :ret map?)


(defn toggle-status
  "Change `note`'s status from open to closed and vice-versa."
  [note]
  (assert (ticket? note) "Only tickets can have their status toggled.")
  (let [s (status note)]
    {:db/id         (:db/id note)
     :ticket/status (if (= s :ticket.status/open)
                      :ticket.status/closed
                      :ticket.status/open)}))


;; =============================================================================
;; Tags


(defn add-tag
  "Add `tag` to `note` idempotently."
  [note tag]
  [:db/add (:db/id note) :note/tags (:db/id tag)])

(s/fdef add-tag
        :args (s/cat :note td/entity? :tag td/entity?)
        :ret vector?)


(defn remove-tag
  "Remove `tag` from `note`."
  [note tag]
  [:db/retract (:db/id note) :note/tags (:db/id tag)])

(s/fdef remove-tag
        :args (s/cat :note td/entity? :tag td/entity?)
        :ret vector?)


;; =============================================================================
;; Comments


(defn create-comment
  "Create a comment given an `author` and the comment `content`."
  [author content]
  {:note/author  (:db/id author)
   :note/content content
   :note/uuid    (d/squuid)})

(s/fdef comment
        :args (s/cat :author td/entity? :content string?)
        :ret map?)


(defn add-comment
  "Add `comment` to `note`. A `comment` is a note with only `note/content` and
  `note/author`."
  [note comment]
  {:db/id         (:db/id note)
   :note/children comment})

(s/fdef add-comment
        :args (s/cat :note td/entity?
                     :comment (s/keys :req [:note/content :note/author]))
        :ret map?)


;; =============================================================================
;; Transformations
;; =============================================================================


(defn- clientize-children
  [db children]
  (->> (map
        (fn [child-note]
          (-> (select-keys child-note [:db/id :note/author :note/content])
              (clojure.core/update :note/author account/clientize)
              (assoc :note/created-at (td/created-at db child-note))))
        children)
       (sort-by :note/created-at)))


(defn- referenced-by
  "The entity id that references this note.

  At the moment, this can only be an `account`, but will presumably be other
  types of entities int he future."
  [note]
  (-> note account :db/id))


(defn clientize
  "Produce a representation for `note` suitable for delivery to a web client.
  Can optionally provide a `created-at` date (e.g. if obtained through a query)
  -- otherwise the `created-at` date will be obtained using the Datomic history
  api."
  ([db note]
   (clientize db note (td/created-at db note)))
  ([db note created-at]
   (let [m (select-keys note [:db/id :note/author :note/subject :note/content
                              :note/children :ticket/status :ticket/assigned-to])]
     (-> (tb/transform-when-key-exists m
           {:note/author        account/clientize
            :note/children      (partial clientize-children db)
            :ticket/assigned-to account/clientize})
         (assoc :note/created-at created-at
                :note/referenced-by (referenced-by note))))))

(s/fdef clientize
        :args (s/cat :db td/db?
                     :note td/entity?
                     :created-at (s/? inst?))
        :ret map?)
