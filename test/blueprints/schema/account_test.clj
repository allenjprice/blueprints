(ns blueprints.schema.account-test
  (:require [blueprints.schema :as schema]
            [blueprints.test.datomic :as dbt]
            [clojure.test :refer :all]
            [datomic.api :as d]))

(use-fixtures :once dbt/conn-fixture)

(deftest accounts-conformed?

  (test-attr a :account/first-name
    (is (dbt/value-type a :string))
    (is (dbt/indexed a)))
  (test-attr a :person/first-name
    (is (dbt/value-type a :string))
    (is (dbt/indexed a)))


  (test-attr a :account/middle-name
    (is (dbt/value-type a :string))
    (is (dbt/indexed a)))
  (test-attr a :person/middle-name
    (is (dbt/value-type a :string))
    (is (dbt/indexed a)))

  (test-attr a :account/last-name
    (is (dbt/value-type a :string))
    (is (dbt/indexed a)))
  (test-attr a :person/last-name
    (is (dbt/value-type a :string))
    (is (dbt/indexed a)))


  (test-attr a :account/phone-number
    (is (dbt/value-type a :string))
    (is (dbt/indexed a)))
  (test-attr a :person/phone-number
    (is (dbt/value-type a :string))
    (is (dbt/indexed a)))

  (test-attr a :account/email
    (is (dbt/unique-identity a))
    (is (dbt/value-type a :string)))

  (test-attr a :account/emergency-contact
    (is (dbt/value-type a :ref))
    (is (dbt/component a))))
