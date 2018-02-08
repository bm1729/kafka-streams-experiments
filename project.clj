(defproject kafka-streams-experiment "0.1.0-SNAPSHOT"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.apache.kafka/kafka_2.12 "1.0.0"]
                 [org.apache.kafka/kafka-streams "1.0.0"]
                 [cheshire/cheshire "5.8.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.fzakaria/slf4j-timbre "0.3.8"]]
  :main ^:skip-aot kafka-streams-experiment.core
  :target-path "target/%s"
  :profiles {:dev     {:test-paths   ["test"]
                       :dependencies [[org.apache.kafka/kafka_2.12 "1.0.0" :classifier "test"]
                                      [org.apache.kafka/kafka-streams "1.0.0" :classifier "test"]
                                      [org.apache.kafka/kafka-clients "1.0.0" :classifier "test"]]}
             :uberjar {:aot :all}})
