(ns starcity-db.core
  (:require [io.rkn.conformity :as c]
            [starcity-db.schema :as schema]
            [datomic.api :as d]))

(def part
  "The name of our partition."
  :db.part/starcity)

(defn conform-schema [conn]
  (c/ensure-conforms conn (schema/partition-norms part))
  (c/ensure-conforms conn (schema/norms part)))
