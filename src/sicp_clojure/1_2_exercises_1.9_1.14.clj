(ns sicp-clojure.1-2-exercises
  (:require [clojure.test :as t]
            [clojure.math.numeric-tower :as m :refer (expt sqrt abs)]))

;;; Exercise 1.9
;; Using the substitution model, illustrate the process generated by each procedure
;; in evaluating (+ 4 5). Are these processes iterative or recursive?

(defn custom-plus [a b]
  (if (= a 0)
    b
    (inc (custom-plus (dec a) b))))

;; both in applicative-order:
;; (custom-plus 4 5)
;; (if (= 4 0) 5 (inc (custom-plus (dec 4) 5)))
;; ... (inc (custom-plus 3 5)))
;; ... (inc -deferred- (if (= 3 0) 5) (inc (custom-plus (dec 3) 5)))
;; ... (inc -deferred- ... (inc (custom-plus 2 5)))
;; ... (inc -deferred- ... (inc -deferred- (if (= 2 0) 5) ...
;;
;; It is a recursive process because the interpreter needs to wait for the evaluation of
;; custom-plus before proceeding with inc, therefore keeping track of the calls to
;; custom-plus and deferring inc.
;; Other ways to say it are:
;; 1) custom-plus is not the last evaluation of the expression.
;; 2) custom-plus is not tail-recursive.

(defn custom-plus* [a b]
  (if (= a 0)
    b
    (custom-plus* (dec a) (inc b))))

;; (custom-plus* 4 5)
;; (if (= 4 0) 5 (custom-plus* (dec 4) (inc 5)))
;; (if (= 4 0) 5 (custom-plus* 3 6)
;; (if (= 3 0) 6 (custom-plus* (dec 3) (inc 6)))
;; (if (= 3 0) 6 (custom-plus* 2 7))
;; (if (= 2 0) 7 (custom-plus* (dec 2) (inc 7)))
;; (if (= 2 0) 7 (custom-plus* 1 8))
;; (if (= 1 0) 8 (custom-plus* (dec 1) (inc 8)))
;; (if (= 1 0) 7 (custom-plus* 0 9))
;; (if (= 0 0) 9 (custom-plus* (dec 0) (inc 9)))
;; 9
;;
;; It is an iterative process because primitive expressions are just chained one
;; after another, the state is self-contained within calls and the stack doesn't grow.
;; Other ways to say it are:
;; 1) custom-plus* is the last evaluation of the expression.
;; 2) custom-plus* is tail-recursive.


;;; Exercise 1.10 - Ackermann's function
(defn A [x y]
  (cond (= y 0) 0
        (= x 0) (* 2 y)
        (= y 1) 2
        :else (A (- x 1)
                 (A x (- y 1)))))

(defn f [n] (A 0 n))
(defn g [n] (A 1 n))
(defn h [n] (A 2 n))
(defn k [n] (* 5 n n))

;;; Exercise 1.11
;; A function f is defined by the rule that f(n) = n if n<3 and f(n) = f(n - 1)
;; + 2f(n - 2) + 3f(n - 3) if n> 3. Write a procedure that computes f by means
;; of a recursive process. Write a procedure that computes f by means of an
;; iterative process.

;; recursive
(defn f-recursive [n]
  (if (< n 3)
    n
    (+ (f-recursive (- n 1))
       (* 2 (f-recursive (- n 2)))
       (* 3 (f-recursive (- n 3))))))

;; iterative
(defn- f-iter-helper [n-1 n-2 n-3 i]
  (if (< i 3)
    n-1
    (f-iter-helper (+ n-1 (* 2 n-2) (* 3 n-3))
                   n-1
                   n-2
                   (- i 1))))

(defn f-iterative [n]
  (if (< n 3)
    n
    (f-iter-helper 2 1 0 n)))

;; iterative with Clojure's loop construct
(defn f-loop [n]
  (if (< n 3)
    n
    (loop [n-1 2
           n-2 1
           n-3 0
           i n]
      (if (< i 3)
        n-1
        (recur (+ n-1 (* 2 n-2) (* 3 n-3))
               n-1
               n-2
               (dec i))))))


