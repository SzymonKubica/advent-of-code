#include "day_3.hpp"
#include <string>
#include <iostream>
#include <vector>
#include <cmath>
#include "../utils.hpp"
#include <boost/multiprecision/cpp_int.hpp>

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

int find_max_joltage(const std::vector<int> &battery_bank)
{
        // We first find the largest digit in the sublist of the bank that
        // excludes the last character. The idea being that if the second to
        // last char is '9' and it is the max, then no matter what is on the
        // last place we want to take '9' as the first digit of our 2 digit
        // number.

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
                second_largest_digit =
                    std::max(second_largest_digit, battery_bank[i]);
        }

        return max_prefix_digit * 10 + second_largest_digit;
}

long long find_max_joltage(const std::vector<int> &battery_bank,
                           int num_chosen_batteries)
{
        int chosen_batteries[num_chosen_batteries];

        for (int i = 0; i < num_chosen_batteries; i++) {
                chosen_batteries[i] = 0;
        }

        int last_chosen_index = 0;
        int max_prefix_digit = battery_bank[0];
        for (int i = 0; i < num_chosen_batteries; i++) {
                for (int j = last_chosen_index + 1;
                     j < battery_bank.size() - (num_chosen_batteries - 1) + i;
                     j++) {
                        int current = battery_bank[j];
                        // Only update if actually larger
                        if (current > max_prefix_digit) {
                                max_prefix_digit = current;
                                last_chosen_index = j;
                        }
                }
                chosen_batteries[i] = max_prefix_digit;
                last_chosen_index++;
                if (last_chosen_index < battery_bank.size()) {
                        max_prefix_digit = battery_bank[last_chosen_index];
                }
        }

        long total = 0;

        for (int i = 0; i < num_chosen_batteries; i++) {
                std::cout << "Chosen battery: " << chosen_batteries[i]
                          << std::endl;
                total += ((long)boost::multiprecision::pow(
                             (boost::multiprecision::cpp_int)10,
                             (long)num_chosen_batteries - (long)i - 1)) *
                         chosen_batteries[i];
        }
        return total;
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

void Year2025Day3::second_part(std::string input_file)
{
        auto battery_banks = read_battery_banks(input_file);

        std::cout << "sizeof(long) = " << sizeof(long) << "\n";
        long long total_joltage;
        for (auto &bank : battery_banks) {
                long long max_joltage = find_max_joltage(bank, 12);
                std::cout << "Max bank joltage: " << max_joltage << std::endl;
                total_joltage += (long long) max_joltage;
        }
        std::cout << "Total joltage: " << total_joltage << std::endl;

        long long a = 987654321111LL;
        long long b = 811111111119LL;
        long long c = 434234234278LL;
        long long d = 888911112111LL;

        long long sum = a + b + c + d;
        std::cout << sum << "\n";
}
