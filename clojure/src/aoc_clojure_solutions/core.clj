(ns aoc-clojure-solutions.core
  (:gen-class)
  (:require [aoc-clojure-solutions.year2022.day-1 :as day-1-2022]))

(defn -main
  "Main entrypoint that dispatches into solutions files."
  [& args]
  (println (day-1-2022/first_part "input-files")))

[1 2 3]