import sys
import importlib
from solution import Solution

def main():
    args = sys.argv[1:]
    day = args[0]
    part = args[1]
    input_file = args[2]

    #Disable logging to only measure raw perf
    #print(f"day: {day}, part: {part}, input file: {input_file}")

    solution_module = importlib.import_module(f"day{args[0]}")
    solution_class = getattr(solution_module, f"Day{args[0]}")

    solution: Solution = solution_class()

    if args[1] == "1":
        solution.first_part(input_file)
    else:
        solution.second_part(input_file)


if __name__ == "__main__":
    main()

