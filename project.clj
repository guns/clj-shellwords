(defproject clj-shellwords "1.0.0-SNAPSHOT"
  :description "Port of Ruby's Shellwords module"
  :url "https://github.com/guns/clj-shellwords"
  :license {:name "Ruby License"
            :url "https://www.ruby-lang.org/en/about/license.txt"}
  :dependencies []
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.5.7"]]}
             :1.2 {:dependencies [[org.clojure/clojure "1.2.1"]]}
             :1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
             :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}}
  :aliases {"test-all" ["with-profile" "dev,1.2:dev,1.3:dev,1.4:dev,1.5:dev,1.6" "test"]})
