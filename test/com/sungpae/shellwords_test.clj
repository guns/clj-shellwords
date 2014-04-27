(ns com.sungpae.shellwords-test
  "Compares output against the Ruby version of shellwords."
  (:require [clojure.java.shell :refer [sh]]
            [clojure.string :as string]
            [clojure.test :refer [is]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as g :refer [such-that]]
            [clojure.test.check.properties :refer [for-all]]
            [com.sungpae.shellwords :refer [shell-escape shell-join
                                            shell-split]]))

(def string-without-0x1F
  "^_  U+001F  <control>  (INFORMATION SEPARATOR ONE)"
  (such-that #(not (.contains ^String % "\u001F")) g/string))

(defspec test-shell-escape 200
  (for-all [s g/string]
    (is (= (:out (sh "ruby" "-rshellwords" "-EUTF-8" "-e"
                     "print $stdin.read.shellescape"
                     :in s))
           (shell-escape s)))))

(defspec test-shell-split 200
  (for-all [s string-without-0x1F]
    (let [ruby (sh "ruby" "-rshellwords" "-EUTF-8" "-e"
                   "print $stdin.read.shellsplit.join(\"\\x1F\")"
                   :in s)]
      (cond
        (pos? (:exit ruby)) (boolean (is (thrown? IllegalArgumentException
                                                  (shell-split s))))
        (empty? (:out ruby)) (is (= [] (shell-split s)))
        :else (is (= (string/split (:out ruby) #"\x1F")
                     (shell-split s)))))))

(defspec test-shell-join 200
  ;; Ruby and Clojure both join an empty [] to an empty string
  (for-all [v (such-that seq (g/vector string-without-0x1F))]
    (let [n (count v)]
      (is (= (:out (sh "ruby" "-rshellwords" "-EUTF-8" "-e"
                       "s = $stdin.read; n = ENV['SIZE'].to_i;
                        print (n == 1 ? [s] : s.split(\"\\x1F\", n)).shelljoin"
                       :in (string/join \u001F v)
                       :env {"SIZE" n}))
             (shell-join v))))))
