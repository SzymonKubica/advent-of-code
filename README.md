# Advent of Code Code Playground

This repository contains my solutions for AoC for the past few years. The
codebase is set up to allow for implementing solutions in different languages
and then using a single executable in the root of this repo to execute them.

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
is supposed to contain solutions implemented in the specific language.

Each of these directories should be a separate project that exposes a single
executable script `run` which can be used to run solutions to the daily
challenges.

An example command line using that run script can be seen below:
```shell
./run.sh 12 1 ../inputs/day12
```
The arguments above in order specify the day for which to run the solution,
the part of the puzzle that is to be executed and optional path to the input
file. If the input file is not provided, the script runner script will default
to sourcing its input from the file under:
```
./<year>/input-files/day-<day>-puzzle-input
```
The run script should pass the supplied arguments to the implementation in the
target language. The solution should then print all of its logs to stdout /
stderr and the final solution of the puzzle should be logged there as well.

The runner script will then stream the output to the console as is appears
as well as capture it so that it can later be saved in a specified file.

## Setup instructions

Prerequisites:
- you have cloned this repo to your local machine
- you have installed [all of the stuff](https://www.rust-lang.org/tools/install)
  that is required for compiling `rust` programs

1. Go to the runner directory and build the `runner` script using:
    ```shell
    cargo build --release
    ```
2. Source the setup script that will alias the runner build output:
    ```shell
    source setup.sh
    ```
3. Now you can use the `runner` script to execute the solutions like so:
    ```shell
    runner --year 2024 --day 1 --part 2 --language rust
    ```
