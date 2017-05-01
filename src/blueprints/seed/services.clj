(ns blueprints.seed.services
  (:require [datomic.api :as d]))

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

(defn norms [conn part]
  {:blueprints.seed/add-initial-services
   {:txes [(add-initial-services part)]}})
