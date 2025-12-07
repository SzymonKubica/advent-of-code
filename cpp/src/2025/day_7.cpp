#include "day_7.hpp"
#include <cstdint>
#include <string>
#include <vector>
#include <map>
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

void Year2025Day7::second_part(std::string input_file)
{
        auto grid = read_manifold_from_file(input_file);

        std::cout << grid << std::endl;

        auto grid_with_coordinates = grid.get_grid_of_points();

        // We need to keep track of how many times a given location is entered
        // by the ray to correctly calculate the total possible paths. This is
        // because if there are n ways of getting into a splitter, then the
        // number alternative paths that go out of the splitter is 2*n.
        std::map<Point, uint64_t> incoming_rays;

        //  For the first row we replace the starting point with a ray and start
        //  processing from the second row
        for (auto &cell : grid_with_coordinates[0]) {
                if (cell.value == TachyonGridCell::Start) {
                        grid.set(cell.location, TachyonGridCell::Ray);
                        incoming_rays[cell.location] = 1;
                }
        }

        int total_splits = 0;
        uint64_t possible_paths = 1;
        for (auto row_it = std::next(grid_with_coordinates.begin());
             row_it != grid_with_coordinates.end(); ++row_it) {
                for (auto &cell : *row_it) {
                        auto &loc = cell.location;
                        auto location_above = loc.translate(Direction::Up);
                        auto cell_above = grid.index(location_above).value();

                        uint64_t rays_incident_above =
                            incoming_rays[location_above];

                        // We only need to do interesting work if there is a ray
                        // incident from the above onto our current cell.
                        if (cell_above != TachyonGridCell::Ray) {
                                continue;
                        }
                        if (cell.value == TachyonGridCell::Splitter) {
                                total_splits++;
                                auto left = loc.translate(Direction::Left);
                                auto right = loc.translate(Direction::Right);
                                grid.set(left, TachyonGridCell::Ray);
                                grid.set(right, TachyonGridCell::Ray);

                                incoming_rays[left] += rays_incident_above;
                                incoming_rays[right] += rays_incident_above;
                                // Only a splitter increases the number of
                                // possible paths.
                                possible_paths += rays_incident_above;

                        } else if (cell.value == TachyonGridCell::Empty) {
                                grid.set(cell.location, TachyonGridCell::Ray);
                                incoming_rays[loc] += rays_incident_above;
                        }
                }
        }
        std::cout << grid << std::endl;
        std::cout << std::endl;
        std::cout << "There are a total of " << possible_paths
                  << " possible paths." << std::endl;
}
