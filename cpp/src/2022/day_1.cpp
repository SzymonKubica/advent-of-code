#include "day_1.hpp"
#include <fstream>
#include <sstream>
#include <iostream>
#include <vector>
#include <algorithm>

void Day1::first_part(std::string input_file)
{
        std::ifstream file(input_file);

        if (!file.is_open()) {
                throw std::runtime_error("Could not open file");
        }
        std::ostringstream ss;
        ss << file.rdbuf();
        std::string content = ss.str();
        file.close();

        std::cout << "Input file content:\n" << content << std::endl;

        std::istringstream iss(content);
        std::vector<std::string> lines;
        std::string line;

        int max_calories = 0;
        int current_calories = 0;

        while (std::getline(iss, line)) {
                if (line.empty()) {
                        if (current_calories > max_calories) {
                                max_calories = current_calories;
                        }
                        current_calories = 0;
                } else {
                        current_calories += std::stoi(line);
                }
        }

        std::cout << "Max calories: " << max_calories << std::endl;
}
void Day1::second_part(std::string input_file)
{
        std::ifstream file(input_file);

        if (!file.is_open()) {
                throw std::runtime_error("Could not open file");
        }
        std::ostringstream ss;
        ss << file.rdbuf();
        std::string content = ss.str();
        file.close();

        std::cout << "Input file content:\n" << content << std::endl;

        std::istringstream iss(content);
        std::vector<std::string> lines;
        std::string line;

        int current_calories = 0;
        std::vector<int> all_calorie_counts;

        while (std::getline(iss, line)) {
                if (line.empty()) {
                        all_calorie_counts.push_back(current_calories);
                        std::cout << "Recorded elf with " << current_calories
                                  << " calories." << std::endl;
                        current_calories = 0;
                } else {
                        current_calories += std::stoi(line);
                }
        }
        all_calorie_counts.push_back(current_calories);
        std::cout << "Recorded elf with " << current_calories << " calories."
                  << std::endl;

        std::sort(all_calorie_counts.begin(), all_calorie_counts.end(),
                  std::greater<int>());

        int top_three_sum = all_calorie_counts[0] + all_calorie_counts[1] +
                            all_calorie_counts[2];

        std::cout << "Sum calories top three elves: " << top_three_sum
                  << std::endl;
}
