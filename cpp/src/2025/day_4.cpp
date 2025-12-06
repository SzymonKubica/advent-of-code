#include "day_4.hpp"
#include <string>
#include <vector>
#include <cstdint>
#include <optional>
#include "../utils.hpp"

enum class GridCell { Empty = 0, PaperRoll = 1, ReachablePaperRoll = 2 };

std::ostream &operator<<(std::ostream &os, const GridCell &r)
{
        switch (r) {
        case GridCell::Empty:
                return os << '.';
        case GridCell::PaperRoll:
                return os << '@';
        case GridCell::ReachablePaperRoll:
                return os << 'x';
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
        auto grid_copy = read_grid_from_file(input_file);
        std::cout << grid << std::endl;

        int accessible_paper_rolls = 0;
        for (const auto &row : grid.get_grid_of_points()) {
                for (const auto &cell : row) {
                        if (grid.index_grid(cell.location) == GridCell::Empty) {
                                continue;
                        }
                        std::vector<GridPoint<GridCell>> neighbours =
                            grid.get_neighbours(cell);

                        int paper_rolls_surrounding = 0;
                        for (auto &nb : neighbours) {
                                if (nb.value == GridCell::PaperRoll) {
                                        paper_rolls_surrounding++;
                                }
                        }
                        if (paper_rolls_surrounding < 4) {
                                accessible_paper_rolls++;
                                grid_copy
                                    .cells[cell.location.y][cell.location.x] =
                                    GridCell::ReachablePaperRoll;
                        }
                }
        }

        std::cout << "The forkflift can access " << accessible_paper_rolls
                  << " paper rolls." << std::endl;
        std::cout << grid_copy << std::endl;
}

void Year2025Day4::second_part(std::string input_file)
{
        auto grid = read_grid_from_file(input_file);
        std::cout << grid << std::endl;

        int total_removed = 0;
        int iteration = 0;
        while (true) {
                int accessible_paper_rolls = 0;
                for (const auto &row : grid.get_grid_of_points()) {
                        for (const auto &cell : row) {
                                auto curr_value =
                                    grid.index_grid(cell.location);
                                if (curr_value == GridCell::Empty ||
                                    curr_value ==
                                        GridCell::ReachablePaperRoll) {
                                        continue;
                                }
                                std::vector<GridPoint<GridCell>> neighbours =
                                    grid.get_neighbours(cell);

                                int paper_rolls_surrounding = 0;
                                for (auto &nb : neighbours) {
                                        if (nb.value == GridCell::PaperRoll) {
                                                paper_rolls_surrounding++;
                                        }
                                }
                                if (paper_rolls_surrounding < 4) {
                                        accessible_paper_rolls++;
                                        grid.cells[cell.location.y]
                                                  [cell.location.x] =
                                            GridCell::ReachablePaperRoll;
                                }
                        }
                }
                if (accessible_paper_rolls > 0) {
                        total_removed += accessible_paper_rolls;
                        std::cout << "Removed " << accessible_paper_rolls
                                  << " on iteration " << iteration << "."
                                  << std::endl;
                        std::cout << grid << std::endl;
                } else {
                        break;
                }
                iteration++;
        }

        std::cout << "The forlift can remove a total of " << total_removed
                  << " paper rolls." << std::endl;
}
