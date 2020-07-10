(defproject titan "0.1.0-SNAPSHOT"
  :description "A minimal Gemini Project client written in Clojure."
  :url "https://github.com/jessebraham/titan"
  :license {:name "MIT"
            :url "https://github.com/jessebraham/titan/blob/master/LICENSE"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/core.match "1.0.0"]
                 [clojure-term-colors "0.1.0"]]
  :main ^:skip-aot titan.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
