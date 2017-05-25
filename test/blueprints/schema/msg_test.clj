(ns blueprints.schema.msg-test
  (:require [blueprints.schema.cmd :as sut]
            [blueprints.test.datomic :as db :refer :all]
            [clojure.test :refer :all]
            [datomic.api :as d]))

(use-fixtures :once db/conn-fixture)

(deftest schema-conformed?
  (test-attr a :msg/uuid
    (is (value-type a :uuid))
    (is (unique-identity a)))

  (test-attr a :msg/key
    (is (value-type a :keyword))
    (is (indexed a)))

  (test-attr a :msg/params
    (is (value-type a :bytes)))

  (test-attr a :msg/data
    (is (value-type a :string))))


(deftest create-msg
  (testing "can create a msg with params"
    (with-conn conn
      (let [db-after (speculate (d/db conn) [[:db.msg/create :test-msg {:a 1}]])
            cmd      (->> (d/q '[:find ?e . :where [?e :msg/key :test-msg]] db-after)
                          (d/entity db-after))]
        (is (= :test-msg (:msg/key cmd)))
        (is (= (:msg/data cmd) "{:a 1}")))))

  (testing "can create a msg /without/ params"
    (with-conn conn
      (let [db-after (speculate (d/db conn) [[:db.msg/create :test-msg {}]])
            cmd      (->> (d/q '[:find ?e . :where [?e :msg/key :test-msg]] db-after)
                          (d/entity db-after))]
        (is (= :test-msg (:msg/key cmd)))
        (is (nil? (:msg/data cmd)))))))
