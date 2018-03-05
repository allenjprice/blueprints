(ns blueprints.seed.norms.catalogues
  (:require [datomic.api :as d]))

(defn- by-code [db code]
  (d/q '[:find ?e .
         :in $ ?code
         :where [?e :service/code ?code]]
       db code))

(defn add-initial-catalogues [conn part]
  (let [db (d/db conn)]
    [;; Storage Options, SoMa
     {:db/id                (d/tempid part)
      :catalogue/name       "Storage Options, 52 Gilbert"
      :catalogue/code       :storage
      :catalogue/properties [[:property/internal-name "52gilbert"]]
      :catalogue/items
      [{:cat-item/index   0
        :cat-item/service (by-code db "storage,bin,small")
        :cat-item/fields
        {:cat-field/label "How many?"
         :cat-field/type  :cat-field.type/quantity
         :cat-field/min   1
         :cat-field/max   10
         :cat-field/step  1.0}}
       {:cat-item/index   1
        :cat-item/service (by-code db "storage,bin,large")
        :cat-item/fields
        {:cat-field/label "How many?"
         :cat-field/type  :cat-field.type/quantity
         :cat-field/min   1
         :cat-field/max   10
         :cat-field/step  1.0}}
       {:cat-item/index   2
        :cat-item/service (by-code db "storage,misc")
        :cat-item/fields
        {:cat-field/label "Tell us about what you'd like to store and we'll get back to you with a quote within 24 hours."
         :cat-field/type  :cat-field.type/desc}}]}
     ;; Storage Options, Mission
     {:db/id                (d/tempid part)
      :catalogue/name       "Storage Options, 2072 Mission"
      :catalogue/code       :storage
      :catalogue/properties [[:property/internal-name "2072mission"]]
      :catalogue/items
      [{:cat-item/index   0
        :cat-item/service (by-code db "storage,bin,small")
        :cat-item/fields
        {:cat-field/label "How many?"
         :cat-field/type  :cat-field.type/quantity
         :cat-field/min   1
         :cat-field/max   10
         :cat-field/step  1.0}}
       {:cat-item/index   1
        :cat-item/service (by-code db "storage,bin,large")
        :cat-item/fields
        {:cat-field/label "How many?"
         :cat-field/type  :cat-field.type/quantity
         :cat-field/min   1
         :cat-field/max   10
         :cat-field/step  1.0}}
       {:cat-item/index   2
        :cat-item/service (by-code db "storage,misc")
        :cat-item/fields
        {:cat-field/label "Tell us about what you'd like to store and we'll get back to you with a quote within 24 hours."
         :cat-field/type  :cat-field.type/desc}}]}
     ;; Room Customization
     {:db/id          (d/tempid part)
      :catalogue/name "Room Customization"
      :catalogue/code :room/customize
      :catalogue/items
      [{:cat-item/index   0
        :cat-item/service (by-code db "customize,furniture,quote")
        :cat-item/desc    "Would you like to customize your room layout, furniture arrangement, or request a specific furniture item?"
        :cat-item/fields
        {:cat-field/label "Please leave a detailed description about what you'd like to accomplish and we'll get you a quote within 24 hours."
         :cat-field/type  :cat-field.type/desc}}
       {:cat-item/index   1
        :cat-item/service (by-code db "customize,room,quote")
        :cat-item/desc    "Your room comes fully furnished and decorated. Please let us know if you would like to personalize the room to reflect your personality and taste. From painting accent colors, and curating art from local artists to hanging/framing your own art, we've got you covered."
        :cat-item/fields
        {:cat-field/label "Please leave a detailed description about what you'd like to accomplish and we'll get you a quote within 24 hours."
         :cat-field/type  :cat-field.type/desc}}]}
     ;; Cleaning & Laundry
     {:db/id          (d/tempid part)
      :catalogue/name "Cleaning and Laundry"
      :catalogue/code :cleaning+laundry
      :catalogue/items
      [{:cat-item/index   0
        :cat-item/service (by-code db "cleaning,weekly")}
       {:cat-item/index   1
        :cat-item/service (by-code db "laundry,weekly")}]}
     ;; Room Upgrades, Mission
     {:db/id                (d/tempid part)
      :catalogue/name       "Room Upgrades"
      :catalogue/properties [[:property/internal-name "2072mission"]]
      :catalogue/code       :room/upgrades
      :catalogue/items
      (map-indexed
       #(assoc %2 :cat-item/index %1)
       [{:cat-item/service (by-code db "kitchenette,coffee/tea,bundle")}
        {:cat-item/service (by-code db "kitchenette,microwave")}
        {:cat-item/service (by-code db "mirror,full-length")}
        {:cat-item/service (by-code db "tv,wall,32inch")}
        {:cat-item/service (by-code db "apple-tv")}
        {:cat-item/service (by-code db "box-fan")}
        {:cat-item/service (by-code db "white-noise-machine")}
        {:cat-item/name    "Planter of Plants"
         :cat-item/desc    "Add a little life to your room."
         :cat-item/service (by-code db "plants,planter")}])}
     ;; Room Upgrades, SoMa
     {:db/id                (d/tempid part)
      :catalogue/name       "Room Upgrades"
      :catalogue/properties [[:property/internal-name "52gilbert"]]
      :catalogue/code       :room/upgrades
      :catalogue/items
      [{:cat-item/index   0
        :cat-item/service (by-code db "mirror,full-length")}
       {:cat-item/index   1
        :cat-item/service (by-code db "tv,wall,32inch")}
       {:cat-item/index   2
        :cat-item/service (by-code db "apple-tv")}
       {:cat-item/index   3
        :cat-item/service (by-code db "box-fan")}
       {:cat-item/index   4
        :cat-item/service (by-code db "white-noise-machine")}
       {:cat-item/index   5
        :cat-item/name    "Planter of Plants"
        :cat-item/desc    "Add a little life to your room."
        :cat-item/service (by-code db "plants,planter")}]}]))

(defn norms [conn part]
  (merge
   {:blueprints.seed/add-initial-catalogues
    {:txes [(add-initial-catalogues conn part)]}}))
