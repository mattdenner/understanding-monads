(ns understanding_monads.t-basic-monads
  (:use midje.sweet)
  (:use clojure.algo.monads))

; The identity monad is pretty dumb in that it is just dealing with the value given, no logic
; is applied above this.
(facts "identity-m"
       (with-monad identity-m
         (fact "m-result returns the value"
               (m-result "foo") => "foo"
               (m-result nil) => nil)
         (fact "m-bind applies the function regardless"
               (m-bind (m-result "foo") (fn [v] (m-result (str "Here: " v)))) => "Here: foo"
               (m-bind (m-result nil) (fn [v] (m-result (str "Here: " v)))) => "Here: ")
         (fact "m-fmap applies the function regardless"
               (m-fmap (fn [v] (str "Here: " v)) (m-result "foo")) => "Here: foo"
               (m-fmap (fn [v] (str "Here: " v)) (m-result nil)) => "Here: ")
         (fact "m-reduce applies the function regardless"
               (m-reduce (fn [m,v] (conj m v)) [] [(m-result "a") (m-result "b")]) => ["a" "b"]
               (m-reduce (fn [m,v] (conj m v)) [] [(m-result "a") (m-result nil)]) => ["a" nil]
               (m-reduce (fn [m,v] (conj m v)) [] [(m-result nil) (m-result "a")]) => [nil "a"])
         ))

; The maybe monad is like the identity monad, in that in Clojure it does not need to change
; the value given (because the existence of the value is what it's concerned with), but it does
; ensure that nothing happens if the value is nil.
(facts "maybe-m"
       (with-monad maybe-m
         (fact "m-zero is nil"
               m-zero => nil)
         (fact "m-result does not need to wrap the value"
               (m-result "foo") => "foo"
               (m-result nil) => nil)
         (fact "m-bind only applies the function if the value is defined"
               (m-bind (m-result "foo") (fn [v] (m-result (str "Here: " v)))) => "Here: foo"
               (m-bind (m-result nil) (fn [v] (m-result (str "Here: " v)))) => nil)
         (fact "m-fmap only applies the function if the value is defined"
               (m-fmap (fn [v] (str "Here: " v)) (m-result "foo")) => "Here: foo"
               (m-fmap (fn [v] (str "Here: " v)) (m-result nil)) => nil)
         (fact "m-reduce returns nil if there is a nil in the list"
               (m-reduce (fn [m,v] (conj m v)) [] [(m-result "a") (m-result "b")]) => ["a" "b"]
               (m-reduce (fn [m,v] (conj m v)) [] [(m-result "a") (m-result nil)]) => nil
               (m-reduce (fn [m,v] (conj m v)) [] [(m-result nil) (m-result "a")]) => nil)
         ))

; Some monads behave like a collection of basic values.  This kind of shows the power of monads
; as I've written one function to check that which essentially only references functions defined
; by the particular monad we're using.
(defn behaves-like-a-container [monad ctor zero]
  (with-monad monad
    (fact "m-zero is the empty container"
          m-zero => zero)
    (fact "m-result wraps the value in a container"
          (m-result "foo") => (ctor "foo")
          (m-result nil) => (ctor nil))
    (fact "m-plus will combine two containers"
          (m-plus (m-result "a") (m-result "b")) => (ctor "a" "b"))
    (fact "m-bind applies across the entire container"
          (m-bind (m-plus (m-result "a") (m-result "b")) (fn [a] (m-result (str a "!")))) => (ctor "a!" "b!"))
    (fact "m-fmap applies across the entire container"
          (m-fmap (fn [a] (str a "!")) (m-plus (m-result "a") (m-result "b"))))
    ))

; The set and sequence monads behave in similar fashions, except they use different containers.
(facts "set-m"      (behaves-like-a-container set-m      (fn [& args] (set args)) #{}))
(facts "sequence-m" (behaves-like-a-container sequence-m (fn [& args] args)       '()))
