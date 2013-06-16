(ns understanding-monads.either
  (:use [clojure.algo.monads :only (defmonad defmonadfn with-monad)])
  (:use [clojure.tools.macro :only (defsymbolmacro)]))

(defmonad either-m
  "A monad describing a value that can be either left or right sided, although this is actually
  'any' sided! Typically right sided values are successes, and left are failures. A monadic
  function within this monad takes a two element array: the first is the side, the second is
  the value. You're advised to use multimethods to handle this."
  [m-result (fn m-result-either [[side value]] {:side side, :value value})
   m-bind   (fn m-bind-either   [mv mf] (mf [(:side mv) (:value mv)]))
   ])

(defn left-either
  "Returns a left-sided either monadic value"
  [v]
  (with-monad either-m (m-result [:left v])))

(defn right-either
  "Returns a right-sided either monadic value"
  [v]
  (with-monad either-m (m-result [:right v])))

(defn side-of-either
  "Helper method for multimethod definitions to extract the side information"
  [[side]]
  side)
