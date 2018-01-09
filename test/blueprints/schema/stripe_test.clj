(ns blueprints.schema.stripe-test
  (:require [blueprints.test.datomic :as dbt :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :once dbt/conn-fixture)


(deftest stripe-conformed?

  (test-attr a :stripe/plan-id
    (is (value-type a :string)))

  (test-attr a :stripe/subs-id
    (is (value-type a :string))
    (is (unique-identity a)))

  (test-attr a :stripe/charge-id
    (is (value-type a :string))
    (is (unique-identity a)))

  (test-attr a :stripe/source-id
    (is (value-type a :string))
    (is (indexed a)))

  (test-attr a :stripe/payout-id
    (is (value-type a :string))
    (is (indexed a))))
