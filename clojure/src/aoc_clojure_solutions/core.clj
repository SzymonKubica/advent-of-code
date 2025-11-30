(ns aoc-clojure-solutions.core
  (:gen-class)
  (:require [aoc-clojure-solutions.year2022.day-1 :as day-1-2022]))

(def solutions-per-year {2022
                         {1 [day-1-2022/first_part day-1-2022/second_part]}})

(defn get-solution
  "Performs a lookup in the solutions per year map above to get the required implementation"
  [year day part] (get (get (get solutions-per-year year) day) (dec part)))

(defn -main
  "Main entrypoint that dispatches into solutions files."
  [& args]
  (let [year (Integer/parseInt (first args))
        day (Integer/parseInt (nth args 1))
        part (Integer/parseInt (nth args 2))
        input-file (nth args 3)]
    (println "Executing Clojure solution for  year: " year ", day: " day ", part: " part ".")
    (println "Getting input from file: " input-file)
    (let [solution (get-solution year day part)]
      (solution input-file))
    )
  )

(comment
  (-main "2022" "1" "2" "../input-files/2022/day-1-puzzle-input"))