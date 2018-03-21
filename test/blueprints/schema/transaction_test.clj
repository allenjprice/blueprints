(ns blueprints.schema.transaction-test
  (:require [toolbelt.datomic.test :as tdt :refer [test-attr]]
            [clojure.test :refer :all]))


(use-fixtures :once (tdt/conn-fixture blueprints.schema/conform))


(deftest transaction-schema-conformed

  (test-attr a :transaction/id
             (is (tdt/value-type a :string))
             (is (tdt/unique-identity a)))

  (test-attr a :transaction/payment
             (is (tdt/value-type a :ref))
             (is (tdt/indexed a)))

  (test-attr a :transaction/payout-id
             (is (tdt/value-type a :string))
             (is (tdt/unique-identity a)))

  (test-attr a :transaction/source-id
             (is (tdt/value-type a :string))
             (is (tdt/unique-identity a))))
