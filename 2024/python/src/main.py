import sys
import importlib
from solution import Solution

def main():
    args = sys.argv[1:]
    day = args[0]
    part = args[1]
    input_file = args[2]

    solution_module = importlib.import_module(f"day{day}")
    solution_class = getattr(solution_module, f"Day{day}")

    solution: Solution = solution_class()

    if part == "1":
        solution.first_part(input_file)
    else:
        solution.second_part(input_file)


if __name__ == "__main__":
    main()

