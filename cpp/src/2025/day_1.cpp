#include "day_1.hpp"
#include <iostream>
#include <vector>
#include <algorithm>
#include "../utils.hpp"

enum class Direction { Left, Right };

struct Rotation {
        int distance;
        Direction direction;

      public:
        static Rotation from_string(std::string &str)
        {
                int distance = std::stoi(str.substr(1, str.size() - 1));
                Direction direction =
                    str[0] == 'L' ? Direction::Left : Direction::Right;

                return {distance, direction};
        }
};

void Year2025Day1::first_part(std::string input_file)
{
        std::vector<std::string> lines = get_lines_from_file(input_file);
        const int dial_positions = 100;

        int pointing_at_zero_count = 0;
        int current_position = 50;
        for (auto &line : lines) {
                auto rotation = Rotation::from_string(line);

                int sign;
                switch (rotation.direction) {
                case Direction::Left:
                        sign = -1;
                        break;
                case Direction::Right:
                        sign = 1;
                        break;
                }

                current_position =
                    (current_position + sign * rotation.distance) %
                    dial_positions;

                if (current_position == 0) {
                        pointing_at_zero_count++;
                }
        }
        std::cout << "Number of times the dial pointed at 0: "
                  << pointing_at_zero_count;
}
void Year2025Day1::second_part(std::string input_file)
{
        std::vector<std::string> lines = get_lines_from_file(input_file);
        const int dial_positions = 100;

        int pointing_at_zero_count = 0;
        int current_position = 50;
        for (auto &line : lines) {
                auto rotation = Rotation::from_string(line);

                int sign;
                switch (rotation.direction) {
                case Direction::Left:
                        sign = -1;
                        break;
                case Direction::Right:
                        sign = 1;
                        break;
                }

                // First handle full circles
                int full_circles = rotation.distance / dial_positions;
                pointing_at_zero_count += full_circles;

                int remainder = rotation.distance % dial_positions;
                int new_position = (current_position + sign * remainder);

                if (new_position >= dial_positions) {
                        // Case where we wrap around clockwise
                        pointing_at_zero_count++;
                }

                if (current_position != 0 && new_position <= 0) {
                        // Case where we wrap around anti-clockwise
                        pointing_at_zero_count++;
                }

                // Ensure we handle negative numbers here
                current_position = (dial_positions + new_position) % dial_positions;
        }
        std::cout << "Number of times the dial pointed at 0: "
                  << pointing_at_zero_count;
}
