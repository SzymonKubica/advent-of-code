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


if __name__ == '__main__':
    inpt = getting_input() 
    spliting(inpt)