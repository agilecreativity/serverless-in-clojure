(ns lesson-two.dynamo
  (:require
    [environ.core :refer [env]]
    [taoensso.faraday :as dynamo]))

; When we are in local development, we feed some fake keys and the endpoint
; for the local DynamoDB server. When we run in Lambda, :development will be
; `nil` and Faraday will grab the real configuration details.
(def client-config
  (if (:development env)
    {:access-key "OMG_DEVELOPMENT"
     :secret-key "I_SHOULD_KEEP_THIS_SECRET!"

     ; Point the configuration at the DynamoDB Local
     :endpoint "http://localhost:8000"}
    {:endpoint "http://dynamodb.us-west-2.amazonaws.com"}))

(def table-name :primes)

(defn put-prime
  "Place a single prime into our list"
  [index prime]
  (dynamo/put-item client-config table-name
                   {:index index
                    :prime prime}))

(defn list-primes
  "Get the entire list of primes.

  Note: Amazon discourages Scans on DynamoDB because it's expensive for read
  operations. This is only for demonstration purposes."
  []
  (->> (dynamo/scan client-config table-name)
       (map (comp int :prime))
       (apply sorted-set)
       vec))

(defn get-prime
  "Get a specific prime from our list"
  [index]
  (int (:prime (dynamo/get-item client-config table-name {:index index}))))

; Here we can programmatically create the table for DynamoDB Local
#_(dynamo/create-table client-config table-name
                       [:index :n]
                       {:throughput {:read 5 :write 5}
                        :block? true})
