;; Copyright (C) 1993-2013 Yukihiro Matsumoto. All rights reserved.
;;
;; Redistribution and use in source and binary forms, with or without
;; modification, are permitted provided that the following conditions
;; are met:
;; 1. Redistributions of source code must retain the above copyright
;; notice, this list of conditions and the following disclaimer.
;; 2. Redistributions in binary form must reproduce the above copyright
;; notice, this list of conditions and the following disclaimer in the
;; documentation and/or other materials provided with the distribution.
;;
;; THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
;; ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
;; IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
;; ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
;; FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
;; DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
;; OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
;; HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
;; LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
;; OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
;; SUCH DAMAGE.

(ns com.sungpae.shellwords
  "Adaptations of methods from Ruby's Shellwords module.

   https://github.com/ruby/ruby/blob/trunk/lib/shellwords.rb

   Authors:

     * Wakou Aoyama
     * Akinori MUSHA <knu@iDaemons.org>

   This port maintained by:

     * Sung Pae <self@sungpae.com>"
  (:require [clojure.string :as string]))

(defn shell-escape
  "Escapes a string so that it can be safely used in a Bourne shell command
   line. Argument will be converted to a string if it is not a string.

   Note that the resulting string should be used unquoted.

     (shell-escape \"It's better to give than to receive\")
     ; \"It\\'s\\ better\\ to\\ give\\ than\\ to\\ receive\"

   Returns the empty shell string \"''\" if s has a length of zero.

   Ported from Ruby's Shellwords#shellescape()"
  [s]
  (let [s (str s)]
    (if (empty? s)
      ;; An empty argument will be skipped, so return empty quotes
      "''"
      (-> s
          (string/replace #"([^A-Za-z0-9_\-.,:\/@\n])" "\\\\$1")
          ;; A LF cannot be escaped with a backslash because a backslash + LF
          ;; combo is regarded as a line continuation and consequently ignored
          (string/replace #"\n" "'\n'")))))

(defn shell-split
  "Splits a string into an array of tokens in the same way the UNIX Bourne
   shell does.

     (shell-split \"these are 'three shell tokens'\")
     ; [\"these\" \"are\" \"three shell tokens\"]

   Ported from Ruby's Shellwords#shellsplit()"
  [line]
  ;; Note that the Pattern is in DOTALL mode
  (let [ms (re-seq #"(?s)\G\s*(?>([^\s\\\'\"]+)|'([^\']*)'|\"((?:[^\"\\]|\\.)*)\"|(\\.?)|(\S))(\s|\z)?" line)]
    (first
      (reduce
        (fn [[words ^StringBuilder field] [_ word sq dq esc garbage sep]]
          (when garbage
            (throw (IllegalArgumentException. (str "Unmatched quote: " (pr-str line)))))
          (.append field (or word sq (string/replace (or dq esc) #"\\(.)" "$1")))
          (if sep
            [(conj words (str field)) (StringBuilder.)]
            [words field]))
        [[] (StringBuilder.)] ms))))

(defn shell-join
  "Builds a command line string from a collection. Non-string elements of the
   collection are converted to strings.

     (shell-join [\"Don't\", \"rock\", \"the\", \"boat\"])
     ; \"Don\\'t rock the boat\"

   Ported from Ruby's Shellwords#shellescape()"
  [coll]
  (string/join " " (mapv shell-escape coll)))
