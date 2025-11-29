from solution import Solution
from typing import List, Tuple


class Day2(Solution):
    def first_part(self, input_file: str):
        inpt = getting_input()
        spliting(inpt)

    def second_part(self, input_file: str):
        pass


def getting_input():
    file = open("input.txt", "r")
    inpt = str(file.read()).splitlines()
    file.close()

    return inpt

def spliting(inpt: list):
    for i in range(len(inpt)):
        inpt[i] = inpt[i].split(': ')
        inpt[i][1] = inpt[i][1].split('; ')
        for j in range(len(inpt[i][1])):
            inpt[i][1][j] = inpt[i][1][j].split(', ')

    #for subset in line[1]:
    #    subset = subset.split(', ')

    print(inpt)

