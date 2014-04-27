```
      __ __           __          __ __                        __
.----|  |__|___.-----|  |--.-----|  |  .--.--.--.-----.----.--|  .-----.
|  __|  |  |___|__ --|     |  -__|  |  |  |  |  |  _  |   _|  _  |__ --|
|____|__|  |   |_____|__|__|_____|__|__|________|_____|__| |_____|_____|
       |___|
```

Port of the Shellwords module from Ruby 2.1.0.

### `shell-escape`

Most of the time, external commands should be called directly, `exec(3)`
style:

```clojure
(ns example
  (:require [clojure.java.shell :refer [sh]]))

(defn exec [& args]
  (apply sh "myprog" args))
```

Sometimes, however, we have to use a shell:

```clojure
(defn command-available? [cmd]
  (zero? (:exit (sh "sh" "-c" (str "command -v " cmd)))))
```

Unfortunately, the function above is sensitive to inputs with shell
metacharacters, and can be easily abused:

```clojure
(command-available? "; echo evil-key >> ~/.ssh/authorized_keys & false")
```

We can protect against this by using the `shell-escape` function to escape
metacharacters in our shell argument:

```clojure
(defn command-available? [cmd]
  (let [escaped-cmd (shell-escape cmd)]
    (zero? (:exit (sh "sh" "-c" (str "command -v " escaped-cmd))))))
```

### `shell-split`

`shell-split` splits a string into an array of words in the same manner as a
Bourne-compatible shell:

```clojure
(shell-split "these are 'three shell tokens'")
; -> ["these" "are" "three shell tokens"]
```

This is useful when working with direct input from a shell.

### Authors and License

Authors:

* Wakou Aoyama
* Akinori MUSHA <knu@iDaemons.org>

Ruby License. See the LICENSE and BSDL files.
