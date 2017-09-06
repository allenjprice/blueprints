(ns blueprints.schema.suggestion-test
  (:require [blueprints.test.datomic :as dbt :refer [test-attr]]
            [clojure.test :refer :all]))


(use-fixtures :once dbt/conn-fixture)


(deftest suggestion-test-conformed

  (test-attr a :suggestion/city
    (is (dbt/value-type a :string))
    (is (dbt/indexed a)))

  (test-attr a :suggestion/account
    (is (dbt/value-type a :ref))
    (is (dbt/indexed a))))
