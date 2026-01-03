#include "day_12.hpp"
#include <string>
#include <vector>
#include <array>
#include <istream>
#include <sstream>
#include "../utils.hpp"

enum class PresentPart { EmptySpace, Present };

struct Present {
        std::array<std::array<PresentPart, 3>, 3> shape;

      public:
        static Present from_str(std::string lines)
        {
                std::istringstream ss(lines);

                std::array<std::array<PresentPart, 3>, 3> output = {
                    {{PresentPart::EmptySpace, PresentPart::EmptySpace,
                      PresentPart::EmptySpace},
                     {PresentPart::EmptySpace, PresentPart::EmptySpace,
                      PresentPart::EmptySpace}}};

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
};

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
        for (int i = 0; i < input_blocks.size(); i++) {
                std::istringstream iss(input_blocks[i]);
                std::string index;
                std::string shape;
                std::getline(iss, index, ':');
                std::getline(iss, shape, ':');

                presents.push_back(Present::from_str(shape));
        }

        for (auto &present : presents) {
                std::cout << present << std::endl;
        }
}

void Year2025Day12::second_part(std::string input_file) {}
