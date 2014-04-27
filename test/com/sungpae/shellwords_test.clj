(ns com.sungpae.shellwords-test
  "Compares output against the Ruby version of shellwords."
  (:require [clojure.java.shell :refer [sh]]
            [clojure.string :as string]
            [clojure.test :refer [is]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as g :refer [such-that]]
            [clojure.test.check.properties :refer [for-all]]
            [com.sungpae.shellwords :refer [shell-escape shell-split]]))

(defspec test-shell-escape 100
  (for-all [s g/string]
    (is (= (:out (sh "ruby" "-rshellwords" "-e" "print $stdin.read.shellescape" :in s))
           (shell-escape s)))))

(defspec test-shell-split 100
  (for-all [s (such-that #(not (.contains ^String % "\000")) g/string)]
    (let [ruby (sh "ruby" "-rshellwords" "-e" "print $stdin.read.shellsplit.join(\"\\x00\")" :in s)]
      (cond
        (pos? (:exit ruby)) (boolean (is (thrown? IllegalArgumentException (shell-split s))))
        (empty? (:out ruby)) (is (= [] (shell-split s)))
        :else (is (= (string/split (:out ruby) #"\x00")
                     (shell-split s)))))))
