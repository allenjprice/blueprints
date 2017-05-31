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
              cmd      (->> (d/q '[:find ?e . :where [?e :cmd/key :application/submit]] db-after)
                            (d/entity db-after))
              msg      (->> (d/q '[:find ?e . :where [?e :msg/key :application/submitted]] db-after)
                            (d/entity db-after))]
          (is (= :application.status/submitted (:application/status app)))
          ;; CMD
          (is (= :application/submit (:cmd/key cmd)))
          (is (= (:db/id app) (-> cmd :cmd/data read-string :application-id)))
          ;; MSG
          (is (= :application/submitted (:msg/key msg)))
          (is (= (:db/id app) (-> msg :msg/data read-string :application-id))))))))
