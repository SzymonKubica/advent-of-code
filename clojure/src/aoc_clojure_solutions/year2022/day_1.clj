(ns aoc-clojure-solutions.year2022.day-1
  (:require [clojure.string :as str]))

(defn tail [vector] (subvec vector 1))

(defn get-vectors-splitting-on-empty-lines-explicit-recur
  "Given a list of string lines, each line being either blank or containing a single integer,
  it returns a vector of vectors of integers, delimited on blank lines.
  This is a version of the function that uses explicit recursion, this
  is not actually used by the solution, but I left it here as a trace
  of my learning process.
  "
  [lines vectors curr]
  (if (empty? lines)
    vectors                                                 ; Accumulation is complete
    (if (str/blank? (first lines))
      ; Blank line: need to end the current vector and add it to the accumulated list.
      (get-vectors-splitting-on-empty-lines-explicit-recur (tail lines) (conj vectors curr) [])
      ; Line with a number: we parse the number and append it to the current vector
      (get-vectors-splitting-on-empty-lines-explicit-recur (tail lines) vectors (conj curr (Integer/parseInt (first lines)))))))


(defn get-vectors-splitting-on-empty-lines
  "Given a list of string lines, each line being either blank or containing a single integer,
  it returns a vector of vectors of integers, delimited on blank lines."
  [lines]
  (->> lines
       (partition-by str/blank?)
       (remove #(str/blank? (first %)))
       (map #(map (fn [number-str] (Integer/parseInt number-str)) %))))

(defn read-file-lines
  "Reads all lines from a given file."
  [file-name] (-> file-name slurp str/split-lines))

(defn first_part [file-name]
  (let [file-lines (read-file-lines file-name)
        food-carried-by-each-elf (get-vectors-splitting-on-empty-lines file-lines)
        total-calories-per-elf (map #(reduce + %) food-carried-by-each-elf)
        max-calories (apply max total-calories-per-elf)
        ]
    (println food-carried-by-each-elf)
    (println total-calories-per-elf)
    (println max-calories)
    max-calories))

(defn second_part [file-name]
  (let [file-lines (read-file-lines file-name)
        food-carried-by-each-elf (get-vectors-splitting-on-empty-lines file-lines)
        total-calories-per-elf-descending (vec (reverse (sort (map #(reduce + %) food-carried-by-each-elf))))
        top-three-elves (subvec total-calories-per-elf-descending 0 3)
        top-three-elves-sum (reduce + top-three-elves)
        ]
    (println food-carried-by-each-elf)
    (println total-calories-per-elf-descending)
    (println top-three-elves)
    (println top-three-elves-sum)
    top-three-elves-sum))

(first_part "../input-files/2022/day-1-puzzle-input")
(second_part "../input-files/2022/day-1-puzzle-input")