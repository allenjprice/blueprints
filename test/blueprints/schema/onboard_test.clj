(ns blueprints.schema.onboard-test
  (:require [blueprints.test.datomic :as dbt :refer :all]
            [clojure.test :refer :all]))


(use-fixtures :once dbt/conn-fixture)


(deftest onboard-conformed?
  (test-attr a :onboard/account
    (is (value-type a :ref))
    (is (indexed a)))

  (test-attr a :onboard/move-in
    (is (value-type a :instant))
    (is (indexed a)))

  (test-attr a :onboard/seen
    (is (value-type a :keyword))
    (is (cardinality a :many))
    (is (indexed a))))
