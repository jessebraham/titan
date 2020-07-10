(ns titan.core
  (:require [clojure.string :as str]
            [titan.render   :as ren]
            [titan.request  :as req]
            [clojure.core.match  :refer [match]]
            [clojure.term.colors :refer [bold red underline]])
  (:gen-class))

;; Load the contents of 'project.clj' into a hash map.
;; https://stackoverflow.com/a/16275034
(def ^:private project
  (->> "project.clj"
       slurp
       read-string
       (drop 2) ; drop `defproject` and project name
       (cons :version)
       (apply hash-map)))

;; The application's name, description, and version are printed at the
;; beginning of multiple messages, so we collect them here to keep things DRY.
(def ^:private app-info
  [(str (bold "Titan") " - " (project :description))
   (str "v" (project :version))])

;; ---------------------------------------------------------------------------
;; printing

(defn- print-lines [lines]
  (println (str/join \newline lines)))

(defn- show-help-and-exit []
  (print-lines app-info)
  (print-lines [""
                (underline (project :url))
                (underline "https://gemini.circumlunar.space/")
                ""
                "USAGE:"
                "  titan [-h, --help] [--version] [URL]"
                ""
                "POSITIONAL ARGUMENTS:"
                "  URL           (optional) load this URL upon launch"
                ""
                "OPTIONAL ARGUMENTS:"
                "  -h, --help    show this help message and exit"
                "  --version     display version information and exit"])
  (System/exit 0))

(defn- show-version-and-exit []
  (println (str "Titan " (project :version)))
  (System/exit 0))

(defn- show-banner []
  (print-lines app-info)
  (print-lines [""
                "Titan has launched!"
                "Enter ? to show a list of commands."
                ""]))

(defn- show-commands []
  (print-lines [""
                "COMMANDS:"
                "  ?             show this list of commands"
                "  go URL        navigate to the provided URL"
                "  exit, quit    exit the application"
                ""]))

;; ---------------------------------------------------------------------------
;; repl

(defn- prompt []
  (print "🚀 ")
  (flush)
  (str/lower-case (str/trim (read-line))))

(defn- command-no-args [cmd]
  (match [cmd]
    ["?"]                 (show-commands)
    [(:or "exit" "quit")] (System/exit 0)
    :else                 (println (red "Invalid command: " cmd))))

(defn- command-with-args [cmd args]
  (match [cmd]
    ["go"] (ren/render-markup (req/load-content (first args)))
    :else  (println (red "Invalid command: "
                         (str/join " " (conj args cmd))))))

(defn- exec-command [line]
  (let [items (filter #(seq %) (str/split line #"\s+"))]
    (if (= (count items) 1)
      (command-no-args   (first items))
      (command-with-args (first items) (drop 1 items)))))

(defn- repl []
  (while true
    (let [line (prompt)]
      (when-not (str/blank? line)
        (exec-command line)))))

(defn- start-repl [url]
  (show-banner)
  (when-not (nil? url)
    (ren/render-markup (req/load-content url)))
  (repl))

;; ---------------------------------------------------------------------------
;; argument parsing & application entry point

(defn- process-args
  "Sequentially match on any optional arguments. If we have not terminated,
   filter out any remaining options as they are no longer required, and return
   the results."
  [args]
  (doseq [opt args]
    (match [(str/lower-case opt)]
      [(:or "-h" "--help")] (show-help-and-exit)
      ["--version"]         (show-version-and-exit)
      :else                 nil))
  (filter #(not (str/starts-with? % "-")) args))

(defn -main
  "Process any command-line arguments, invoking the desired behaviour. By
   default start the application's REPL and load a URL at launch if provided."
  [& args]
  (let [args (process-args args)] ; `process-args` may call `System/exit`
    (start-repl (first args))))