;;; Exercise 1.12
;; Write a procedure that computes elements of Pascal's triangle by means of
;; a recursive process.

(defn pascal [row col]
  "Calculates the Pascal's triangle position given row and column."
  (if (or (= col 0) (= row col)) ;; (= row 0) already covered by (= row col)
    1
    (+ (pascal (- row 1) (- col 1))
       (pascal (- row 1) col))))

(defn- double-loop [i j]
  (if (> i -1)
    (if (< j i)
      (do (print (pascal i j) " ")
        (double-loop i (+ j 1)))
      (do (print (pascal i j) "\n")
        (double-loop (- i 1) 0)))))

(defn draw-pascal [rows]
  "Prints the Pascal's triangles from top to the input number of rows."
  (double-loop (- rows 1) 0))

;; (draw-pascal 5) ; Uncomment to draw the Pascal's triangle.


;;; Exercise 1.13
;; Prove that Fib(n) is the closest integer to (phi^n)/(sqrt 5), where phi=(1 + sqrt 5)/2.
;; Hint: Let psi = (1 - sqrt 5)/2. Use induction and the definition of the Fibonacci numbers
;; (see section 1.2.2) to prove that Fib(n) = (phi^n - psi^n)/(sqrt 5).
;;
;; For a detailed proof on paper see both:
;; http://www.kendyck.com/math/sicp/ex1-13.xml

;; We will simply calculate the formula and check whether the result is within 1/2 to Fib(n).
;; See tests below.

(def phi (/ (+ 1 (m/sqrt 5)) 2))
(def psi (/ (- 1 (m/sqrt 5)) 2))

(defn hint [n]
  (/ (- (m/expt phi n) (m/expt psi n)) (m/sqrt 5)))

(defn- fib-iter [a b counter]
  (if (= counter 0)
    b
    (recur (+ a b) a (- counter 1))))

(defn fib [n]
  (fib-iter 1 0 n))


;;; Exercise 1.14
;; Draw the tree illustrating the process generated by the count-change procedure of section
;; 1.2.2 in making change for 11 cents. What are the orders of growth of the space and number
;; of steps used by this process as the amount to be changed increases?
;;
;; It is easy to see that the number of steps grows by some factor every time the amount
;; increases by the amount of some denomination. If the increase spans across multiple denominations
;; we will add of course many more steps (50 cent will generate steps for 25, 10, 5 and 1 cent).
;; The order of growth is then related to the denomination(s) still to process at each step.
;;
;; Starting from the end cases, the following shows that for each partial call to (cc n 1),
;; 2n steps are generated. Theta(n) = O(n).
;; (cc 11 1)
;;     |     \
;; (cc 11 0) (cc 10 1)
;;     =0        |    \
;;           (cc 10 0) (cc 9 1)
;;               =0       |...   \...
;;                     (cc 2 0)  (cc 1 1)
;;                         =0       |     \
;;                               (cc 1 0) (cc 0 1)
;;                                  =0      =1
;;
;; For (cc n 2) the process generates n/(denomination at position 2 = 5 cent) steps, unfolding
;; one (cc n 1) each. Therefore, the number of steps in this case is (2n)(n/5). Theta(n) = O(n^2).
;; (cc 11 2)
;;     |     \
;; (cc 11 1) (cc 6 2)
;;  22 steps     |    \
;;           (cc 6 1) (cc 1 2)
;;            12 steps    |    \
;;                     (cc 1 1)  (cc -4 2)
;;                      2 steps      =0
;;
;; Following the same reasoning for (cc 11 3) (cc 11 4) and (c 11 5) it is easy to show that
;; the total order of growth is Theta(n) = O(n^5).
;;
;; For a more precise and clear calculation:
;; http://www.billthelizard.com/2009/12/sicp-exercise-114-counting-change.html
;
;; Moreover, count-change is a tree-recursive process which grows in space proportionally with
;; the depth of the tree, which is linear with the input n.


