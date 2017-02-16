(ns starcity-db.schema.msg
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

      [key :keyword :index
       "App-defined keyword to dispatch upon."]

      [params :bytes
       "Serialized parameters for the msg handler."]))]))

(defn norms [part]
  {:schema.msg/add-msg-schema
   {:txes [schema]}})
