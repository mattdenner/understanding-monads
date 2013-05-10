(ns understanding_monads.t-basic-monad-transformers
  (:use midje.sweet)
  (:use clojure.algo.monads))

; So you combine the behaviours of monads using transformers.  Pretend we have the following:
; we have a sequence that contains numbers, or nils, and we want to apply functions across
; these, except the nils.  For example, let's increment the numbers:
(defn increment-sequence-of-numbers-normally [s]
  (map
    (fn [v] (if (nil? v) v (inc v)))
    s)
  )

(facts "increment-sequence-of-numbers-normally"
       (fact "increments sequences of numbers"
             (increment-sequence-of-numbers-normally [1 2 3]) => [2 3 4])
       (fact "does not increment nils"
             (increment-sequence-of-numbers-normally [1 nil 2]) => [2 nil 3]))

; But we can use the sequence monad for iterating over the sequence:
(defn increment-sequence-of-numbers-using-sequence-m [s]
  (with-monad sequence-m
    (m-fmap
      (fn [v] (if (nil? v) v (inc v)))
      s)
    ))

(facts "increment-sequence-of-numbers-using-sequence-m"
       (fact "increments sequences of numbers"
             (increment-sequence-of-numbers-using-sequence-m [1 2 3]) => [2 3 4])
       (fact "does not increment nils"
             (increment-sequence-of-numbers-using-sequence-m [1 nil 2]) => [2 nil 3]))

; And that inside bit is actually maybe-m, so we can rewrite that again:
(defn increment-sequence-of-numbers-using-sequence-m-and-maybe-m [s]
  (with-monad sequence-m
    (m-fmap
      (fn [v] (with-monad maybe-m (m-fmap inc v)))
      s)
    ))

(facts "increment-sequence-of-numbers-using-sequence-m-and-maybe-m"
       (fact "increments sequences of numbers"
             (increment-sequence-of-numbers-using-sequence-m-and-maybe-m [1 2 3]) => [2 3 4])
       (fact "does not increment nils"
             (increment-sequence-of-numbers-using-sequence-m-and-maybe-m [1 nil 2]) => [2 nil 3]))

; So what we've really done is wrap a maybe-m inside a sequence-m, which can be achieved by
; using the maybe-t transformer to inject the behaviour inside the sequence-m monad:
(def sequence-of-maybes-m (maybe-t sequence-m))             ; Our new monad
(defn increment-sequence-of-numbers-using-transformer [s]
  (with-monad sequence-of-maybes-m
    (m-fmap inc s)))

(facts "increment-sequence-of-numbers-using-transformer"
       (fact "increments sequences of numbers"
             (increment-sequence-of-numbers-using-transformer [1 2 3]) => [2 3 4])
       (fact "does not increment nils"
             (increment-sequence-of-numbers-using-transformer [1 nil 2]) => [2 nil 3]))

; Think about it: we've just made it possible to apply basic functions to elements of a sequence
; where the elements may be optional!  We haven't had to write any special code, we get it
; because of the monads.
;
; But be aware: transformers are directional!  So a (sequence-t maybe-m) operates on an
; optional sequence, not a sequence of optional values:
(def maybe-a-sequence-m (sequence-t maybe-m))
(defn increment-optional-sequence-of-numbers [s]
  (with-monad maybe-a-sequence-m
    (m-fmap inc s)))

(facts "increment-optional-sequence-of-numbers"
       (fact "increments a sequence when it exists"
             (increment-optional-sequence-of-numbers [1 2 3]) => [2 3 4])
       (fact "does not blow up if the sequence does not exist"
             (increment-optional-sequence-of-numbers nil) => nil)
       (fact "but blows up if the sequence contains nils"
             (increment-optional-sequence-of-numbers [1 nil 2]) => (throws NullPointerException)))
