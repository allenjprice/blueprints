(ns blueprints.schema.stripe-customer-test
  (:require [toolbelt.datomic.test :as tdt :refer [test-attr]]
            [clojure.string :as string]
            [clojure.test :refer :all]))

(use-fixtures :once (tdt/conn-fixture blueprints.schema/conform))


(deftest stripe-customer-schema-conformed

  (test-attr a :customer/platform-id
    (is (tdt/value-type a :string))
    (is (tdt/unique-identity a)))

  (test-attr a :customer/account
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (test-attr a :customer/connected
    (is (tdt/value-type a :ref))
    (is (tdt/cardinality a :many))
    (is (tdt/component a))
    (is (tdt/indexed a)))

  (test-attr a :connected-customer/customer-id
    (is (tdt/value-type a :string))
    (is (tdt/unique-identity a)))

  (test-attr a :connected-customer/property
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (test-attr a :stripe-customer/bank-account-token
    (is (string/starts-with? (:db/doc a) "DEPRECATED")))

  (test-attr a :stripe-customer/managed
    (is (string/starts-with? (:db/doc a) "DEPRECATED"))))
