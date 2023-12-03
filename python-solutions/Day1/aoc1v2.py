from curses.ascii import isdigit

numbers = {
    1: 'one'
    2: 'two'
    3: 'three'
    4: 'four'
    5: 'five'
    6: 'six'
    7: 'seven'
    8: 'eight'
    9: 'nine'
}

def getting_input():
    file = open("inpt.txt", "r")
    inpt = str(file.read()).splitlines()
    file.close()
    
    return inpt
# threeightwo > 3ight2 
# .....eightwo| > ....8wo
def text_to_digit(inpt: list):
    for line in inpt:
        if line.startswith(numbers{i} for i in range(1, 9)):
            line.replace(range{i}, str(i))
            

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