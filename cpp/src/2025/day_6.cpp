#include "day_6.hpp"
#include <string>
#include <vector>
#include <sstream>
#include <cmath>
#include <iostream>
#include "../utils.hpp"

enum class Operation { Addition, Multiplication };

Operation from_char(char c)
{
        switch (c) {
        case '*':
                return Operation::Multiplication;
        case '+':
                return Operation::Addition;
        default:
                throw std::invalid_argument("Unrecognized operator");
        }
}

char operation_to_char(Operation op)
{
        switch (op) {
        case Operation::Addition:
                return '+';
        case Operation::Multiplication:
                return '*';
        default:
                throw std::invalid_argument("Unrecognized operator");
        }
}

struct Problem {
        Operation operation;
        std::vector<uint64_t> numbers;

      public:
        uint64_t solve()
        {
                switch (operation) {
                case Operation::Addition: {
                        uint64_t total = 0;
                        for (uint64_t x : numbers) {
                                total += x;
                        }
                        return total;
                } break;
                case Operation::Multiplication: {
                        uint64_t total = 1;
                        for (uint64_t x : numbers) {
                                total *= x;
                        }
                        return total;
                } break;
                default:
                        throw std::invalid_argument("Unrecognized operator");
                }
        }
};

std::ostream &operator<<(std::ostream &os, const Problem &problem)
{
        for (int i = 0; i < problem.numbers.size(); i++) {
                os << problem.numbers[i];
                if (i < problem.numbers.size() - 1) {
                        os << " " << operation_to_char(problem.operation)
                           << " ";
                }
        }
        return os;
}

std::vector<Problem> read_problems_from_file(std::string input_file)
{
        auto lines = read_lines_from_file(input_file);

        std::vector<std::vector<uint64_t>> numbers;
        std::vector<Operation> operations;

        // The last line contains the operations
        for (int i = 0; i < lines.size() - 1; i++) {
                std::vector<uint64_t> row;
                std::istringstream iss(lines[i]);

                uint64_t x;
                while (iss >> x) {
                        row.push_back(x);
                }
                numbers.push_back(row);
        }

        std::istringstream iss(lines[lines.size() - 1]);

        char c;
        while (iss >> c) {
                operations.push_back(from_char(c));
        }

        std::vector<Problem> problems;
        for (int i = 0; i < numbers[0].size(); i++) {
                std::vector<uint64_t> column;
                for (int j = 0; j < numbers.size(); j++) {
                        column.push_back(numbers[j][i]);
                }
                Operation operation = operations[i];

                problems.emplace_back(operation, column);
        }
        return problems;
}

std::vector<Problem>
read_problems_from_file_right_to_left_math_experimental(std::string input_file)
{
        auto lines = read_lines_from_file(input_file);
        // first get the operations
        std::vector<Operation> operations;
        std::istringstream iss(lines[lines.size() - 1]);
        char c;
        while (iss >> c) {
                operations.push_back(from_char(c));
        }

        int lines_with_numbers = lines.size() - 1;
        std::vector<std::vector<uint64_t>> problem_numbers;
        std::vector<uint64_t> numbers;
        for (int i = lines[0].size() - 1; i >= 0; i--) {
                char column_chars[lines_with_numbers + 1];
                // We need to null-terminate the buffer
                column_chars[lines_with_numbers] = '\0';
                for (int j = 0; j < lines_with_numbers; j++) {
                        column_chars[j] = lines[j][i];
                }
                std::string number(column_chars);
                if (number.find_first_not_of(" \t\n\r\f\v") ==
                    std::string::npos) {
                        // string is blank meaning that we need to start
                        // collecting the next problem
                        problem_numbers.push_back(numbers);
                        numbers.clear();
                        continue;
                }
                std::cout << number << std::endl;
                numbers.push_back(stoi(number));
        }
        // We also need to process the last problem (leftmost one)
        problem_numbers.push_back(numbers);
        numbers.clear();

        std::vector<Problem> problems;
        // We process problems from right to left hence we need to index
        // operations in reverse order so that they match properly.
        for (int i = 0; i < problem_numbers.size(); i++) {
                problems.emplace_back(operations[operations.size() - 1 - i],
                                      problem_numbers[i]);
        }

        return problems;
};

void Year2025Day6::first_part(std::string input_file)
{
        auto problems = read_problems_from_file(input_file);
        uint64_t grand_total = 0;
        for (auto &problem : problems) {
                grand_total += problem.solve();
        }
        std::cout << "Grand total: " << grand_total << std::endl;
}

void Year2025Day6::second_part(std::string input_file)
{
        auto problems =
            read_problems_from_file_right_to_left_math_experimental(input_file);
        uint64_t grand_total = 0;
        for (auto &problem : problems) {
                std::cout << problem << std::endl;
                grand_total += problem.solve();
        }
        std::cout << "Grand total: " << grand_total << std::endl;
}
