year=$1
day=$2
part=$3
input=$4

time java -jar -ea $AOC_ROOT_DIR/clojure/target/uberjar/aoc-clojure-solutions-0.1.0-SNAPSHOT-standalone.jar $year $day $part $input

