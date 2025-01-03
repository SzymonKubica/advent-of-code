from solution import Solution
from typing import List, Tuple


class Day1(Solution):
    def first_part(self, input_file: str):
        file = open(input_file, "r")
        input = file.read()
        left, right = parse_input_lists(input)
        output = sum(
            map(lambda tup: abs(tup[0] - tup[1]), zip(sorted(left), sorted(right)))
        )
        print("Difference", output)

    def second_part(self, input_file: str):
        file = open(input_file, "r")
        input = file.read()
        left, right = parse_input_lists(input)

        similarity_score = sum(map(lambda x: x * len(list(filter(lambda y: y == x, right))), left))
        print("Similarity score: ", similarity_score)


def parse_input_lists(input: str) -> Tuple[List[int], List[int]]:
    left = []
    right = []
    for line in input.splitlines():
        first, second = line.split("   ")
        left.append(int(first))
        right.append(int(second))

    return (left, right)
