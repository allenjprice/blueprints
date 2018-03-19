(ns blueprints.schema.suggestion-test
  (:require [toolbelt.datomic.test :as tdt :refer [test-attr]]
            [clojure.test :refer :all]))


(use-fixtures :once (tdt/conn-fixture blueprints.schema/conform))


(deftest suggestion-test-conformed

  (test-attr a :suggestion/city
    (is (tdt/value-type a :string))
    (is (tdt/indexed a)))

  (test-attr a :suggestion/account
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a))))
