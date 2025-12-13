#include <iostream>
#include <unordered_map>
#include <vector>
#include "src/2022/day_1.hpp"
#include "src/2025/day_1.hpp"
#include "src/2025/day_2.hpp"
#include "src/2025/day_3.hpp"
#include "src/2025/day_4.hpp"
#include "src/2025/day_5.hpp"
#include "src/2025/day_6.hpp"
#include "src/2025/day_7.hpp"
#include "src/2025/day_8.hpp"
#include "src/solution.hpp"

int main(int argc, char *argv[])
{
        if (argc != 5) {
                std::cerr << "Usage: " << argv[0]
                          << " <year> <day> <part> <input_file>" << std::endl;
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

        std::unordered_map<int, std::vector<Solution *>> yearly_solutions;

        yearly_solutions[2022] = std::vector<Solution *>{new Year2022Day1()};
        yearly_solutions[2025] = std::vector<Solution *>{
            new Year2025Day1(), new Year2025Day2(), new Year2025Day3(),
            new Year2025Day4(), new Year2025Day5(), new Year2025Day6(),
            new Year2025Day7(), new Year2025Day8()};

        auto solution = yearly_solutions[year][day - 1];

        if (part == 1) {
                solution->first_part(input_file);
        } else {
                solution->second_part(input_file);
        }
        return 0;
}
