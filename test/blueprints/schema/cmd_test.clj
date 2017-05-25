(ns blueprints.schema.cmd-test
  (:require [blueprints.schema.cmd :as sut]
            [blueprints.test.datomic :as db :refer :all]
            [clojure.test :refer :all]
            [datomic.api :as d]))

(use-fixtures :once db/conn-fixture)

(deftest schema-conformed?
  (test-attr a :cmd/uuid
    (is (value-type a :uuid))
    (is (unique-identity a)))

  (test-attr a :cmd/id
    (is (value-type a :string))
    (is (unique-identity a)))

  (test-attr a :cmd/key
    (is (value-type a :keyword))
    (is (indexed a)))

  (test-attr a :cmd/params
    (is (value-type a :bytes)))

  (test-attr a :cmd/meta
    (is (value-type a :bytes)))

  (test-attr a :cmd/status
    (is (value-type a :ref))
    (is (indexed a)))

  (test-attr _ :cmd.status/pending)
  (test-attr _ :cmd.status/successful)
  (test-attr _ :cmd.status/failed)

  (test-attr a :cmd/data
    (is (value-type a :string)))

  (test-attr a :cmd/ctx
    (is (value-type a :string))))

(deftest create-cmd
  (testing "can create a cmd"
    (with-conn conn
      (let [db-after (speculate (d/db conn) [[:db.cmd/create :test-cmd {:id   "hello"
                                                                        :ctx  {:a 1}
                                                                        :data {:b 2}}]])
            cmd      (->> (d/q '[:find ?e . :where [?e :cmd/key :test-cmd]] db-after)
                          (d/entity db-after))]
        (is (= :test-cmd (:cmd/key cmd)))
        (is (= "hello" (:cmd/id cmd)))
        (is (= (:cmd/ctx cmd) "{:a 1}"))
        (is (= (:cmd/data cmd) "{:b 2}"))))))
