(ns understanding-monads.t-either-monad
  (:use midje.sweet)
  (:use clojure.algo.monads)
  (:use understanding-monads.either))

(facts "either-m"
       ; Note that left-either and right-either are really constructors, therefore don't apply across
       ; monads (like m-fmap or other methods).
       (fact "left-either returns left sided value"
             (left-either "value") => (with-monad either-m (m-result [:left "value"]))
             (left-either nil)     => (with-monad either-m (m-result [:left nil])))

       (fact "right-either returns right sided value"
             (right-either "value") => (with-monad either-m (m-result [:right "value"]))
             (right-either nil)     => (with-monad either-m (m-result [:right nil])))

       (with-monad either-m
         ; Can't be a monad if it don't obey the laws!
         (facts "monad laws"
                (let [first-monadic-function  (fn [[side value]] (m-result [:first  {:side side, :value value}]))
                      second-monadic-function (fn [[side value]] (m-result [:second {:side side, :value value}]))
                      simple-value            [:inside "value"]
                      monadic-value           (m-result simple-value)]

                  (fact "#1" (m-bind monadic-value first-monadic-function)                                  => (first-monadic-function simple-value))
                  (fact "#2" (m-bind monadic-value m-result)                                                => monadic-value)
                  (fact "#3" (m-bind (m-bind monadic-value first-monadic-function) second-monadic-function) => (m-bind monadic-value (fn [v] (m-bind (first-monadic-function v) second-monadic-function))))))

         ; Really you'd use a multimethod when dealing with an either-m:
         (defmulti handle-message-mf side-of-either)
         (defmethod handle-message-mf :left  [[_ value]] (m-result [:inside (str "Left:" value)]))
         (defmethod handle-message-mf :right [[_ value]] (m-result [:inside (str "Right:" value)]))

         (fact "m-bind is best used with multimethods"
               (m-bind (left-either 1) handle-message-mf)  => (m-result [:inside "Left:1"])
               (m-bind (right-either 1) handle-message-mf) => (m-result [:inside "Right:1"]))

         ; Why we monad: here's the same with normal functions!
         (defmulti handle-message-f side-of-either)
         (defmethod handle-message-f :left  [[_ value]] [:inside (str "Left:" value)])
         (defmethod handle-message-f :right [[_ value]] [:inside (str "Right:" value)])

         (fact "m-fmap works"
               (m-fmap handle-message-f (left-either 1))  => (m-result [:inside "Left:1"])
               (m-fmap handle-message-f (right-either 1)) => (m-result [:inside "Right:1"]))

         ))
