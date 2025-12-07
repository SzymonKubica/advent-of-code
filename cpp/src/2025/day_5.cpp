#include "day_5.hpp"
#include <string>
#include <vector>
#include <ostream>
#include <algorithm>
#include <cstdint>
#include "../utils.hpp"

struct FreshnessRange {
        /**
         * Start of the range of fresh IDs (inclusive).
         */
        uint64_t start;
        /**
         * End of the range of fresh IDs (inclusive).
         */
        uint64_t end;

      public:
        uint64_t size()
        {
                // +1 as both start and end are inclusive.
                return end - start + 1;
        }
        static FreshnessRange from_string(std::string representation)
        {
                int hypen_idx = representation.find('-');
                return {std::stoull(representation.substr(0, hypen_idx)),
                        std::stoull(representation.substr(
                            hypen_idx + 1, representation.size()))};
        }
};

std::ostream &operator<<(std::ostream &os, const FreshnessRange &range)
{
        return os << "[" << range.start << ", " << range.end << "]";
}

enum class RangeEndpointType { Start, End };

struct FreshnessRangeEndpoint {
        RangeEndpointType type;
        uint64_t id_position;

      public:
        bool operator<(const FreshnessRangeEndpoint &other)
        {
          // If we have a range of length 1 where the start is equal to the
          // end we need to ensure that we first open and then close, otherwise
          // this can break the endpoints balance.
                if (id_position == other.id_position) {
                        return type == RangeEndpointType::Start &&
                               other.type == RangeEndpointType::End;
                }
                return id_position < other.id_position;
        }
};

std::ostream &operator<<(std::ostream &os,
                         const FreshnessRangeEndpoint &endpoint)
{
        switch (endpoint.type) {
        case RangeEndpointType::Start:
                return os << "(" << endpoint.id_position;
        case RangeEndpointType::End:
                return os << endpoint.id_position << ")";
        default:
                throw std::invalid_argument(
                    "Freshness range endpoint has an unrecognized type.");
        }
}

std::pair<std::vector<FreshnessRange>, std::vector<uint64_t>>
read_freshness_ranges_and_ingredient_ids(std::string input_file)
{
        std::vector<std::string> lines = read_lines_from_file(input_file);

        std::vector<FreshnessRange> ranges;
        std::vector<uint64_t> ingredient_ids;

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
                ingredient_ids.push_back(std::stoull(current_line));
                curr++;
        }

        return {ranges, ingredient_ids};
}

void Year2025Day5::first_part(std::string input_file)
{
        auto [ranges, ingredient_ids] =
            read_freshness_ranges_and_ingredient_ids(input_file);

        int fresh_ingredients = 0;
        for (uint64_t id : ingredient_ids) {
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

void Year2025Day5::second_part(std::string input_file)
{
        // The idea here is to get all range endpoints, preserve the information
        // whether they are ending or starting the range (irrespective of which
        // range) then iterate over the endpoints and do somethign like
        // 'paranthesis-balancing' check where we keep track of the region where
        // there is at least one range that is open.
        auto [ranges, ingredient_ids] =
            read_freshness_ranges_and_ingredient_ids(input_file);

        std::vector<FreshnessRangeEndpoint> endpoints;

        for (auto &range : ranges) {
                endpoints.emplace_back(RangeEndpointType::Start, range.start);
                endpoints.emplace_back(RangeEndpointType::End, range.end);
        }

        std::sort(endpoints.begin(), endpoints.end());

        std::vector<FreshnessRange> merged_ranges;
        uint64_t last_open_id;
        int ranges_open = 0;
        for (auto &endpoint : endpoints) {
                switch (endpoint.type) {
                case RangeEndpointType::Start:
                        if (ranges_open == 0) {
                                last_open_id = endpoint.id_position;
                        }
                        ranges_open++;
                        break;
                case RangeEndpointType::End:
                        ranges_open--;
                        if (ranges_open == 0) {
                                merged_ranges.emplace_back(
                                    last_open_id, endpoint.id_position);
                        }
                        break;
                default:
                        throw std::invalid_argument(
                            "Freshness range endpoint has an unrecognized "
                            "type.");
                }
                // Add indentation based on how many ranges are open.
                // Note that if we added an open range we want to decrease the
                // number of visible open ranges so that the output is
                // 'balanced' i.e. the indent increases after an new range is
                // opened but it decreases BEFORE the range is closed.
                int indent = endpoint.type == RangeEndpointType::Start
                                 ? ranges_open - 1
                                 : ranges_open;
                for (int i = 0; i < indent; i++) {
                        std::cout << "    ";
                }
                std::cout << endpoint << std::endl;
        }

        uint64_t total_fresh_ids = 0;
        for (auto &range : merged_ranges) {
                std::cout << range << std::endl;
                total_fresh_ids += range.size();
        }

        std::cout << "Total available fresh ingredient IDs: " << total_fresh_ids
                  << std::endl;
}
