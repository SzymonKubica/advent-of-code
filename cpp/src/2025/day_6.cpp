#include "day_6.hpp"
#include <string>
#include <vector>
#include <sstream>
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

void Year2025Day6::first_part(std::string input_file)
{
        auto problems = read_problems_from_file(input_file);
        uint64_t grand_total = 0;
        for (auto &problem : problems) {
                grand_total += problem.solve();
        }
        std::cout << "Grand total: " << grand_total << std::endl;
}

void Year2025Day6::second_part(std::string input_file) {}
