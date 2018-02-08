(ns kafka-streams-experiment.core-test
  (:require [clojure.test :refer :all]
            [kafka-streams-experiment.core :as target])
  (:import (org.apache.kafka.streams StreamsConfig StreamsBuilder)
           (java.util Properties)
           (org.apache.kafka.common.serialization Serdes$StringSerde)
           (org.apache.kafka.test ProcessorTopologyTestDriver)))

(def string-serde (new Serdes$StringSerde))

(def string-serializer (.serializer string-serde))

(def string-deserializer (.deserializer string-serde))

(defn- streams-config
  []
  (let [string-serde (-> (new Serdes$StringSerde) .getClass .getName)
        properties (doto (new Properties)
                     (.setProperty StreamsConfig/APPLICATION_ID_CONFIG "test")
                     (.setProperty StreamsConfig/BOOTSTRAP_SERVERS_CONFIG "localhost:9091")
                     (.setProperty StreamsConfig/KEY_SERDE_CLASS_CONFIG string-serde)
                     (.setProperty StreamsConfig/VALUE_SERDE_CLASS_CONFIG string-serde))]
    (new StreamsConfig properties)))

(defn- simple-topology
  []
  (let [builder (new StreamsBuilder)]
    (.to (.stream builder "from") "to")
    (.build builder)))

(defn- processor-topology-test-driver
  [config topology]
  (new ProcessorTopologyTestDriver config topology))

(deftest kafka-streaming
  (testing "Simple streaming works"
    (let [driver (processor-topology-test-driver (streams-config) (simple-topology))
          _ (.process driver "from" "key" "value" string-serializer string-serializer)
          output (.readOutput driver "to" string-deserializer string-deserializer)]
      (is (= "key" (.key output)))
      (is (= "value" (.value output)))))

  (testing "Joining D0149"
    (testing "Invalid JSON"
      (let [topology-config {:input-topic "from" :error-topic "error" :output-topic "to"}
            driver (processor-topology-test-driver (streams-config) (target/topology topology-config))
            _ (.process driver "from" "key" "blah-json" string-serializer string-serializer)
            error-output (.readOutput driver "error" string-deserializer string-deserializer)]
        (is (= "key" (.key error-output)))
        (is (= "blah-json" (.value error-output)))))))
