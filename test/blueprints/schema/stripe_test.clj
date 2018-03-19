(ns blueprints.schema.stripe-test
  (:require [toolbelt.datomic.test :as tdt :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :once (tdt/conn-fixture blueprints.schema/conform))


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
    (is (indexed a))))
