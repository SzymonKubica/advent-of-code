#include "day_2.hpp"
#include <iostream>
#include <ostream>
#include <sstream>
#include <vector>
#include <string>
#include "../utils.hpp"

struct Range {
        int start;
        int end;

      public:
};

std::ostream &operator<<(std::ostream &os, const Range &r)
{
        return os << "(" << r.start << ", " << r.end << ")";
}

Range range_from_str(std::string range_str)
{
        int hyphen_index = range_str.find('-', 0);
        int start = atoi(range_str.substr(0, hyphen_index).c_str());
        int end = atoi(range_str.substr(hyphen_index + 1).c_str());
        return {start, end};
}

void Year2025Day2::first_part(std::string input_file)
{
        std::vector<std::string> lines = get_lines_from_file(input_file);
        // The input is a single line in this puzzle
        std::string input = lines[0];

        std::vector<Range> ranges;
        std::string range_str;
        std::istringstream ss(input);
        while (std::getline(ss, range_str, ',')) {
                ranges.emplace_back(range_from_str(range_str));
        }

        for (auto &range : ranges) {
                std::cout << range << std::endl;
        }
}
void Year2025Day2::second_part(std::string input_file)
{
        std::vector<std::string> lines = get_lines_from_file(input_file);
}