(t/deftest tests
  ;; (A 1 10) - substitution for relevant branches only
  ;; (: else (A (- 1 1) (A 1 (- 10 1)))))
  ;; (: else (A 0 (A 1 9))))
  ;;          |    |
  ;;          |   (:else (A 0 (A 1 8)))
  ;;          |           |    |
  ;;          |           |    |
  ;;          |           |    (:else (A 0 (A 1 1)))
  ;;          |           |            |    |
  ;;          |           |            |   ((= 1 1) 2)
  ;;          |           |           ((= 0 0) (* 2 2))
  ;;          |           ((= 0 0) (* 2 256))
  ;;          ((= 0 0) (* 2 512))
  ;;
  ;; If x is not zero, the first call goes to the :else branch, which recursively calls A with x-1
  ;; as first argument and another recursive call to A for calculating y as second argument.
  ;;
  ;; The calls with x-1 will all fall into (= x 0) (* 2 y) therefore needing the value of y and,
  ;; when obtained, returning it doubled. The stopping condition of the second-level
  ;; recursion is when (= y 1) is matched, which will return 2. Combining recursive doubling
  ;; with 2 as starting point leads to (A 1 y) = 2^y.
  (t/is (= (m/expt 2 10) (A 1 10)))

  ;; (A 2 4) - substitution for relevant branches only
  ;; (: else (A 1 (A 2 3)))
  ;;          |    |
  ;;          |   (:else (A 1 (A 2 2)))
  ;;          |           |    |
  ;;          |           |    (: else (A 1 (A 2 1)))
  ;;          |           |             |    |
  ;;          |           |             |    ((= 1 1) 2)
  ;;          |           |            see before 2^2
  ;;          |           see before 2^4
  ;;          see before 2^16
  ;;
  ;; The double recursion in this case results in the convolution of the 2-to-the-y functions:
  ;; (A 2 y) = (A 1 (A 2 y-1)) = 2^(2^2^...) for y-1 times (see below)
  (t/is (= (m/expt 2 (m/expt 2 (m/expt 2 2))) (A 2 4)))
  (t/is (= (m/expt 2 (m/expt 2 2)) (A 2 3)))

  ;; (A 3 3)
  ;; One additional level of convolution is applied every time x is incremented:
  ;; (A 3 3) = (A 2 4) = 2^(2^2^...) -> for y times
  (t/is (= (m/expt 2 (m/expt 2 (m/expt 2 2))) (A 3 3)))

  ;; As per Ackermann's definition, f computes 2*n
  (t/is (= (* 2 15) (f 15)))

  ;; As seen above, g computes 2^n (Knuth single up-arrow)
  (t/is (= (m/expt 2 15) (g 15)))

  ;; As seen above, h computes 2^2^2^... n exponentations (Knuth double up-arrow)
  (t/is (= (m/expt 2 (m/expt 2 (m/expt 2 2))) (h 4)))
  (t/is (= 0 (f-recursive 0)))
  (t/is (= 1 (f-recursive 1)))
  (t/is (= 2 (f-recursive 2)))
  (t/is (= 4 (f-recursive 3)))
  (t/is (= 11 (f-recursive 4)))
  (t/is (= 335 (f-recursive 8)))
  (t/is (= 0 (f-iterative 0)))
  (t/is (= 1 (f-iterative 1)))
  (t/is (= 2 (f-iterative 2)))
  (t/is (= 4 (f-iterative 3)))
  (t/is (= 11 (f-iterative 4)))
  (t/is (= 335 (f-iterative 8)))
  (t/is (= 0 (f-loop 0)))
  (t/is (= 1 (f-loop 1)))
  (t/is (= 2 (f-loop 2)))
  (t/is (= 4 (f-loop 3)))
  (t/is (= 11 (f-loop 4)))
  (t/is (= 335 (f-loop 8)))
  (t/is (= 1 (pascal 4 0)))
  (t/is (= 1 (pascal 4 4)))
  (t/is (= 6 (pascal 4 2)))
  (t/is (>= 0.5 (- (hint 0) (fib 0))))
  (t/is (>= 0.5 (- (hint 1) (fib 1))))
  (t/is (>= 0.5 (- (hint 2) (fib 2))))
  (t/is (>= 0.5 (- (hint 10) (fib 10))))
  (t/is (>= 0.5 (- (hint 50) (fib 50)))))
