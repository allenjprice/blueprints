(defproject starcity/blueprints "1.8.0"
  :description "The Starcity database schema and migration API."
  :url "https://github.com/starcity-properties/blueprints"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [com.datomic/datomic-free "0.9.5544"]
                 [io.rkn/conformity "0.4.0"]
                 [starcity/toolbelt "0.1.7" :exclusions [com.datomic/datomic-free]]
                 [prismatic/plumbing "0.5.4"]
                 [datomic-schema "1.3.0"]
                 [clj-time "0.13.0"]]

  :plugins [[s3-wagon-private "1.2.0"]]

  :repositories {"releases" {:url           "s3://starjars/releases"
                             :username      :env/aws_access_key
                             :passphrase    :env/aws_secret_key
                             :sign-releases false}})
