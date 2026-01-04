#include "day_12.hpp"
#include <string>
#include <vector>
#include <array>
#include <cassert>
#include <istream>
#include <sstream>
#include "../utils.hpp"

enum class PresentPart { EmptySpace, Present };

struct Present {
        std::array<std::array<PresentPart, 3>, 3> shape;

      public:
        static Present from_str(std::string lines)
        {
                std::cout << "Parsing present shape from lines: " << std::endl
                          << lines << std::endl;
                std::istringstream ss(lines);

                std::array<std::array<PresentPart, 3>, 3> output = {
                    {{{PresentPart::EmptySpace, PresentPart::EmptySpace,
                       PresentPart::EmptySpace}},
                     {{PresentPart::EmptySpace, PresentPart::EmptySpace,
                       PresentPart::EmptySpace}},
                     {{PresentPart::EmptySpace, PresentPart::EmptySpace,
                       PresentPart::EmptySpace}}}};

                std::string line;
                int row = 0;
                while (std::getline(ss, line)) {
                        int column = 0;
                        for (char c : line) {
                                output[row][column] =
                                    c == '#' ? PresentPart::Present
                                             : PresentPart::EmptySpace;
                                column++;
                        }
                        row++;
                }
                return {output};
        }

        int get_area() const
        {
                int area = 0;

                for (auto &row : this->shape) {
                        for (auto &cell : row) {
                                if (cell == PresentPart::Present) {
                                        area++;
                                }
                        }
                }
                return area;
        }
};

struct Region {
        int width;
        int height;
        std::vector<int> present_counts;

      public:
        static Region from_str(std::string line)
        {
                std::string dimensions;
                std::string counts;
                {
                        std::istringstream ss(line);
                        assert(std::getline(ss, dimensions, ':'));
                        assert(std::getline(ss, counts, ':'));
                }

                int width;
                int height;
                {
                        std::string width_str;
                        std::string height_str;
                        std::istringstream ss(dimensions);
                        assert(std::getline(ss, width_str, 'x'));
                        assert(std::getline(ss, height_str, 'x'));
                        width = stoi(width_str);
                        height = stoi(height_str);
                }

                std::vector<int> puzzle_counts;

                {
                        // remove the leading space.
                        std::istringstream ss(counts.substr(1));
                        std::string count;
                        while (std::getline(ss, count, ' ')) {
                                puzzle_counts.push_back(stoi(count));
                        }
                }
                return {width, height, puzzle_counts};
        }
};

std::ostream &operator<<(std::ostream &os, const Region &region)
{

        os << "Region[ " << region.width << "x" << region.height << ": ";

        for (int count : region.present_counts) {
                os << count << " ";
        }
        os << "]";
        return os;
}

std::ostream &operator<<(std::ostream &os, const PresentPart &present_part)
{
        switch (present_part) {
        case PresentPart::EmptySpace:
                os << ".";
                break;
        case PresentPart::Present:
                os << "#";
                break;
        };
        return os;
}

std::ostream &operator<<(std::ostream &os, const Present &present)
{
        for (auto &row : present.shape) {
                for (auto &cell : row) {
                        os << cell;
                }
                os << std::endl;
        }
        return os;
}

bool region_can_fit_presents(const Region &region,
                             const std::vector<Present> &presents)
{
        std::cout << "Testing region: " << region << std::endl;
        // First: definitely impossible case - the total area of the region is
        // smaller than the total area of the presents to fit (assuming
        // perfectly efficient packing)
        int region_area = region.width * region.height;
        int presents_area = 0;

        for (int i = 0; i < region.present_counts.size(); i++) {
                int count = region.present_counts[i];
                int present_area = presents[i].get_area();
                presents_area += count * present_area;
        }

        std::cout << "Checking the definitely impossible case." << std::endl;
        std::cout << "Region area: " << region_area
                  << " total presents area: " << presents_area << std::endl;
        if (region_area < presents_area) {
                std::cout << "The region configuration is definitely impossible"
                          << std::endl;
                return false;
        }

        // Second: definitely possible case - assume each present is a 3x3
        // square and check if we can fit this many squares.

        int total_presents_count = 0;

        for (int i = 0; i < region.present_counts.size(); i++) {
                int count = region.present_counts[i];
                total_presents_count += count;
        }

        int capacity_for_3x3_squares = (region.width / 3) * (region.height / 3);

        std::cout << "Checking the definitely possible case." << std::endl;
        std::cout << "Total presents to fit: " << total_presents_count
                  << " capacity for 3x3 squares: " << capacity_for_3x3_squares
                  << std::endl;
        if (total_presents_count <= capacity_for_3x3_squares) {
                std::cout << "The region configuration is definitely possible"
                          << std::endl;
                return true;
        }

        // this should not be reachable as it is very difficult to solve the
        throw new std::invalid_argument(
            "The region and presents configuration is neither definitely "
            "impossible nor definitely possible and we don't support "
            "determination of such cases.");
}

void Year2025Day12::first_part(std::string input_file)
{
        auto contents = read_file_as_string(input_file);

        // We read the input file parts one-by-one. The parts are delimited with
        // double
        // \n
        std::vector<std::string> input_blocks;
        std::string delimiter = "\n\n";
        size_t position;
        while ((position = contents.find(delimiter)) != std::string::npos) {
                input_blocks.push_back(contents.substr(0, position));
                contents.erase(0, position + delimiter.length());
        }
        input_blocks.push_back(contents);

        std::vector<Present> presents;

        // All 'blocks' except for the last one contain the present shape
        // configurations so we parse them here.
        for (int i = 0; i < input_blocks.size() - 1; i++) {
                std::istringstream iss(input_blocks[i]);
                std::string index;
                std::string shape;
                std::getline(iss, index, ':');
                std::getline(iss, shape, ':');

                // We take the substring from the second character to trim the
                // leading '/n' that is left over after the ':'
                presents.push_back(Present::from_str(shape.substr(1)));
        }

        std::cout << "Parsed present shapes: " << std::endl;
        for (auto &present : presents) {
                std::cout << present << std::endl;
        }

        std::vector<Region> regions;
        std::istringstream ss(input_blocks[input_blocks.size() - 1]);
        std::string region_config_line;

        while (std::getline(ss, region_config_line)) {
                std::cout << "Parsing region configuration from: "
                          << region_config_line << std::endl;
                Region region = Region::from_str(region_config_line);
                std::cout << "Successfully parsed: " << region << std::endl;
                regions.push_back(region);
        }

        int possible_regions = 0;
        for (const auto &region : regions) {
                if (region_can_fit_presents(region, presents)) {
                        possible_regions++;
                }
        }

        std::cout << "A total of " << possible_regions
                  << " can fit their presents configurations." << std::endl;
}

void Year2025Day12::second_part(std::string input_file) {}
