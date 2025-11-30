#include <iostream>
#include "src/2022/day_1.hpp"

int main(int argc, char *argv[]) {
  if (argc != 5) {
    std::cerr << "Usage: " << argv[0] << " <year> <day> <part> <input_file>"
              << std::endl;
    return 1;
  }
  std::cout << "Welcome to advent of code solutions in c++" << std::endl;

  int year = std::stoi(argv[1]);
  int day = std::stoi(argv[2]);
  int part = std::stoi(argv[3]);
  std::string input_file = argv[4];

  std::cout << "Running solution for year: " << year << ", day: " << day
            << ", part: " << part << ", input file: " << input_file << "."
            << std::endl;
  return 0;
}
