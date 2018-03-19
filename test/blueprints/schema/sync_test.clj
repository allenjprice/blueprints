(ns blueprints.schema.sync-test
  (:require [toolbelt.datomic.test :as tdt :refer [test-attr]]
            [clojure.test :refer :all]))


(use-fixtures :once (tdt/conn-fixture blueprints.schema/conform))


(deftest sync-schema-conformed

  (test-attr a :sync/ref
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (test-attr a :sync/ext-id
    (is (tdt/value-type a :string))
    (is (tdt/indexed a))
    (is (tdt/unique-identity a)))

  (test-attr a :sync/service
    (is (tdt/value-type a :keyword))
    (is (tdt/indexed a)))

  (test-attr a :sync/last-synced
    (is (tdt/value-type a :instant))
    (is (tdt/indexed a))))
