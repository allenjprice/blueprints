(ns blueprints.seed.norms.services
  (:require [datomic.api :as d]
            [blueprints.models.service :as service]
            [taoensso.timbre :as timbre]))

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
    :service/desc          "Board your dog with Starcity while you're away."
    :service/desc-internal "Doggie stays with us. Price is billed per night."
    :service/billed        :service.billed/once
    :service/catalogs      [:pets]
    :service/price         45.0
    :service/cost          0.0
    :service/active        true
    :service/properties    [[:property/code "2072mission"]
                            [:property/code "52gilbert"]]
    :service/fields        [{:service-field/index    0
                             :service-field/type     :service-field.type/date
                             :service-field/required true
                             :service-field/label    "When do we start boarding?"}
                            {:service-field/index    1
                             :service-field/required true
                             :service-field/type     :service-field.type/date
                             :service-field/label    "When will you pick up your pup?"}
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
    :service/active        true
    :service/cost          0.0
    :service/price         30.0
    :service/catalogs      [:pets]
    :service/properties    [[:property/code "2072mission"]
                            [:property/code "52gilbert"]]
    :service/fields        [{:service-field/index    0
                             :service-field/type     :service-field.type/dropdown
                             :service-field/required true
                             :service-field/label    "When should we take your pup for a walk?"
                             :service-field/options  [{:service-field-option/index 0
                                                       :service-field-option/label "Morning"
                                                       :service-field-option/value "Morning"}
                                                      {:service-field-option/index 1
                                                       :service-field-option/label "Afternoon"
                                                       :service-field-option/value "Afternoon"}
                                                      {:service-field-option/index 2
                                                       :service-field-option/label "Evening"
                                                       :service-field-option/value "Evening"}]}
                            {:service-field/index 1
                             :service-field/type  :service-field.type/text
                             :service-field/label "Any additional instructions?"}]}

   ;; add - dog walking subscription
   {:db/id                 (d/tempid part)
    :service/code          "pets,dogs,walking,subscription"
    :service/name          "Dog Walking - Subscription "
    :service/name-internal "Dog Walking - Subscription"
    :service/desc          "Five walks per week, during business days, for your furry family member."
    :service/cost          0.0
    :service/price         420.0
    :service/active        true
    :service/catalogs      [:pets :subscriptions]
    :service/billed        :service.billed/monthly
    :service/properties    [[:property/code "2072mission"]
                            [:property/code "52gilbert"]]
    :service/fields        [{:service-field/index    0
                             :service-field/type     :service-field.type/dropdown
                             :service-field/required true
                             :service-field/label    "When should we take your pup for a walk?"
                             :service-field/options  [{:service-field-option/index 0
                                                       :service-field-option/label "Morning"
                                                       :service-field-option/value "Morning"}
                                                      {:service-field-option/index 1
                                                       :service-field-option/label "Afternoon"
                                                       :service-field-option/value "Afternoon"}
                                                      {:service-field-option/index 2
                                                       :service-field-option/label "Evening"
                                                       :service-field-option/value "Evening"}]}
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
                    :price      15.0
                    :active     true
                    :cost       0.0
                    :rental     false
                    :billed     :service.billed/once
                    :fields     [(service/create-field "Any additional instructions?" :text)]})

   ;; modify - weekly laundry service
   {:db/id                 [:service/code "laundry,weekly"]
    :service/name-internal "Weekly Laundry Service and Delivery - Subscription"
    :service/desc          "Give us your dirty laundry, we'll bring it back so fresh and so clean. Whether you need dry-cleaning or wash and fold, we'll keep your shirts pressed and your jackets stain-free with our next-day laundry service. The membership price includes pickup and delivery - individual item pricing will be billed at the end of the month."
    :service/active        true
    :service/properties    [[:property/code "2072mission"]
                            [:property/code "52gilbert"]]
    :service/cost          0.0
    :service/catalogs      [:laundry :subscriptions]
    :service/fields        [{:service-field/index 0
                             :service-field/type  :service-field.type/text
                             :service-field/label "Any additional instructions?"}]}

   ;; modify - small bins
   {:db/id                 [:service/code "storage,bin,small"]
    :service/name-internal "Storage Bin - Small"
    :service/catalogs      [:storage :subscriptions]
    :service/active        true
    :service/cost          0.13
    :service/properties    [[:property/code "2072mission"]
                            [:property/code "52gilbert"]]
    :service/fields        [{:service-field/index 0
                             :service-field/type  :service-field.type/text
                             :service-field/label "Any additional instructions?"}]}

   ;; modify - large bins
   {:db/id                 [:service/code "storage,bin,large"]
    :service/name-internal "Storage Bin - Large"
    :service/catalogs      [:storage :subscriptions]
    :service/active        true
    :service/properties    [[:property/code "2072mission"]
                            [:property/code "52gilbert"]]
    :service/cost          0.23
    :service/fields        [{:service-field/index 0
                             :service-field/type  :service-field.type/text
                             :service-field/label "Any additional instructions?"}]}

   ;; modify - misc storage
   {:db/id                 [:service/code "storage,misc"]
    :service/name-internal "Other Storage - for odd objects"
    :service/catalogs      [:storage :subscriptions]
    :service/active        false
    :service/fields        [{:service-field/index 0
                             :service-field/type  :service-field.type/text
                             :service-field/label "Any additional instructions?"}]}

   ;; add - single room cleaning
   {:db/id                 (d/tempid part)
    :service/code          "cleaning,room,single"
    :service/name          "Room Cleaning - Single"
    :service/name-internal "Room Cleaning - Single"
    :service/desc          "Have your furniture dusted, floor vacuumed, all surfaces cleaned, and your linens changed."
    :service/catalogs      [:cleaning]
    :service/price         40.0
    :service/cost          11.0
    :service/active        true
    :service/properties    [[:property/code "2072mission"]
                            [:property/code "52gilbert"]]
    :service/billed        :service.billed/once
    :service/fields        [{:service-field/index    0
                             :service-field/type     :service-field.type/date
                             :service-field/required true
                             :service-field/label    "When would you like your room cleaned?"}
                            {:service-field/index 1
                             :service-field/type  :service-field.type/text
                             :service-field/label "Any additional instructions?"}]}

   ;; add - single linen change
   {:db/id                 (d/tempid part)
    :service/code          "cleaning,linen,single"
    :service/name          "Bed Linen Change - Single"
    :service/name-internal "Bed Linen Change - Single"
    :service/desc          "Have us change your sheets for a fresh set."
    :service/billed        :service.billed/once
    :service/catalogs      [:cleaning]
    :service/price         20.0
    :service/cost          5.50
    :service/properties    [[:property/code "2072mission"]
                            [:property/code "52gilbert"]]
    :service/fields        [{:service-field/index    0
                             :service-field/type     :service-field.type/date
                             :service-field/required true
                             :service-field/label    "When would you like your linens changed?"}
                            {:service-field/index 1
                             :service-field/type  :service-field.type/text
                             :service-field/label "Any additional instructions?"}]}

   ;; modify - weekly room cleaning
   {:db/id                 [:service/code "cleaning,weekly"]
    :service/name-internal "Room Cleaning - Weekly"
    :service/cost          44.0
    :service/price         130.0
    :service/active        true
    :service/billed        :service.billed/monthly
    :service/properties    [[:property/code "2072mission"]
                            [:property/code "52gilbert"]]
    :service/catalogs      [:cleaning :subscriptions]
    :service/fields        [{:service-field/index 0
                             :service-field/type  :service-field.type/text
                             :service-field/label "Any additional instructions?"}]}

   ;; add - extra keyfob
   {:db/id                 (d/tempid part)
    :service/code          "keyfob"
    :service/name          "Extra Keyfob"
    :service/name-internal "Extra Keyfob"
    :service/desc          "Need a keyfob? No prob!"
    :service/billed        :service.billed/once
    :service/cost          2.0
    :service/price         20.0
    :service/active        true
    :service/properties    [[:property/code "2072mission"]]
    :service/fields        [{:service-field/index    0
                             :service-field/type     :service-field.type/number
                             :service-field/required true
                             :service-field/label    "How many?"}]}

   ;; modify - move-in assistance
   {:db/id                 [:service/code "moving,move-in"]
    :service/name-internal "Move-in assistance"
    :service/catalogs      [:misc]
    :service/active        false
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
    :service/active        false
    :service/fields        [{:service-field/index 0
                             :service-field/type  :service-field.type/text
                             :service-field/label "Tell us about your wish, and we'll get back to you within 24 hours."}]}


   ;; add - furniture installtion fee
   {:db/id          (d/tempid part)
    :service/code   "fee,installation,furniture"
    :service/name   "Installation Fee - Furniture"
    :service/desc   "One-time instalation charge added to a furniture rental."
    :service/billed :service.billed/once
    :service/active false
    :service/price  36.0
    :service/cost   30.0}


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



