(ns immutable.examples
  (:require [clojure.set :refer :all]))

;; immutable lists
(list 1 2 3 4)

'(1 2 3 4)

;; unchanged by ops
(def list-a '(1 2 3 4))

list-a

(def list-b (cons 0 list-a))

list-b
list-a

(def list-c (pop list-a))

list-c
list-a

;; immutable vectors (indexed random access associative)
(vector 3 4 5)
[3 4 5]

(def vector-a [1 2 3 4])

vector-a

(def vector-b (conj vector-a 5))


vector-b
vector-a

(def vector-c (pop vector-a))

vector-c
vector-a

(def vector-d (assoc vector-a 1 10))

vector-d

(def vector-e (update vector-a 2 inc))

vector-e

;; immutable sets
(hash-set 1 2 3 4)
#{1 2 3 4}

(def set-a #{1 2 3 4})

set-a

(def set-b (conj set-a 5))

set-b

(def set-c (disj set-a 2))

set-c

(def set-d (union set-a set-b))

set-d

(def set-e (intersection set-a set-b))

set-e
