(ns blueprints.schema.income-file
  (:require [datomic-schema.schema :as s]))

(def ^{:added "1.0.x"} schema
  (s/generate-schema
   [(s/schema
     income-file
     (s/fields
      [account :ref
       "The account that this income file belongs to."]

      ;; TODO: The following three attributes could be better generalized to
      ;; `:file/#{content-type size path}`.
      [content-type :string
       "The type of content that this file holds."]
      [size :long
       "The size of this file in bytes."]
      [path :string
       "The path to this file on the filesystem."]))]))

(defn norms [part]
  {:starcity/add-income-files-schema-8-3-16
   {:txes [schema]}})
