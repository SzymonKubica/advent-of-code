#include "day_3.hpp"
#include <string>
#include <iostream>
#include <vector>
#include "../utils.hpp"

std::vector<std::vector<int>> read_battery_banks(std::string input_file)
{
        std::vector<std::string> lines = get_lines_from_file(input_file);

        std::vector<std::vector<int>> battery_banks;
        for (auto &line : lines) {
                std::vector<int> battery_bank;
                for (int i = 0; i < line.size(); i++) {
                        battery_bank.push_back(line.at(i) - '0');
                }
                battery_banks.push_back(battery_bank);
        }
        return battery_banks;
}

int find_max_joltage(const std::vector<int> &battery_bank) {
  // We first find the largest digit in the sublist of the bank that excludes
  // the last character. The idea being that if the second to last char is '9'
  // and it is the max, then no matter what is on the last place we want to take
  // '9' as the first digit of our 2 digit number.

  int max_prefix_digit = battery_bank[0];
  int max_prefix_index = 0;
  for (int i = 1; i < battery_bank.size() - 1; i++) {
    int current = battery_bank[i];
    // Only update if actually larger
    if (current > max_prefix_digit) {
      max_prefix_digit = current;
      max_prefix_index = i;
    }
  }

  // We  find the second largest character
  int second_largest_digit = battery_bank[max_prefix_index + 1];
  for (int i = max_prefix_index + 2; i < battery_bank.size(); i++) {
    second_largest_digit = std::max(second_largest_digit, battery_bank[i]);
  }

  return max_prefix_digit * 10 + second_largest_digit;
}

void Year2025Day3::first_part(std::string input_file)
{
        auto battery_banks = read_battery_banks(input_file);

        int total_joltage;
        for (auto &bank : battery_banks) {
                total_joltage += find_max_joltage(bank);
        }
        std::cout << "Total joltage: " << total_joltage << std::endl;
}

void Year2025Day3::second_part(std::string input_file) {
        auto battery_banks = read_battery_banks(input_file);

        int total_joltage;
        for (auto &bank : battery_banks) {
                total_joltage += find_max_joltage(bank);
        }
        std::cout << "Total joltage: " << total_joltage << std::endl;
}
