(ns sicp-clojure.1-3-samples
  (:require [clojure.test :as t]
            [clojure.math.numeric-tower :as m :refer (sqrt abs)]))

;;; 1.3.1  Procedures as Arguments

(defn cube [n] (* n n n))

;; common pattern in the following procedures
(defn- sum-integers [a b]
  (if (> a b)
      0
      (+ a (sum-integers (+ a 1) b))))

(defn- sum-cubes [a b]
  (if (> a b)
      0
      (+ (cube a) (sum-cubes (+ a 1) b))))

(defn- pi-sum [a b]
  (if (> a b)
      0
      (+ (/ 1.0 (* a (+ a 2))) (pi-sum (+ a 4) b))))

;; high-order function
(defn sum [term a next-a b]
  "Sums from a through b."
  (if (> a b)
    0
    (+ (term a) (sum term (next-a a) next-a b))))

(defn sum-cubes* [a b]
  (sum cube a inc b))

(defn sum-integers* [a b]
  (sum identity a inc b))

(defn pi-sum* [a b]
  (defn pi-term [x]
    (/ 1.0 (* x (+ x 2))))
  (defn pi-next [x]
    (+ x 4))
  (sum pi-term a pi-next b))

(defn integral [f a b dx]
  (defn add-dx [x] (+ x dx))
  (* (sum f (+ a (/ dx 2.0)) add-dx b) dx))


;;; 1.3.3  Procedures as General Methods
;; Finding roots of equations by the half-interval method
(defn close-enough? [x y] (< (m/abs (- x y)) 0.001))

(defn- sin [x] (Math/sin x))

(defn- cos [x] (Math/cos x))

(defn search [f neg-point pos-point]
  (let [midpoint (/ (+ neg-point pos-point) 2.0)]
    (if (close-enough? neg-point pos-point)
      midpoint
      (let [test-value (f midpoint)]
        (cond (pos? test-value) (search f neg-point midpoint)
              (neg? test-value) (search f midpoint pos-point)
              :else midpoint)))))

(defn half-interval-method [f a b]
  (let [a-value (f a)
        b-value (f b)]
    (cond (and (neg? a-value) (pos? b-value)) (search f a b)
          (and (neg? b-value) (pos? a-value)) (search f b a)
          :else (throw (RuntimeException. (str "Values " a " & " b " are not of opposite sign."))))))

(def tolerance 0.00001)

(defn fixed-point [f first-guess]
  (defn close-enough? [v1 v2]
    (< (m/abs (- v1 v2)) tolerance))
  (defn try-it [guess]
    (let [next-guess (f guess)]
      ;; (println guess next-guess) ; Uncomment for Exercise 1.36
      (if (close-enough? guess next-guess)
        next-guess
        (try-it next-guess))))
  (try-it first-guess))

;; The following implementation doesn't converge.
(defn sqrt-infinite [x]
  (fixed-point (fn [y] / x y) 1.0))

;; Average damping solves the problem.
(defn sqrt* [x]
  (fixed-point (fn [y] (/ (+ y (/ x y)) 2)) 1.0))


(t/run-tests)
(t/deftest tests
  (t/is (= 3025 (sum-cubes 1 10)))
  (t/is (= 55 (sum-integers 1 10)))
  (t/is (close-enough? Math/PI (* 8 (pi-sum 1 1000)))) ; doesn't return a result < 0.001 from PI
  (t/is (= (sum-cubes 1 10) (sum-cubes* 1 10)))
  (t/is (= (sum-integers 1 10) (sum-integers* 1 10)))
  (t/is (= (pi-sum 1 1000) (pi-sum* 1 1000)))
  (t/is (close-enough? 0.25 (integral cube 0 1 0.01)))
  (t/is (close-enough? 0.25 (integral cube 0 1 0.001)))
  (t/is (close-enough? Math/PI (half-interval-method sin 2.0 4.0)))
  (t/is (close-enough? 1.89306640625 (half-interval-method (fn [x] (- (* x x x) (* 2 x) 3))
                                             1.0
                                             2.0)))
  (t/is (close-enough? 0.7390822985224023 (fixed-point cos 1.0)))
  (t/is (close-enough? 1.2587315962971173 (fixed-point (fn [x] (+ (sin x) (cos x))) 1.0)))
  (t/is (close-enough? (m/sqrt 2) (sqrt* 2))))