#include "day_5.hpp"
#include <string>
#include <vector>
#include <ostream>
#include "../utils.hpp"

struct FreshnessRange {
        /**
         * Start of the range of fresh IDs (inclusive).
         */
        long start;
        /**
         * End of the range of fresh IDs (inclusive).
         */
        long end;

      public:
        static FreshnessRange from_string(std::string representation)
        {
                int hypen_idx = representation.find('-');
                return {std::stol(representation.substr(0, hypen_idx)),
                        std::stol(representation.substr(
                            hypen_idx + 1, representation.size()))};
        }
};

std::ostream &operator<<(std::ostream &os, const FreshnessRange &range)
{
        return os << "[" << range.start << ", " << range.end << "]";
}

std::pair<std::vector<FreshnessRange>, std::vector<long>>
read_freshness_ranges_and_ingredient_ids(std::string input_file)
{
        std::vector<std::string> lines = read_lines_from_file(input_file);

        std::vector<FreshnessRange> ranges;
        std::vector<long> ingredient_ids;

        int curr = 0;
        while (true) {
                std::string current_line = lines[curr];
                if (current_line.empty()) {
                        curr++;
                        break;
                }

                ranges.push_back(FreshnessRange::from_string(current_line));
                curr++;
        }

        while (curr < lines.size()) {
                std::string current_line = lines[curr];
                ingredient_ids.push_back(std::stol(current_line));
                curr++;
        }

        return {ranges, ingredient_ids};
}

void Year2025Day5::first_part(std::string input_file)
{
        auto [ranges, ingredient_ids] =
            read_freshness_ranges_and_ingredient_ids(input_file);

        int fresh_ingredients = 0;
        for (long id : ingredient_ids) {
                for (auto &range : ranges) {
                        std::cout << "Checking range: " << range << std::endl;
                        if (range.start <= id && id <= range.end) {
                                fresh_ingredients++;
                                break;
                        }
                }
        }

        std::cout << "Number of fresh ingredients: " << fresh_ingredients
                  << std::endl;
}

void Year2025Day5::second_part(std::string input_file) {
  // sort the ranges by their start id
  // iterate over sorted list
  // if two ranges overlap replace them with one
  // continue until stability reached
  // add up 'lengths' of all remaining, disjoint arrays
}
