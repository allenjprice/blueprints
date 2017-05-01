(ns blueprints.seed-test
  (:require [blueprints.seed :as seed]
            [blueprints.test.datomic :as db :refer [with-conn]]
            [clojure.test :refer :all]
            [datomic.api :as d]))

(use-fixtures :once db/conn-fixture)

;; We don't need to check for the presence of all entities in our seed sets due
;; to the transactional aspect of Datomic--if some of the data is there, all of
;; it should be there.

(defn find-license [conn term]
  (d/q '[:find ?e .
         :in $ ?term
         :where [?e :license/term ?term]]
       (d/db conn) term))

(defn find-service [conn code]
  (d/q '[:find ?e .
         :in $ ?code
         :where [?e :service/code ?code]]
       (d/db conn) code))

(defn find-catalogue [conn code]
  (d/q '[:find ?e .
         :in $ ?code
         :where [?e :catalogue/code ?code]]
       (d/db conn) code))

(deftest has-seed-data?
  (with-conn conn
    (let [_ (seed/conform conn :db.part/user)]
      (testing "licenses have been seeded"
        (is (not (nil? (find-license conn 3)))))

      (testing "properties have been seeded"
        (is (not (nil? (d/entity (d/db conn) [:property/internal-name "52gilbert"]))))
        (is (not (nil? (d/entity (d/db conn) [:property/internal-name "2072mission"])))))

      (testing "services have been seeded"
        (is (not (nil? (find-service conn "plants,planter")))))

      (testing "catalogues have been seeded"
        (is (not (nil? (find-catalogue conn :cleaning+laundry))))))))
