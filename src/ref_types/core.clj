(ns ref-types.core
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))


(def current-artist (ref "Venetian Snares"))
(def current-track (ref "Hajnal"))


(defn ref-example-1
  []
  (println "refexample executing...")
  (println (str "using @ dereferencing, "
                "the initial value of current-artist is " @current-artist))
  (println "the initial value of current-track is" @current-track)
  (println)
  (println "Updating the refs using STM in a transaction to be...")

  ; Spin off a new thread that will try to
  ; access the inconsistant state of the refs...
  ; but it won't happen :)
  (future (Thread/sleep 1000)
          (println "---------------------------")
          (println "Sneaking in from another thread to check for inconsistancy")
          (println "Current, unaltered artist... From the future :)")
          (println @current-artist)
          (println "--------------------------"))

  (dosync
   (println "Entered transaction")
   (ref-set current-artist "Aphex Twin")
   (Thread/sleep 2000)
   (ref-set current-track "Xtal")
   (println "Leaving transaction"))

  (println (str "the new value of current-artist is " @current-artist))
  (println "the new value of current-track is " @current-track)

  (println)
  (println "Use 'ref-set' when we don't care about the current value of a ref")
  (println)
  (println "Use 'alter' if we do care about the current value of the ref")
  (println "and should retry if there is a chance it had been altered")
  (println)
  (println "Use 'commute' if we do care about the current value of the ref")
  (println "and we don't care if the value has been altered such as a counter")
  (shutdown-agents))


(defn ref-example-2
  []
  (println "Not implemented yet."))


(defn ref-example
  [args]
  (cond (= (first args) "1") (ref-example-1)
        (= (first args) "2") (ref-example-2)))


(def currently-playing (atom {:artist "Venetian Snares" :track "Hajnal"}))

(defn atom-example-1
  []
  (println (str "currently-playing is " (deref currently-playing)))

  (println)
  (println "Updating the whole atom using reset!.")
  (println "New track info: Aphex Twin and Xtal")

  (reset! currently-playing {:artist "Aphex Twin" :track "Xtal"})

  (println (str "currently-playing is " (deref currently-playing)))

  (println)
  (println "Updating part of the atom with swap!.")
  (println "New track info: 'Come to Daddy'")
  (println)

  (swap! currently-playing assoc :track "Come to Daddy")

  (println (str "currently-playing is " (deref currently-playing))))



(defn atom-example
  [args]
  (cond (= (first args) "1") (atom-example-1)))


(def agent-007 (agent ["Sean Connery"]))

(defn agent-example
  [args]
  (println "Never send a blocking action.  You may block other agents.
Use send-off instead.")
  (println)
  (println (str "Agent is... " @agent-007))
  (println)
  (println "Sending off update")
  (send-off agent-007 (fn [a]
                        (Thread/sleep 5000)
                        ["Pierce Brosnan"]))
  (println)
  (println (str "Agent is still..." @agent-007))
  (println)
  (println "Sleepy agent update returns and agent is...")

  (await agent-007)
  (println)

  (println @agent-007)
  (shutdown-agents))


(def cli-options [["-r" "--ref-example" "Ref Example"]
                  ["-a" "--atom-example" "Atom Example"]
                  ["-g" "--agent-example" "Agent Example"]
                  ["-h" "--help"]])



(defn -main [& args]
  (let [cli (parse-opts args cli-options)
        options (:options cli)
        args (:arguments cli)]
    (cond (:help options) (println (:summary cli))
          (:ref-example options) (ref-example args)
          (:atom-example options) (atom-example args)
          (:agent-example options) (agent-example args)
          :otherwise (println cli))))
