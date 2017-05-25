(ns blueprints.schema.msg
  "The `msg` is much like `cmd`, but where cmds are used to convey things that
  /should/ happen, `msg`s are used to convey that something /did/ happen, and
  usually trigger some sort of side-effect.

  Note that `msg` do not have a status -- this is for two reasons:

  - The meaning of a `msg` loses significance with time, and
  - A `msg` is often processed by multiple different handlers to accomplish
  different things -- a msg would have to be able to represent N /statuses/,
  where N is the number of handlers. This seems like overkill."
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]))

(def ^{:added "1.3.0"} schema
  (s/generate-schema
   [(s/schema
     msg
     (s/fields
      [uuid :uuid :unique-identity
       "SQUUID to identify this msg."]

      [key :keyword
       "App-defined keyword to dispatch upon."]

      [params :bytes
       "Serialized parameters for the msg handler."]))]))

(def ^{:added "1.6.0"} index-schema-attrs
  [{:db/id               :msg/key
    :db/index            true
    :db.alter/_attribute :db.part/db}])

(def ^{:added "1.6.0"} add-data-attr
  "The choice to use `:bytes` as the `:db.valueType` for `:msg/params`has proven
  to be a poor one. Reasons:

  1. Requires an external library to use (nippy).
  2. Params aren't human-readable without library.
  3. (1) and (2) together make the use of database functions difficult.

  This schema change adds one more attribute that uses strings as the
  serialization format, thus solving above issues."
  (concat
   (s/generate-schema
    [(s/schema
      msg
      (s/fields
       [data :string
        "The serialized data for the message handler."]))])
   [{:db/id  :msg/params
     :db/doc "DEPRECATED 1.6.0: Prefer `:msg/data`."}]))

(defn- ^{:added "1.6.0"} add-create-msg [part]
  [{:db/id    (d/tempid part)
    :db/ident :db.msg/create
    :db/doc   "Create a new msg."
    :db/fn
    (datomic.function/construct
     {:lang     "clojure"
      :params   '[db key data]
      :requires '[[datomic.api :as d]]
      :code     '[(merge
                   {:db/id    (d/tempid :db.part/starcity)
                    :msg/uuid (d/squuid)
                    :msg/key  key}
                   (when-not (empty? data) {:msg/data (pr-str data)}))]})}])

(defn norms [part]
  {:schema.msg/add-msg-schema
   {:txes [schema]}

   :schema.msg/improvements-05252017
   {:txes     [index-schema-attrs add-data-attr]
    :requires [:schema.msg/add-msg-schema]}

   :schema.msg/add-create-msg-05252017
   {:txes [(add-create-msg part)]}})
