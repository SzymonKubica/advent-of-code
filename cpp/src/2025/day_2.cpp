#include "day_2.hpp"
#include <iostream>
#include <ostream>
#include <sstream>
#include <vector>
#include <string>
#include "../utils.hpp"

struct Range {
        long start;
        long end;

      public:
};

std::ostream &operator<<(std::ostream &os, const Range &r)
{
        return os << "(" << r.start << ", " << r.end << ")";
}

Range range_from_str(std::string range_str)
{
        int hyphen_index = range_str.find('-', 0);
        long start = atol(range_str.substr(0, hyphen_index).c_str());
        long end = atol(range_str.substr(hyphen_index + 1).c_str());
        return {start, end};
}

std::vector<Range> read_ranges(std::string input_file)
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
        return ranges;
}

void log_invalid_id_count_and_sum(long count, long sum)
{
        std::cout << "Found " << count << " invalid IDs." << std::endl;
        std::cout << "Sum of the invalid IDs: " << sum << "." << std::endl;
}

void Year2025Day2::first_part(std::string input_file)
{
        auto ranges = read_ranges(input_file);

        int invalid_id_count = 0;
        long invalid_id_sum = 0;
        for (auto &range : ranges) {
                std::cout << range << std::endl;
                for (long i = range.start; i <= range.end; i++) {
                        std::string i_str = std::to_string(i);
                        if (i_str.size() % 2 != 0) {
                                continue;
                        }
                        std::string first_half =
                            i_str.substr(0, i_str.size() / 2);
                        std::string second_half =
                            i_str.substr(i_str.size() / 2, i_str.size() / 2);

                        if (first_half == second_half) {
                                invalid_id_count++;
                                invalid_id_sum += i;
                        }
                }
        }
        log_invalid_id_count_and_sum(invalid_id_count, invalid_id_sum);
}

void Year2025Day2::second_part(std::string input_file)
{
        auto ranges = read_ranges(input_file);

        int invalid_id_count = 0;
        long invalid_id_sum = 0;
        for (auto &range : ranges) {
                std::cout << "Processing: " << range << std::endl;
                for (long i = range.start; i <= range.end; i++) {
                        std::string i_str = std::to_string(i);
                        int length = i_str.size();
                        int midpoint = length / 2;
                        // We only need to process increasing length substrings
                        // of the first half. The reason for this is that
                        // anything longer than the first half cannot be
                        // repeated.
                        for (long j = 1; j <= midpoint; j++) {
                                std::string prefix = i_str.substr(0, j);
                                int compared_substr_idx = j;
                                bool all_matching = true;
                                while (compared_substr_idx < length) {
                                        std::string compared = i_str.substr(
                                            compared_substr_idx, j);

                                        if (prefix != compared) {
                                                all_matching = false;
                                                break;
                                        }

                                        compared_substr_idx += j;
                                }

                                if (all_matching) {
                                        invalid_id_count++;
                                        invalid_id_sum += i;
                                        // We break here because if a shorter
                                        // prefix is already matching e.g.
                                        // prefix "2" for string "2222", the
                                        // prefix of "22" would also be matching
                                        // and result in the same string.
                                        break;
                                }
                        }
                }
        }
        log_invalid_id_count_and_sum(invalid_id_count, invalid_id_sum);
}
