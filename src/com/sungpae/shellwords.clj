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

;; # Escapes a string so that it can be safely used in a Bourne shell
;; # command line.  +str+ can be a non-string object that responds to
;; # +to_s+.
;; #
;; # Note that a resulted string should be used unquoted and is not
;; # intended for use in double quotes nor in single quotes.
;; #
;; #   argv = Shellwords.escape("It's better to give than to receive")
;; #   argv #=> "It\\'s\\ better\\ to\\ give\\ than\\ to\\ receive"
;; #
;; # String#shellescape is a shorthand for this function.
;; #
;; #   argv = "It's better to give than to receive".shellescape
;; #   argv #=> "It\\'s\\ better\\ to\\ give\\ than\\ to\\ receive"
;; #
;; #   # Search files in lib for method definitions
;; #   pattern = "^[ \t]*def "
;; #   open("| grep -Ern #{pattern.shellescape} lib") { |grep|
;; #     grep.each_line { |line|
;; #       file, lineno, matched_line = line.split(':', 3)
;; #       # ...
;; #     }
;; #   }
;; #
;; # It is the caller's responsibility to encode the string in the right
;; # encoding for the shell environment where this string is used.
;; #
;; # Multibyte characters are treated as multibyte characters, not bytes.
;; #
;; # Returns an empty quoted String if +str+ has a length of zero.
;; def shellescape(str)
;;   str = str.to_s
;;
;;   # An empty argument will be skipped, so return empty quotes.
;;   return "''" if str.empty?
;;
;;   str = str.dup
;;
;;   # Treat multibyte characters as is.  It is caller's responsibility
;;   # to encode the string in the right encoding for the shell
;;   # environment.
;;   str.gsub!(/([^A-Za-z0-9_\-.,:\/@\n])/, "\\\\\\1")
;;
;;   # A LF cannot be escaped with a backslash because a backslash + LF
;;   # combo is regarded as line continuation and simply ignored.
;;   str.gsub!(/\n/, "'\n'")
;;
;;   return str
;; end
(defn shell-escape
  "Ported from Ruby's Shellwords#shellescape()"
  [s]
  (if (empty? s)
    "''"
    (-> s
        (string/replace #"([^A-Za-z0-9_\-.,:\/@\n])" "\\\\$1")
        (string/replace #"\n" "'\n'"))))

;; # Splits a string into an array of tokens in the same way the UNIX
;; # Bourne shell does.
;; #
;; #   argv = Shellwords.split('here are "two words"')
;; #   argv #=> ["here", "are", "two words"]
;; #
;; # String#shellsplit is a shortcut for this function.
;; #
;; #   argv = 'here are "two words"'.shellsplit
;; #   argv #=> ["here", "are", "two words"]
;; def shellsplit(line)
;;   words = []
;;   field = ''
;;   line.scan(/\G\s*(?>([^\s\\\'\"]+)|'([^\']*)'|"((?:[^\"\\]|\\.)*)"|(\\.?)|(\S))(\s|\z)?/m) do
;;     |word, sq, dq, esc, garbage, sep|
;;     raise ArgumentError, "Unmatched double quote: #{line.inspect}" if garbage
;;     field << (word || sq || (dq || esc).gsub(/\\(.)/, '\\1'))
;;     if sep
;;       words << field
;;       field = ''
;;     end
;;   end
;;   words
;; end
(defn shell-split
  "Ported from Ruby's Shellwords#shellsplit()"
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
