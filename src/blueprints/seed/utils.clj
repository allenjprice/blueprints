(ns blueprints.seed.utils
  (:require [datomic.api :as d]
            [clj-time.core :as t]
            [clj-time.coerce :as c]))


(defn tempid [& [n]]
  (if (some? n)
    (d/tempid :db.part/starcity n)
    (d/tempid :db.part/starcity)))


(defn weeks-from-now [n]
  (c/to-date (t/plus (t/now) (t/weeks n))))


(defn days-from-now [n]
  (c/to-date (t/plus (t/now) (t/days n))))


(defn end-of-month []
  (c/to-date (t/last-day-of-the-month (t/now))))


(defn now []
  (java.util.Date.))
