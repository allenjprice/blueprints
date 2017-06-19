(ns blueprints.schema.application-test
  (:require [blueprints.schema.cmd :as sut]
            [blueprints.test.datomic :as db :refer :all]
            [clojure.test :refer :all]
            [datomic.api :as d]))

(use-fixtures :once db/conn-fixture)

(deftest submit-application
  (with-conn conn
    (let [tid (d/tempid :db.part/starcity)
          res @(d/transact conn [{:db/id tid :application/status :application.status/in-progress}])
          db  (:db-after res)
          id  (d/resolve-tempid db (:tempids res) tid)]
      (testing "can submit an application"
        (let [db-after (speculate db [[:db.application/submit id]])
              app      (d/entity db-after id)
              event    (->> (d/q '[:find ?e . :where [?e :event/key :application/submit]] db-after)
                            (d/entity db-after))]
          (is (= :application.status/submitted (:application/status app)))
          (is (= :application/submit (:event/key event)))
          (is (= (:db/id app) (-> event :event/params read-string :application-id))))))))
