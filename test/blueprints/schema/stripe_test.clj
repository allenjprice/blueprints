(ns blueprints.schema.stripe-test
  (:require [blueprints.test.datomic :as dbt :refer [test-attr]]
            [clojure.test :refer :all]))

(use-fixtures :once dbt/conn-fixture)


(deftest stripe-conformed?

  (test-attr a :stripe/plan-id
    (is (dbt/value-type a :string))
    (is (dbt/unique-identity a)))

  (test-attr a :stripe/subs-id
    (is (dbt/value-type a :string))
    (is (dbt/unique-identity a)))

  (test-attr a :stripe/charge
    (is (dbt/value-type a :ref))
    (is (dbt/indexed a))
    (is (dbt/component a))))
