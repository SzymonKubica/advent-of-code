#include "day_4.hpp"
#include <string>
#include <vector>
#include <cstdint>
#include <optional>
#include "../utils.hpp"

enum class GridCell { Empty = 0, PaperRoll = 1 };

std::ostream &operator<<(std::ostream &os, const GridCell &r)
{
        switch (r) {
        case GridCell::Empty:
                return os << '.';
        case GridCell::PaperRoll:
                return os << '@';
        }
        return os;
}

std::optional<GridCell> from_char(char c)
{
        switch (c) {
        case '.':
                return GridCell::Empty;
        case '@':
                return GridCell::PaperRoll;
        default:
                return std::nullopt;
        }
}

Grid<GridCell> read_grid_from_file(std::string input_file)
{
        std::vector<std::string> lines = read_lines_from_file(input_file);
        std::vector<std::vector<GridCell>> cells;

        for (auto &line : lines) {
                std::vector<GridCell> row;
                for (int i = 0; i < line.size(); i++) {
                        row.push_back(from_char(line.at(i)).value());
                }
                cells.push_back(row);
        }
        return {.cells = cells};
}

void Year2025Day4::first_part(std::string input_file)
{
        auto grid = read_grid_from_file(input_file);
        std::cout << grid << std::endl;
}

void Year2025Day4::second_part(std::string input_file) {}