(defn- ^{:added "2.3.0"} add-rentals [part]
  [
   ;; add furniture rental - laptop desk
   {:db/id              (d/tempid part)
    :service/code       "furniture,rental,laptop,desk"
    :service/name       "Laptop Desk"
    :service/desc       "A small desk for your laptop. There is a one-time delivery and installation fee for this service."
    :service/active     true
    :service/properties [[:property/code "2072mission"]
                         [:property/code "52gilbert"]]
    :service/price      8.0
    :service/cost       5.6
    :service/billed     :service.billed/monthly
    :service/rental     true
    :service/catalogs   [:furniture :subscription]}


   ;; add - furniture rental - desk
   {:db/id              (d/tempid part)
    :service/code       "furniture,rental,desk"
    :service/name       "Desk"
    :service/desc       "A secretary desk for you to work on. There is a one-time delivery and installation fee for this service."
    :service/active     true
    :service/properties [[:property/code "2072mission"]
                         [:property/code "52gilbert"]]
    :service/price      20.0
    :service/cost       15.0
    :service/fees       [[:service/code "fee,installation,furniture"]]
    :service/billed     :service.billed/monthly
    :service/rental     true
    :service/catalogs   [:furniture :subscription]}


   ;; add - furniture rental small dresser
   {:db/id              (d/tempid part)
    :service/code       "furniture,rental,dresser,small"
    :service/name       "Small Dresser"
    :service/desc       "Want extra space for your clothes? We have dressers! Dimensions: 34\" tall x 18\" D x 36\" W. There is a one-time delivery and installation fee for this service."
    :service/active     true
    :service/properties [[:property/code "2072mission"]
                         [:property/code "52gilbert"]]
    :service/price      15.0
    :service/cost       12.0
    :service/fees       [[:service/code "fee,installation,furniture"]]
    :service/billed     :service.billed/monthly
    :service/rental     true
    :service/catalogs   [:furniture :subscription]}


   ;; add - furniture rental large dresser
   {:db/id              (d/tempid part)
    :service/code       "furniture,rental,dresser,large"
    :service/name       "Large Dresser"
    :service/desc       "Want extra space for your clothes? We have dressers! Dimensions: 48.75\" tall x 18\" D x 36\" W. There is a one-time delivery and installation fee for this service."
    :service/active     true
    :service/properties [[:property/code "2072mission"]
                         [:property/code "52gilbert"]]
    :service/price      15.0
    :service/cost       12.0
    :service/fees       [[:service/code "fee,installation,furniture"]]
    :service/billed     :service.billed/monthly
    :service/rental     true
    :service/catalogs   [:furniture :subscription]}


   ;; add - furniture rental bookshelf
   {:db/id              (d/tempid part)
    :service/code       "furniture,rental,bookshelf"
    :service/name       "Bookshelf"
    :service/desc       "Need some extra shelves for your books or decorative items?  Add a freestanding bookshelf. Dimensions: 76.25\" tall x 14.5\" D x 24.5\" W. There is a one-time delivery and installation fee for this service."
    :service/active     true
    :service/properties [[:property/code "2072mission"]
                         [:property/code "52gilbert"]]
    :service/price      10.0
    :service/cost       6.61
    :service/fees       [[:service/code "fee,installation,furniture"]]
    :service/billed     :service.billed/monthly
    :service/rental     true
    :service/catalogs   [:furniture :subscription]}


   ;; add - furniture rental microwave
   {:db/id              (d/tempid part)
    :service/code       "furniture,rental,microwave"
    :service/name       "Microwave"
    :service/desc       "A small microwave for your kitchenette. There is a one-time delivery and installation fee for this service."
    :service/active     true
    :service/properties [[:property/code "2072mission"]]
    :service/price      8.0
    :service/cost       4.0
    :service/fees       [[:service/code "fee,installation,furniture"]]
    :service/billed     :service.billed/monthly
    :service/rental     true
    :service/catalogs   [:furniture :subscription]}])


