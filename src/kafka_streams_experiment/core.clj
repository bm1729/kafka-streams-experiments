(ns kafka-streams-experiment.core
  (:gen-class)
  (:require [cheshire.core :as json]
            [taoensso.timbre :as log])
  (:import (org.apache.kafka.streams.kstream Predicate ValueMapper KStream)
           (org.apache.kafka.streams StreamsBuilder)))

(defn- create-predicate
  [predicate-fn]
  (reify Predicate (test [this k v] (predicate-fn v))))

(defn- always-true
  []
  (create-predicate (constantly true)))

(defn- create-value-mapper
  [value-mapper-fn]
  (reify ValueMapper (apply [this v] (value-mapper-fn v))))

(defn- safe-value-mapper-fn
  [value-mapper-fn]
  (fn [v]
    (try
      (value-mapper-fn v)
      (catch Exception e
        {::original v
         ::error    (.getMessage e)}))))

(defn- safe-map
  [^KStream k-stream error-topic value-mapper-fn]
  (let [safe-value-mapper (create-value-mapper (safe-value-mapper-fn value-mapper-fn))
        predicates (into-array [(create-predicate #(contains? % ::error)) (always-true)])
        [error-stream other-stream] (-> k-stream
                                        (.mapValues safe-value-mapper)
                                        (.branch predicates))]

    ;; Messages on the error-stream get logged and sent to the error topic
    (-> error-stream
        (.mapValues (create-value-mapper (fn [v]
                                           (log/error (::error v))
                                           (::original v))))
        (.to error-topic))

    ;; Return the other stream for threading
    other-stream))

(defn topology
  [{:keys [input-topic error-topic output-topic]}]
  (let [builder (new StreamsBuilder)]
    (-> builder
        (.stream input-topic)
        (safe-map error-topic json/parse-string)
        (.to output-topic))
    (.build builder)))
