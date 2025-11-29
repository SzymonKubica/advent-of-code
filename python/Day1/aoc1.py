from curses.ascii import isdigit


def getting_input():
    file = open("inpt.txt", "r")
    inpt = str(file.read()).splitlines()
    file.close()
    
    return inpt

def filter_input(inpt: list):
    filtered_input = []
    for line in inpt:
        filtered_input.append(''.join(filter(lambda x: x.isdigit(), line)))

    return filtered_input

def get_calibration_values(filtered_input: list):
    calibration_values = []
    for line in filtered_input:
        if len(line) >= 2:
            calibration_values.append(line[0] + line[len(line) - 1])
        else:
            calibration_values.append(line[0] + line[0])

    return calibration_values


if __name__ == '__main__':
    inpt = getting_input()
    filtered_input = filter_input(inpt)
    calibration_values = get_calibration_values(filtered_input)
    calibration_values = list(map(int, calibration_values))
    print(sum(calibration_values))