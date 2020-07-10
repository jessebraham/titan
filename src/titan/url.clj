(ns titan.url
  (:require [clojure.string :as str]))

;; ---------------------------------------------------------------------------
;; url transformers

(defn absolute [url]
  (cond
    (str/starts-with? url "gemini://") url
    (str/starts-with? url "//")        (str "gemini:" url)
    :else                              (str "gemini://" url)))

(defn hostname [url]
  (first (str/split (str/replace url #"^gemini://" "") #"\/")))
