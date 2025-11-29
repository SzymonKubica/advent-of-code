from solution import Solution
from typing import List, Tuple


def filter_input(input: list):
    filtered_input = []
    for line in input:
        filtered_input.append("".join(filter(lambda x: x.isdigit(), line)))

    return filtered_input


def get_calibration_values(filtered_input: list):
    calibration_values = []
    for line in filtered_input:
        if len(line) >= 2:
            calibration_values.append(line[0] + line[len(line) - 1])
        else:
            calibration_values.append(line[0] + line[0])

    return calibration_values


class Day1(Solution):
    def first_part(self, input_file: str):
        file = open(input_file, "r")
        input = str(file.read()).splitlines()
        file.close()
        filtered_input = filter_input(input)
        calibration_values = get_calibration_values(filtered_input)
        print(sum(list(map(int, calibration_values))))

    def second_part(self, input_file: str):
        file = open(input_file, "r")
        input = str(file.read()).splitlines()
        file.close()
        calibration_values = get_calibration_values_part2(input)
        calibration_values = list(map(int, calibration_values))
        print(sum(calibration_values))


def parse_input_lists(input: str) -> Tuple[List[int], List[int]]:
    left = []
    right = []
    for line in input.splitlines():
        first, second = line.split("   ")
        left.append(int(first))
        right.append(int(second))

    return (left, right)


numbers = {
    1: "one",
    2: "two",
    3: "three",
    4: "four",
    5: "five",
    6: "six",
    7: "seven",
    8: "eight",
    9: "nine",
}


def first(line: str):
    first_digit = ""

    for j in range(len(line)):
        for i in range(1, 10):
            if line[0].isdigit():
                first_digit = line[0]
                return first_digit
            elif line.startswith(numbers[i], j) or line.startswith(str(i), j):
                first_digit = str(i)
                return first_digit


def last(line: str):
    last_digit = ""

    for j in range(len(line)):
        for i in range(1, 10):
            if line[0].isdigit():
                last_digit = line[0]
                return last_digit
            elif line.startswith("".join(reversed(numbers[i])), j) or line.startswith(
                str(i), j
            ):
                last_digit = str(i)
                return last_digit


def get_calibration_values_part2(inpt: list):
    calibration_values = []
    first_digit = ""
    last_digit = ""

    for line in inpt:
        first_digit = first(line)
        last_digit = last("".join(reversed(line)))
        calibration_values.append(first_digit + last_digit)

    return calibration_values
