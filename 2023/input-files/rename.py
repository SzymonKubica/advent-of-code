
import os
from os import listdir
from os.path import isfile, join
def main():
    print("Hello Szymon, time to rename some messy flies")


    base_dir = "./"
    for file_name in listdir(base_dir):
        print(str(join(base_dir, file_name)))
        rename_file(file_name)


def rename_file(file_name: str):
    parts = file_name.split("-")
    if not len(parts) == 2:
        return
    day = int(parts[0][3:])
    is_test = "test" in parts[1]
    test_number = int(parts[1][4:]) if is_test else 0
    print(f"day: {day}, is_test: {is_test}, test_number: {test_number}")
    file_kind = "example" if is_test else "puzzle-input"
    suffix = f"-{test_number - 1}"if is_test and test_number > 1 else ""
    new_file_name = f"day-{day}-{file_kind}{suffix}"
    print(new_file_name)
    os.rename(file_name, new_file_name)

if __name__ == "__main__":
    main()

