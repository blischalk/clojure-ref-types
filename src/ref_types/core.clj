(ns ref-types.core
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))


(def current-artist (ref "Venetian Snares"))
(def current-track (ref "Hajnal"))

(defn ref-example
  [args]
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

(def currently-playing (atom {:artist "Venetian Snares" :track "Hajnal"}))

(defn atom-example
  [args]
  (println "refexample executing..."))

(def cli-options [["-r" "--ref-example" "Ref Example"]
                  ["-a" "--atom-example" "Atom Example"]
                  ["-h" "--help"]])


(defn -main [& args]
  (let [cli (parse-opts args cli-options)
        options (:options cli)
        args (:arguments cli)]
    (cond (:help options) (println (:summary cli))
          (:ref-example options) (ref-example args)
          (:atom-example options) (atom-example args)
          :otherwise (println cli))))
