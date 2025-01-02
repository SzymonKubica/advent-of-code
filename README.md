# Advent of Code Code Playground

This repository contains my solutions for AoC for various years. The codebase
is set up to allow for implementing solutions in different languages and then
using a single executable in the root of this repo to execute them.

## Structure of the Codebase

The structure of the project is as follows:
```
.
├── 2023
│   ├── python
│   └── rust
├── 2024
│   └── rust
├── account-ownership-code
└── README.md

```
In the directories corresponding to the yearly solutions, each of the folders
is suppoesed to contain solutions implemented in the specific language.

Each of these directories should be a separate project that exposes a single
executable `run` that can be used to run solutions to the daily challenges.

An example cmd line using that run script can be seen below:
```shell
./run --day 12 --part 1 --input-file ../inputs/day12
```

The run script should instruct the target language which day should be executed,
if we need to use the part 1 or part 2 logic and where to find the input.

TODO: implement the default input file sourcing.



