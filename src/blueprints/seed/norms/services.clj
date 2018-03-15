(ns blueprints.seed.norms.services
  (:require [datomic.api :as d]
            [blueprints.models.service :as service]))

(defn- ^{:added "1.5.0"} add-initial-services [part]
  [{:db/id          (d/tempid part)
    :service/code   "storage,bin,small"
    :service/name   "Small Storage Bin"
    :service/desc   "An 18 gallon bin for your belongings."
    :service/price  6.0
    :service/billed :service.billed/monthly}
   {:db/id          (d/tempid part)
    :service/code   "storage,bin,large"
    :service/name   "Large Storage Bin"
    :service/desc   "A 30 gallon bin for your belongings."
    :service/price  8.0
    :service/billed :service.billed/monthly}
   {:db/id          (d/tempid part)
    :service/code   "storage,misc"
    :service/name   "Other Storage"
    :service/desc   "Storage for large/oddly-shaped items that don't fit in bins (e.g. musical instruments, sports equipment, etc)."
    :service/billed :service.billed/monthly}
   {:db/id          (d/tempid part)
    :service/code   "moving,move-in"
    :service/name   "Move-in Assistance"
    :service/desc   "Movers to help move your things into your room on move-in day."
    :service/price  50.0
    :service/billed :service.billed/once}
   {:db/id          (d/tempid part)
    :service/code   "customize,furniture,quote"
    :service/name   "Furniture/Layout Customization"
    :service/desc   "request for a quote on arbitrary furniture-related modifications"
    :service/billed :service.billed/once}
   {:db/id          (d/tempid part)
    :service/code   "customize,room,quote"
    :service/name   "Room Personalization"
    :service/desc   "request for a quote on arbitrary room modifications"
    :service/billed :service.billed/once}
   {:db/id          (d/tempid part)
    :service/code   "cleaning,weekly"
    :service/name   "Weekly Room Cleaning"
    :service/desc   "Have your room dusted, vacuumed and all surfaces cleaned on a weekly basis to keep things fresh and tidy."
    :service/price  100.0
    :service/billed :service.billed/monthly}
   {:db/id          (d/tempid part)
    :service/code   "laundry,weekly"
    :service/name   "Complete Laundry Service &amp; Delivery"
    :service/desc   "Give us your dirty laundry, we'll bring it back so fresh and so clean. Whether you need dry-cleaning or wash and fold, we'll keep your shirts pressed and your jackets stain-free with our next-day laundry service. The membership price includes pickup and delivery&mdash;individual item pricing will be billed at the end of the month."
    :service/price  40.0
    :service/billed :service.billed/monthly}
   {:db/id              (d/tempid part)
    :service/code       "kitchenette,coffee/tea,bundle"
    :service/name       "Coffee or Tea Equipment Bundle"
    :service/desc       "Includes coffee/tea maker of your choice, electric water kettle, mugs and tray."
    :service/properties [[:property/internal-name "2072mission"]]
    :service/price      75.0
    :service/billed     :service.billed/once
    :service/variants   [{:svc-variant/name "Chemex"}
                         {:svc-variant/name "French Press"}
                         {:svc-variant/name "Tea Infuser Pot"}]}
   {:db/id              (d/tempid part)
    :service/code       "kitchenette,microwave"
    :service/name       "Microwave"
    :service/desc       "A microwave for your kitchenette, rented for the duration of your membership."
    :service/price      50.0
    :service/rental     true
    :service/properties [[:property/internal-name "2072mission"]]
    :service/billed     :service.billed/once}
   {:db/id          (d/tempid part)
    :service/code   "mirror,full-length"
    :service/name   "Full-length Mirror"
    :service/desc   "Rent a full-length mirror for the duration of your membership."
    :service/rental true
    :service/price  25.0
    :service/billed :service.billed/once}
   {:db/id          (d/tempid part)
    :service/code   "tv,wall,32inch"
    :service/name   "Wall-mounted 32\" TV"
    :service/desc   "Includes the TV, wall mount and installation."
    :service/price  315.0
    :service/billed :service.billed/once}
   {:db/id          (d/tempid part)
    :service/code   "apple-tv"
    :service/name   "Apple TV"
    :service/desc   "Apple TV and installation."
    :service/price  155.0
    :service/billed :service.billed/once}
   {:db/id          (d/tempid part)
    :service/code   "box-fan"
    :service/name   "Box Fan"
    :service/desc   "A box fan for warm summer days."
    :service/price  25.0
    :service/rental true
    :service/billed :service.billed/once}
   {:db/id          (d/tempid part)
    :service/code   "white-noise-machine"
    :service/name   "White Noise Machine"
    :service/desc   "Tune out the city while you sleep."
    :service/price  50.0
    :service/rental true
    :service/billed :service.billed/once}
   {:db/id            (d/tempid part)
    :service/code     "plants,planter"
    :service/name     "Planter of Plants"
    :service/desc     "Add a little life to your room."
    :service/billed   :service.billed/once
    :service/variants [{:svc-variant/name  "small"
                        :svc-variant/price 25.0}
                       {:svc-variant/name  "medium"
                        :svc-variant/price 40.0}
                       {:svc-variant/name  "hanging"
                        :svc-variant/price 40.0}]}])




