from curses.ascii import isdigit

numbers = {
    1 : 'one',
    2 : 'two',
    3 : 'three',
    4 : 'four',
    5 : 'five',
    6 : 'six',
    7 : 'seven',
    8 : 'eight',
    9 : 'nine',
    }


def getting_input():
    file = open("inpt.txt", "r")
    inpt = str(file.read()).splitlines()
    file.close()
    
    return inpt


def first(line: str):
    first_digit = ''

    for j in range(len(line)):
        for i in range(1, 10):
            if line[0].isdigit():
                first_digit = line[0]
                return first_digit
            elif line.startswith(numbers[i], j) or line.startswith(str(i), j):
                first_digit = str(i)
                return first_digit


def last(line: str):
    last_digit = ''

    for j in range(len(line)):
        for i in range(1, 10):
            if line[0].isdigit():
                last_digit = line[0]
                return last_digit
            elif line.startswith(''.join(reversed(numbers[i])), j) or line.startswith(str(i), j):
                last_digit = str(i)
                return last_digit


def get_calibration_values(inpt: list):
    calibration_values = []
    first_digit = ''
    last_digit = ''

    for line in inpt:
       first_digit = first(line)
       last_digit = last(''.join(reversed(line)))
       calibration_values.append(first_digit + last_digit)

    return calibration_values


if __name__ == '__main__':
    inpt = getting_input()
    calibration_values = get_calibration_values(inpt)
    calibration_values = list(map(int, calibration_values))
    print(sum(calibration_values))