(defn- ^{:added "2.4.1"} add-types-fix-subscriptions [part]
  [
   ;; onboarding + subscriptions with an s + type service
   {:db/id            [:service/code "furniture,rental,laptop,desk"]
    :service/type     :service.type/service
    :service/fees     [[:service/code "fee,installation,furniture"]]
    :service/catalogs [:furniture :subscriptions :onboarding]}

   {:db/id            [:service/code "furniture,rental,desk"]
    :service/type     :service.type/service
    :service/catalogs [:furniture :subscriptions :onboarding]}

   {:db/id            [:service/code "furniture,rental,dresser,small"]
    :service/type     :service.type/service
    :service/catalogs [:furniture :subscriptions :onboarding]}

   {:db/id            [:service/code "furniture,rental,dresser,large"]
    :service/type     :service.type/service
    :service/catalogs [:furniture :subscriptions :onboarding]}

   {:db/id            [:service/code "furniture,rental,bookshelf"]
    :service/type     :service.type/service
    :service/catalogs [:furniture :subscriptions :onboarding]}

   {:db/id            [:service/code "storage,bin,small"]
    :service/type     :service.type/service
    :service/catalogs [:storage :subscriptions :onboarding]}

   {:db/id            [:service/code "furniture,rental,microwave"]
    :service/type     :service.type/service
    :service/catalogs [:furniture :subscriptions :onboarding]}

   {:db/id            [:service/code "storage,bin,large"]
    :service/type     :service.type/service
    :service/catalogs [:storage :subscriptions :onboarding]}

   {:db/id            [:service/code "pets,dogs,walking,subscription"]
    :service/type     :service.type/service
    :service/catalogs [:pets :subscriptions :onboarding]}

   {:db/id            [:service/code "storage,misc"]
    :service/type     :service.type/service
    :service/catalogs [:storage :subscriptions :onboarding]}

   {:db/id            [:service/code "moving,move-in"]
    :service/type     :service.type/service
    :service/catalogs [:onboarding]}

   {:db/id            [:service/code "cleaning,weekly"]
    :service/type     :service.type/service
    :service/catalogs [:cleaning :subscriptions :onboarding]}

   {:db/id            [:service/code "laundry,weekly"]
    :service/type     :service.type/service
    :service/catalogs [:laundry :subscriptions :onboarding]}

   ;; type service
   {:db/id        [:service/code "cleaning,room,single"]
    :service/type :service.type/service}

   {:db/id        [:service/code "cleaning,linen,single"]
    :service/type :service.type/service}

   {:db/id        [:service/code "keyfob"]
    :service/type :service.type/service}

   {:db/id        [:service/code "pets,dog,boarding"]
    :service/type :service.type/service}

   {:db/id        [:service/code "wish"]
    :service/type :service.type/service}

   {:db/id        [:service/code "pets,dog,walking,single"]
    :service/type :service.type/service}


   {:db/id        [:service/code "drycleaning,single"]
    :service/type :service.type/service}

   {:db/id        [:service/code "kitchenette,coffee/tea,bundle"]
    :service/type :service.type/service}

   {:db/id        [:service/code "fee,installation,furniture"]
    :service/type :service.type/fee}

   [:db/retract [:service/code "furniture,rental,bookshelf"] :service/catalogs :subscription]
   [:db/retract [:service/code "furniture,rental,desk"] :service/catalogs :subscription]
   [:db/retract [:service/code "furniture,rental,laptop,desk"] :service/catalogs :subscription]
   [:db/retract [:service/code "furniture,rental,dresser,small"] :service/catalogs :subscription]
   [:db/retract [:service/code "furniture,rental,dresser,large"] :service/catalogs :subscription]])


(defn norms [conn part]
  {:blueprints.seed/add-initial-services
   {:txes [(add-initial-services part)]}

   :blueprints.seed/update-services-03012018
   {:txes     [(update-services part)]
    :requires [:blueprints.seed/add-initial-services]}

   :blueprints.seed/add-rentals-040418
   {:txes     [(add-rentals part)]
    :requires [:blueprints.seed/update-services-03012018]}

   :blueprints.seed/add-types-and-onboarding-04092018
   {:txes     [(add-types-fix-subscriptions part)]
    :requires [:blueprints.seed/add-rentals-040418]}})