(defn- ^{:added "2.3.0"} update-services [part]
  ;; add - dog boarding
  [{:db/id                 (d/tempid part)
    :service/code          "pets,dog,boarding"
    :service/name          "Dog boarding"
    :service/name-internal "Dog boarding"
    :service/desc          "Board your dog with Starcity while you're away"
    :service/desc-internal "Doggie stays with us. Price is billed per night."
    :service/billed        :service.billed/once
    :service/catalogs      [:pets]
    :service/variants      [{:svc-variant/name  "small"
                             :svc-variant/price 45.0}
                            {:svc-variant/name  "medium"
                             :svc-variant/price 45.0}]
    :service/properties    [[:property/code "2072mission"]
                            [:property/code "52gilbert"]]
    :service/fields        [{:service-field/index 0
                             :service-field/type  :service-field.type/date
                             :service-field/label "When do we start boarding?"}
                            {:service-field/index 1
                             :service-field/type  :service-field.type/date
                             :service-field/label "When will you pick up your pup?"}
                            {:service-field/index 2
                             :service-field/type  :service-field.type/text
                             :service-field/label "Any additional instructions?"}]}

   ;; add - dog walking
   {:db/id                 (d/tempid part)
    :service/code          "pets,dog,walking,single"
    :service/name          "Dog Walking - Single"
    :service/name-internal "Dog Walking - Single"
    :service/desc          "One walk for your furry family member."
    :service/billed        :service.billed/once
    :service/catalogs      [:pets]
    :service/variants      [{:svc-variant/name  "small"
                             :svc-variant/price 15.0}
                            {:svc-variant/name  "medium"
                             :svc-variant/price 15.0}]
    :service/properties    [[:property/code "2072mission"]
                            [:property/code "52gilbert"]]}

   ;; add - dog walking subscription
   {:db/id                 (d/tempid part)
    :service/code          "pets,dogs,walking,subscription"
    :service/name          "Dog Walking - Subscription"
    :service/name-internal "Dog Walking - Subscription"
    :service/desc          "Daily walks for your furry family member"
    :service/catalogs      [:pets :subscriptions]
    :service/billed        :service.billed/monthly
    :service/properties    [[:property/code "2072mission"]
                            [:property/code "52gilbert"]]
    :service/fields        [{:service-field/index 0
                             :service-field/type  :service-field.type/time ;; how do we represent broader categories of time? e.g, morning, afternoon, evening?
                             :service-field/label "When should we take your pup for a walk?"}
                            {:service-field/index 1
                             :service-field/type  :service-field.type/text
                             :service-field/label "Any additional instructions?"}]}

   ;; add - single laundry service
   (service/create "drycleaning,single"
                   "Dry Cleaning Pickup and Delivery"
                   "Starcity will take care of picking up your dry cleaning, getting it cleaned, and returning it to you. Does not include the cost of dry cleaning itself."
                   {:catalogs   [:laundry]
                    :properties [[:property/code "2072mission"]
                                 [:property/code "52gilbert"]]
                    :fields     [(service/create-field "Any additional instructions?" :text)]})

   ;; modify - weekly laundry service
   {:db/id                 [:service/code "laundry,weekly"]
    :service/name-internal "Complete Laundry Service and Delivery"
    :service/catalogs      [:laundry :subscriptions]
    :service/fields        [{:service-field/index 0
                             :service-field/type  :service-field.type/text
                             :service-field/label "Any additional instructions?"}]}

   ;; modify - small bins
   {:db/id                 [:service/code "storage,bin,small"]
    :service/name-internal "Storage Bin - Small"
    :service/catalogs      [:storage :subscriptions]
    :service/fields        [{:service-field/index 0
                             :service-field/type  :service-field.type/text
                             :service-field/label "Any additional instructions?"}]}

   ;; modify - large bins
   {:db/id                 [:service/code "storage,bin,large"]
    :service/name-internal "Storage Bin - Large"
    :service/catalogs      [:storage :subscriptions]
    :service/fields        [{:service-field/index 0
                             :service-field/type  :service-field.type/text
                             :service-field/label "Any additional instructions?"}]}

   ;; modify - misc storage
   {:db/id                 [:service/code "storage,misc"]
    :service/name-internal "Other Storage - for odd objects"
    :service/catalogs      [:storage :subscriptions]
    :service/fields        [{:service-field/index 0
                             :service-field/type  :service-field.type/text
                             :service-field/label "Any additional instructions?"}]}

   ;; add - single room cleaning
   {:db/id                 (d/tempid part)
    :service/code          "cleaning,room,single"
    :service/name          "Single Room Cleaning"
    :service/name-internal "Single Room Cleaning"
    :service/desc          "Have your room dusted, vacuumed, all surfaces cleaned and your linens changed."
    :service/catalogs      [:cleaning]
    :service/price         40.0
    :service/billed        :service.billed/once
    :service/fields        [{:service-field/index 0
                             :service-field/type  :service-field.type/date
                             :service-field/label "When would you like your room cleaned?"}
                            {:service-field/index 1
                             :service-field/type  :service-field.type/text
                             :service-field/label "Any additional instructions?"}]}

   ;; add - single linen change
   {:db/id                 (d/tempid part)
    :service/code          "cleaning,linen,single"
    :service/name          "Single Bed Linen Change"
    :service/name-internal "Single Bed Linen Change"
    :service/desc          "Have us change your sheets for fresh set."
    :service/billed        :service.billed/once
    :service/catalogs      [:cleaning]
    :service/price         20.0
    :service/fields        [{:service-field/index 0
                             :service-field/type  :service-field.type/text
                             :service-field/label "Any additional instructions?"}]}

   ;; modify - weekly room cleaning
   {:db/id                 [:service/code "cleaning,weekly"]
    :service/name-internal "Weekly Room Cleaning"
    :service/catalogs      [:cleaning :subscriptions]
    :service/fields        [{:service-field/index 0
                             :service-field/type  :service-field.type/text
                             :service-field/label "Any additional instructions?"}]}

   ;; add - extra keyfob
   {:db/id                 (d/tempid part)
    :service/code          "keyfob"
    :service/name          "Extra Keyfob"
    :service/name-internal "Extra Keyfob"
    :service/desc          "Lost keyfob? No prob!"
    :service/billed        :service.billed/once
    :service/properties    [[:property/code "2072mission"]
                            [:property/code "52gilbert"]]
    :service/catalogs      [:misc]
    :service/fields        [{:service-field/index 0
                             :service-field/type  :service-field.type/number
                             :service-field/label "How many?"}]}

   ;; modify - move-in assistance
   {:db/id                 [:service/code "moving,move-in"]
    :service/name-internal "Move-in assistance"
    :service/catalogs      [:misc]
    :service/fields        [{:service-field/index 0
                             :service-field/type  :service-field.type/text
                             :service-field/label "Any additional instructions?"}]}
   ;; add - make a wish
   {:db/id                 (d/tempid part)
    :service/code          "wish"
    :service/name          "Wish"
    :service/name-internal "Wish"
    :service/desc          "Wish upon a star."
    :service/billed        :service.billed/once
    :service/catalogs      [:misc]
    :service/fields        [{:service-field/index 0
                             :service-field/type  :service-field.type/text
                             :service-field/label "Tell us about your wish, and we'll get back to you within 24 hours."}]}

   ;; removals
   [:db.fn/retractEntity [:service/code "customize,furniture,quote"]]
   [:db.fn/retractEntity [:service/code "customize,room,quote"]]
   [:db.fn/retractEntity [:service/code "kitchenette,coffe/tea,bundle"]]
   [:db.fn/retractEntity [:service/code "kitchenette,microwave"]]
   [:db.fn/retractEntity [:service/code "mirror,full-length"]]
   [:db.fn/retractEntity [:service/code "tv,wall,32inch"]]
   [:db.fn/retractEntity [:service/code "apple-tv"]]
   [:db.fn/retractEntity [:service/code "box-fan"]]
   [:db.fn/retractEntity [:service/code "white-noise-machine"]]
   [:db.fn/retractEntity [:service/code "plants,planter"]]])


(defn norms [conn part]
  {:blueprints.seed/add-initial-services
   {:txes [(add-initial-services part)]}

   :blueprints.seed/update-services-03012018
   {:txes [(update-services part)]
    :requires [:blueprints.seed/add-initial-services]}})
