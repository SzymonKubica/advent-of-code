# Advent of Code Code Playground

This repository contains my solutions for AoC for various years. The codebase
is set up to allow for implementing solutions in different languages and then
using a single executable in the root of this repo to execute them.

## Structure of the Codebase

The structure of the project is as follows:
```
.
├── 2023
│   ├── input-files
│   ├── python
│   └── rust
├── 2024
│   ├── cpp
│   ├── elixir
│   ├── input-files
│   └── rust
├── README.md
├── runner
└── utils

```
In the directories corresponding to the yearly solutions, each of the folders
is suppoesed to contain solutions implemented in the specific language.

Each of these directories should be a separate project that exposes a single
executable script `run` that can be used to run solutions to the daily challenges.

An example cmd line using that run script can be seen below:
```shell
./run.sh 12 1 ../inputs/day12
```
Where the arguments in order specify the day for which we want to run the solution,
the part of the puzzle that is to be executed and optional paths to the input and
output files. If the output and input files are not provided, the script should
default ot printing output to the console and sourcing it from the file under:
```
./<year>/input-files/day-<day>-puzzle-input
```

The run script should instruct the target language which day should be executed,
if we need to use the part 1 or part 2 logic and where to find the input.

TODO: implement the default input file sourcing.



