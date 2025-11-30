#include "day_1.hpp"
#include <iostream>
#include <vector>
#include <algorithm>
#include "../utils.hpp"

void Day1::first_part(std::string input_file)
{
        std::vector<std::string> lines = get_lines_from_file(input_file);

        int max_calories = 0;
        int current_calories = 0;

        for (auto &line : lines) {
                if (line.empty()) {
                        max_calories = std::max(current_calories, max_calories);
                        current_calories = 0;
                } else {
                        current_calories += std::stoi(line);
                }
        }

        std::cout << "Max calories: " << max_calories << std::endl;
}
void Day1::second_part(std::string input_file)
{
        std::vector<std::string> lines = get_lines_from_file(input_file);

        int current_calories = 0;
        std::vector<int> all_calorie_counts;

        for (auto &line : lines) {
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
