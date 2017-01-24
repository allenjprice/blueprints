(ns starcity-db.schema.news
  (:require [datomic-schema.schema :as s]))

(def ^{:added "1.2.0"} schema
  (s/generate-schema
   [(s/schema
     news
     (s/fields
      [title :string :fulltext
       "The title of the news item."]

      [content :string :fulltext
       "The content of the news item."]

      [account :ref :index
       "The account that this news item belongs to."]

      [avatar :ref :index
       "An avatar to display on the news item."]

      [dismissed :boolean :index
       "Has this news item been dismissed?"]

      [action :keyword :index
       "Arbitrary keyword that can be used by clients to identify what to do with this news item."]

      [created-at :instant :index
       "Time at which this entity was created -- used for sorting."]))]))

(defn norms [part]
  {:schema.news/add-news-schema
   {:txes [schema]}})
