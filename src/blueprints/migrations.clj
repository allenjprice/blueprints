(ns blueprints.migrations
  (:require [datomic.api :as d]
            [io.rkn.conformity :as c]))

(defn norms [conn part]
  {})

(defn conform [conn part]
  (c/ensure-conforms conn (norms conn part)))
