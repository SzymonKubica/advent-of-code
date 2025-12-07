#include "day_7.hpp"
#include <string>
#include <vector>
#include "../utils.hpp"

/**
 * Tachyon is this sub-atomic particle that is cast in rays in the puzzle back
 * story.
 */
enum class TachyonGridCell { Start, Ray, Splitter, Empty };

namespace TachyonGridCellUtils
{
TachyonGridCell from_char(char c)
{
        switch (c) {
        case '.':
                return TachyonGridCell::Empty;
        case '|':
                return TachyonGridCell::Ray;
        case 'S':
                return TachyonGridCell::Start;
        case '^':
                return TachyonGridCell::Splitter;
        default:
                throw std::invalid_argument(
                    "Unrecognized grid cell representation.");
        }
}
} // namespace TachyonGridCellUtils

std::ostream &operator<<(std::ostream &os, const TachyonGridCell &cell)
{
        switch (cell) {
        case TachyonGridCell::Start:
                return os << 'S';
        case TachyonGridCell::Ray:
                return os << '|';
        case TachyonGridCell::Splitter:
                return os << '^';
        case TachyonGridCell::Empty:
                return os << '.';
        default:
                throw std::invalid_argument(
                    "Unrecognized grid cell representation.");
        }
}

Grid<TachyonGridCell> read_manifold_from_file(std::string input_file)
{
        std::vector<std::vector<TachyonGridCell>> grid;
        auto lines = read_lines_from_file(input_file);

        for (auto &line : lines) {
                std::vector<TachyonGridCell> row;
                for (int i = 0; i < line.size(); i++) {
                        row.push_back(
                            TachyonGridCellUtils::from_char(line.at(i)));
                }
                grid.push_back(row);
        }
        return {grid};
}

void Year2025Day7::first_part(std::string input_file)
{
        auto grid = read_manifold_from_file(input_file);

        std::cout << grid << std::endl;

        auto grid_with_coordinates = grid.get_grid_of_points();

        //  For the first row we replace the starting point with a ray and start
        //  processing from the second row
        for (auto &cell : grid_with_coordinates[0]) {
                if (cell.value == TachyonGridCell::Start) {
                        grid.set(cell.location, TachyonGridCell::Ray);
                }
        }

        int total_splits = 0;
        for (auto row_it = std::next(grid_with_coordinates.begin());
             row_it != grid_with_coordinates.end(); ++row_it) {
                for (auto &cell : *row_it) {
                        auto cell_above =
                            grid.index(cell.location.translate(Direction::Up))
                                .value();

                        if (cell_above == TachyonGridCell::Ray) {
                                if (cell.value == TachyonGridCell::Splitter) {
                                        total_splits++;
                                        grid.set(cell.location.translate(
                                                     Direction::Left),
                                                 TachyonGridCell::Ray);
                                        grid.set(cell.location.translate(
                                                     Direction::Right),
                                                 TachyonGridCell::Ray);
                                } else if (cell.value ==
                                           TachyonGridCell::Empty) {
                                        grid.set(cell.location,
                                                 TachyonGridCell::Ray);
                                }
                        }
                }
                std::cout << grid << std::endl;
                std::cout << std::endl;
        }

        std::cout << "The ray is split a total of " << total_splits << " times."
                  << std::endl;
}

void Year2025Day7::second_part(std::string input_file) {}
