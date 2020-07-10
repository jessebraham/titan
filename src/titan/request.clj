(ns titan.request
  (:import (java.io BufferedReader BufferedWriter
                    InputStreamReader OutputStreamWriter)
           (javax.net.ssl SSLSocketFactory))
  (:require [clojure.string :as str]
            [titan.url      :as url]))

;; ---------------------------------------------------------------------------
;; gemini requests

(defn- create-ssl-socket [host port]
  (let [f (SSLSocketFactory/getDefault)
        s (.createSocket f host port)]
    (.startHandshake s)
    s))

(defn- write-body [w url]
  (let [body (str url "\r\n")]
    (.write w body 0 (count body))
    (.flush w)))

(defn- read-response-lines [r]
  (loop [line   (.readLine r)
         result []]
    (if-not line
      result
      (recur (.readLine r)
             (conj result line)))))

(defn- read-response [s]
  (let [r     (BufferedReader. (InputStreamReader. (.getInputStream  s)))
        lines (read-response-lines r)]
    (.close r)
    ; FIXME: check response header and act accordingly
    ; FIXME: body is optional
    (str/join \newline (drop 1 lines))))

(defn- make-request [s url]
  (let [resp (atom nil)
        w    (BufferedWriter. (OutputStreamWriter. (.getOutputStream s)))]
    (write-body w url)              ; FIXME: handle invalid hosts
    (reset! resp (read-response s)) ; FIXME: how do I do this idiomatically?
    (.close w)
    @resp))

(defn load-content
  "Make a request to `url` on `port`, returning the response as a string."
  [url & {:keys [port] :or {port 1965}}]
  ; FIXME: validate absolute url
  (let [s    (create-ssl-socket (url/hostname url) port)
        resp (make-request s (url/absolute url))]
    (.close s)
    resp))
