# Advent of Code Playground

This repository contains my solutions for AoC for the past few years. The
codebase is set up to allow for implementing solutions in different languages
and then using a single executable in the root of this repository to execute them.

## Project Structure

The structure of the project is as follows:
```
.
├── README.md
├── input-files
│   ├── 2023
│   └── 2024
├── java
│   ...
├── python
│   ...
├── runner
│   ...
├── rust
│   ...
├── setup.sh
└── utils
    ├── README.md
    └── rust

```
We store all puzzle input files in the `input-files` directory. Solution
impelementations in different languages should source the input files from
there.

Each of the language-specific directories is a separate project that exposes a
single executable script `run.sh` which can be used to run solutions to the
challenges.

An example command line using that run script can be seen below:
```shell
./run.sh 2024 12 1 ../inputs-files/2023/day-10-puzzle-input
```
The arguments above specify the year and day for which to run the solution,
the part of the puzzle that is to be executed and optional path to the input
file. If the input file is not provided, the script runner script will default
to sourcing its input from the file under:
```
./input-files/<year>/day-<day>-puzzle-input
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
  that is required for compiling `rust` programs (or any other language you
  want to work with).

1. Build the runner tool by executing the build script:
    ```shell
    build.sh
    ```
2. Source the setup script that will alias the runner build output and set up
   your environment:
    ```shell
    source setup.sh
    ```
3. Now you can use the `runner` script-utility to execute the solutions like so:
    ```shell
    runner --year 2024 --day 1 --part 2 --language rust
    ```
4. For languages that require compilation, you can specify the `--build` flag
   to ensure that the `runner` builds the latest version of the solutions project.
   The build is done by delegating to the language-specific `build.sh` script
   so please ensure your chosen language has that provided.
    ```shell
    runner --year 2024 --day 1 --part 2 --language java --build
    ```

## Onboarding a new language

1. Create a new folder with the name of your new language in which  you want to
   add solutions. Example: when adding solutions for c++, one can create a new
   directory under `cpp/`

2. Set up the `run.sh` and `build.sh` scripts (you can look at existing directories
   for reference). Important:
   - `build.sh` should compile all of your solutions (if your chosen language
   requires compilation)
   - `run.sh` runs your solutions for requested year day and part, like so:
   ```shell
   ./run.sh 2024 12 1 ../inputs/day12
   ```
3. You can now use the main `runner` script to build and execute your solutions, e.g:
    ```shell
    runner --year 2024 --day 1 --part 2 --language cpp --build
    ```
