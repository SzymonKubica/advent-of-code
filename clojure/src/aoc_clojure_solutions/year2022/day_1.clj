(ns aoc-clojure-solutions.year2022.day-1
  (:require [clojure.string :as str]))

(defn get-calorie-count-vectors
  [lines vectors current-vector]
  (if (empty? lines)
    vectors
    (if (str/blank? (get lines 0))
      (get-calorie-count-vectors (subvec lines 1) (conj vectors current-vector) [])
      (get-calorie-count-vectors (subvec lines 1) vectors (conj current-vector (Integer/parseInt (get lines 0)))))))

(defn first_part [file-name] (println "Supplied file name" file-name)
  (let [file-lines (-> file-name slurp str/split-lines)
        calorie-count-vectors (get-calorie-count-vectors file-lines [] [])
        calorie-sums (map (fn [vector] (reduce + vector)) calorie-count-vectors)
        calorie-sums-sorted (vec (sort calorie-sums))
        max-calories (get calorie-sums-sorted (dec (count calorie-sums-sorted)))
        ]
    (println calorie-count-vectors)
    (println calorie-sums)
    (println max-calories)
    calorie-count-vectors))


(first_part "../input-files/2022/day-1-puzzle-input")

(System/getProperty "user.dir")
