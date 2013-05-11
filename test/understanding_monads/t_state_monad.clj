(ns understanding-monads.t-state-monad
  (:use midje.sweet)
  (:use clojure.algo.monads))

; The state monad is all about managing, well, state.  The monadic values are functions that
; take a state and return the value at the state and the new state; so I'll use 'mvf' in my
; notation to me "monadic value function".  m-result becomes returns a mvf that is effectively
; the initial value.
(facts "state-m"
       (with-monad state-m
         (fact "m-result returns an mvf"
               ((m-result "initial") "state") => [ "initial" "state" ]
               ((m-result "initial") 10) => [ "initial" 10 ])

         ; You know how the type signature of m-bind is:
         ; m-bind: M[a] -> (a -> M[b]) -> M[b]
         ; Well in the state-m you have to replace 'M' with 'M[s]', so you have:
         ; m-bind: M[s][a] -> (a) -> M[s][b]) -> M[s][b]
         ; In other words: values are wrapped in the state monad, which holds the
         ; state.
         (fact "m-bind returns an mvf that applies the function to the value"
               ((m-bind (m-result "initial") (fn [v] (m-result (str v "!")))) "state") => [ "initial!" "state" ]
               ((m-bind (m-result "initial") (fn [v] (m-result "ignore"))) "state") => [ "ignore" "state" ])

         ; Helpers functions that create mvf that do typically required behaviour
         (fact "fetch-state returns the state as the value"
               ((fetch-state) "state") => [ "state" "state" ])
         (fact "set-state sets the state and returns the old state as the value"
               ((set-state "new") "old") => [ "old" "new" ])
         (fact "update-state applies the function to the state"
               ((update-state (fn [s] (str s "!"))) "state") => [ "state" "state!" ])
         ))

; Alright, so let's build a very very stupid calculator!  The state is the result of the
; calculation, and the value is the list of operations to apply.  We keep applying the
; next operation (the 2 head elements of the list) to the state until the list is empty.
(facts "sort of proving the point here"
       (defn calculator [zero & original-operations]
         (with-monad state-m
           ((m-until
              (fn [operations] (empty? operations))
              (fn [operations] (domonad [[op value & rest] (m-result operations)
                                         current           (fetch-state)
                                         _                 (set-state (op current value))
                                         ] rest))
              original-operations
              ) zero)))

       (fact "1 + 1 = 2"     (calculator 1 + 1)     => [ nil 2 ])
       (fact "1 + 2 = 3"     (calculator 1 + 2)     => [ nil 3 ])
       (fact "1 + 1 + 1 = 3" (calculator 1 + 1 + 1) => [ nil 3 ])
       (fact "1 + 2 * 3 = 9" (calculator 1 + 2 * 3) => [ nil 9 ])
       )
