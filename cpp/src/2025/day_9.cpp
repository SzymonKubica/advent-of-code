#include "day_9.hpp"
#include <cstdint>
#include <iomanip>
#include <string>
#include <vector>
#include <ostream>
#include <sstream>
#include <algorithm>
#include <string>
#include <map>
#include <cassert>

#include "../utils.hpp"

namespace Day9
{
std::vector<Point> read_points_from_file(std::string input_file);
};

enum class FloorTile {
        RedTile,
        GreenTile,
        BoundaryCorner,
        Empty,
};

std::ostream &operator<<(std::ostream &os, FloorTile tile)
{
        switch (tile) {
        case FloorTile::RedTile:
                os << "#";
                break;
        case FloorTile::GreenTile:
                os << "X";
                break;
        case FloorTile::BoundaryCorner:
                os << "B";
                break;
        case FloorTile::Empty:
                os << ".";
                break;
        }
        return os;
}

struct Rectangle {
        Point primary_corner;
        Point opposite_corner;

      public:
        Rectangle(Point first, Point second)
        {
                this->primary_corner = first;
                this->opposite_corner = second;
        }

        bool contains(const Point &point)
        {
                auto &higher_corner = primary_corner.y <= opposite_corner.y
                                          ? primary_corner
                                          : opposite_corner;
                auto &lower_corner = primary_corner.y > opposite_corner.y
                                         ? primary_corner
                                         : opposite_corner;
                auto &left_corner = primary_corner.x <= opposite_corner.x
                                        ? primary_corner
                                        : opposite_corner;
                auto &right_corner = primary_corner.x > opposite_corner.x
                                         ? primary_corner
                                         : opposite_corner;

                return left_corner.x <= point.x && point.x <= right_corner.x &&
                       higher_corner.y <= point.y && point.y <= lower_corner.y;
        }

        /**
         * Assuming that the segment is vertical / horizontal it tests whether
         * the rectangle intersects this segment.
         */
        bool intersects(const Segment &segment)
        {
                bool is_vertical = segment.first.x == segment.second.x;
                bool is_horizontal = segment.first.y == segment.second.y;

                int64_t min_x = std::min(primary_corner.x, opposite_corner.x);
                int64_t max_x = std::max(primary_corner.x, opposite_corner.x);
                int64_t min_y = std::min(primary_corner.y, opposite_corner.y);
                int64_t max_y = std::max(primary_corner.y, opposite_corner.y);

                assert(is_vertical || is_horizontal);

                if (is_vertical) {
                        bool line_intersects_rectangle =
                            min_x <= segment.first.x &&
                            segment.first.x <= max_x;
                        int64_t segment_min_y =
                            std::min(segment.first.y, segment.second.y);
                        int64_t segment_max_y =
                            std::max(segment.first.y, segment.second.y);
                        bool segment_overlaps_rectangle =
                            segment_min_y <= min_y && min_y <= segment_max_y ||
                            segment_min_y <= max_y && max_y <= segment_max_y;

                        return line_intersects_rectangle &&
                               segment_overlaps_rectangle;
                }
                if (is_horizontal) {
                        bool line_intersects_rectangle =
                            min_y <= segment.first.y &&
                            segment.first.y <= max_y;
                        int64_t segment_min_x =
                            std::min(segment.first.x, segment.second.x);
                        int64_t segment_max_x =
                            std::max(segment.first.x, segment.second.x);
                        bool segment_overlaps_rectangle =
                            segment_min_x <= min_x && min_x <= segment_max_x ||
                            segment_min_x <= max_x && max_x <= segment_max_x;
                        return line_intersects_rectangle &&
                               segment_overlaps_rectangle;
                }
                throw new std::invalid_argument(
                    "The tested segment has to be either horizontal or "
                    "vertical.");
        }

        std::vector<std::pair<Point, Point>> get_segments()
        {
                Point second_vertex = {.x = primary_corner.x,
                                       .y = opposite_corner.y};
                Point third_vertex = {.x = opposite_corner.x,
                                      .y = primary_corner.y};
                return {{primary_corner, second_vertex},
                        {second_vertex, opposite_corner},
                        {opposite_corner, third_vertex},
                        {third_vertex, primary_corner}};
        }
};

std::ostream &operator<<(std::ostream &os, const Rectangle &rectangle)
{
        os << "Rectangle defined by: " << std::endl;
        os << rectangle.primary_corner << std::endl;
        os << rectangle.opposite_corner << std::endl;
        return os;
}

std::vector<Point> Day9::read_points_from_file(std::string input_file)
{
        auto lines = read_lines_from_file(input_file);
        std::vector<Point> points;
        for (auto &line : lines) {
                std::vector<int> numbers;
                std::istringstream ss(line);
                std::string coordinate;

                while (std::getline(ss, coordinate, ',')) {
                        numbers.push_back(std::stoi(coordinate));
                }

                assert((numbers.size() == 2) &&
                       "Each point should have exactly 2 coordinates");
                points.push_back({.x = numbers[0], .y = numbers[1]});
        }
        return points;
}

int64_t calculate_area(const Point &top_left, const Point &bottom_right)
{
        auto absolute_diff = (top_left - bottom_right).modulus();
        return (absolute_diff.x + 1) * (absolute_diff.y + 1);
}

void Year2025Day9::first_part(std::string input_file)
{
        auto points = Day9::read_points_from_file(input_file);
        for (auto &p : points) {
                std::cout << p << std::endl;
        }

        std::vector<std::pair<const Point &, const Point &>> point_pairs;

        for (int i = 0; i + 1 < points.size(); i++) {
                for (int j = i + i; j < points.size(); j++) {
                        point_pairs.emplace_back(points[i], points[j]);
                }
        }

        int64_t max_area = 0;
        for (auto &pair : point_pairs) {
                max_area =
                    std::max(max_area, calculate_area(pair.first, pair.second));
        }

        std::cout << "Maximum area of a rectangle with its opposite corners "
                     "located on red tiles: "
                  << max_area << std::endl;
        std::cout << "This was found after processing " << point_pairs.size()
                  << " pairs." << std::endl;
}

/**
 * Returns the direction of the 'bend' that is defined by three points.
 * Note that the points need to be located on a rectangular grid and form a
 * right angle. Also note that the direction is relative to the direction of the
 * segment defined by the first two points, hence it always returns either Left
 * or Right.
 */
Direction determine_turn_direction(const Point &first, const Point &corner,
                                   const Point &second)
{

        // First edge is horizontal
        if (first.x == corner.x) {
                Direction current =
                    first.y > corner.y ? Direction::Up : Direction::Down;
                assert(corner.y == second.y);
                Direction turned =
                    corner.x < second.x ? Direction::Right : Direction::Left;

                // Depending on which way we are facing initially it affects the
                // actual direction of the turn.
                if (current == Direction::Up) {
                        return turned;
                } else {
                        return opposite_direction(turned);
                }
        } else if (first.y == corner.y) {
                Direction current =
                    first.x < corner.x ? Direction::Right : Direction::Left;
                Direction turned =
                    corner.y > second.y ? Direction::Up : Direction::Down;
                assert(corner.x == second.x);

                // Depending on which way we are facing initially it affects the
                // actual direction of the turn.
                if (current == Direction::Right) {
                        if (turned == Direction::Up) {
                                return Direction::Left;
                        } else {
                                return Direction::Right;
                        }
                } else if (current == Direction::Left) {
                        if (turned == Direction::Up) {
                                return Direction::Right;
                        } else {
                                return Direction::Left;
                        }
                }
        }
        throw new std::invalid_argument(
            "Invalid set of 3 points defining a corner.");
}

void Year2025Day9::second_part(std::string input_file)
{
        auto points = Day9::read_points_from_file(input_file);
        for (auto &p : points) {
                std::cout << p << std::endl;
        }

        std::vector<std::pair<const Point &, const Point &>> boundary_segments;
        for (int i = 0; i + 1 < points.size(); i++) {
                boundary_segments.emplace_back(points[i], points[i + 1]);
        }
        boundary_segments.emplace_back(points[points.size() - 1], points[0]);

        std::vector<std::pair<const Point &, const Point &>> point_pairs;

        for (int i = 0; i + 1 < points.size(); i++) {
                for (int j = i + 1; j < points.size(); j++) {
                        point_pairs.emplace_back(points[i], points[j]);
                }
        }

        // we copy the pairs to be able to sort in place
        std::vector<std::pair<std::pair<Point, Point>, int64_t>>
            pairs_with_areas;

        for (auto &pair : point_pairs) {
                pairs_with_areas.emplace_back(
                    pair, calculate_area(pair.first, pair.second));
        }

        auto greater_area_pair =
            [](std::pair<std::pair<const Point &, const Point &>, int64_t>
                   first,
               std::pair<std::pair<const Point &, const Point &>, int64_t>
                   second) { return first.second > second.second; };

        std::sort(pairs_with_areas.begin(), pairs_with_areas.end(),
                  greater_area_pair);

        std::cout << "Maximum area of a rectangle with its opposite corners "
                     "located on red tiles: "
                  << pairs_with_areas[0].second << std::endl;
        std::cout << "This was found after processing " << point_pairs.size()
                  << " pairs." << std::endl;

        std::vector<Rectangle *> rectangles;
        for (auto pair_with_area : pairs_with_areas) {
                rectangles.push_back(new Rectangle(
                    pair_with_area.first.first, pair_with_area.first.second));
        }

        // determine the loop direction
        int clockwise_turn_count = 0;
        for (int i = 0; i < points.size() - 2; i++) {
                Direction direction = determine_turn_direction(
                    points[i], points[i + 1], points[i + 2]);
                if (direction == Direction::Right) {
                        clockwise_turn_count++;
                } else {
                        clockwise_turn_count--;
                }
        }

        std::cout << "Total rotation: " << clockwise_turn_count << std::endl;
        bool clockwise_loop = clockwise_turn_count == 4;
        if (clockwise_loop) {
                std::cout << "Detected a clockwise point loop" << std::endl;
        } else {
                std::cout << "Detected an anti-clockwise point loop"
                          << std::endl;
        }

        // After we know whether we are going clockwise or anti-clockwise, we
        // know the direction towards the outside of the loop. This is needed to
        // translate the points of the boundary to determine the 'outside
        // boundary edges'. Those are then used to check if a given rectangle is
        // contained inside of the boundary by testing for intersections between
        // its edges and the edges of the 'outside boundary'.
        Direction towards_outside =
            clockwise_loop ? Direction::Left : Direction::Right;

        auto find_outer_boundary_vertex =
            [](const Point &first, const Point &vertex, const Point &second,
               Direction towards_outside) {
                    Direction current_direction =
                        segment_direction({first, vertex});
                    Direction turn_direction =
                        determine_turn_direction(first, vertex, second);

                    // To create the 'outside boundary' edges we need to
                    // translate the points towards the outside.
                    Direction towards_outside_translation =
                        absolute_direction(current_direction, towards_outside);

                    // We also need to factor in the direction of the turns. If
                    // the current corner is convex, we need to move one unit in
                    // the current direction to go over that vertex. If it is
                    // concave we need to move 'inwards' and hence we need to
                    // translate the point one unit opposite to the current
                    // direction of motion.
                    Direction turn_translation_direction =
                        turn_direction == towards_outside
                            ? opposite_direction(current_direction)
                            : current_direction;
                    Point translated_corner =
                        vertex.translate(turn_translation_direction)
                            .translate(towards_outside_translation);
                    return translated_corner;
            };

        std::vector<Point> outside_points;
        for (int i = 0; i < points.size() - 2; i++) {
                outside_points.push_back(find_outer_boundary_vertex(
                    points[i], points[i + 1], points[i + 2], towards_outside));
        }
        // We also need to process the last two vertex that were not processed
        // as the loop wraps around.
        outside_points.push_back(find_outer_boundary_vertex(
            points[points.size() - 2], points[points.size() - 1], points[0],
            towards_outside));
        outside_points.push_back(find_outer_boundary_vertex(
            points[points.size() - 1], points[0], points[1], towards_outside));

        // We create a grid for visualization (separated this in a sub-scope
        // as it does not warrant a separate function and I don't want to
        // clutter up the logic).
        Grid<FloorTile> grid;
        // Disable visualization for part 2 as it is not possible to allocate
        // such a big grid.
        if (input_file.find("puzzle-input") == std::string::npos) {
                int64_t max_y, max_x;
                max_y = 0;
                max_x = 0;

                for (auto &point : points) {
                        max_x = std::max(max_x, point.x);
                        max_y = std::max(max_y, point.y);
                }

                for (auto &point : outside_points) {
                        max_x = std::max(max_x, point.x);
                        max_y = std::max(max_y, point.y);
                }

                std::cout << "Max x coordinate: " << max_x << std::endl;
                std::cout << "Max y coordinate: " << max_y << std::endl;

                std::vector<std::vector<FloorTile>> cells(
                    max_y + 1, std::vector(max_x + 1, FloorTile::Empty));
                grid = {cells};

                for (auto &point : points) {
                        std::cout << point << std::endl;
                        grid.set(point, FloorTile::RedTile);
                }

                for (auto &point : outside_points) {
                        std::cout << point << std::endl;
                        grid.set(point, FloorTile::BoundaryCorner);
                }
                std::cout << grid << std::endl;
        }

        // Now that we have found the outside boundary edges we can test if
        // any of the rectangles intersects with it.
        for (auto &rectangle : rectangles) {
                std::cout << "Processsing rectangle:" << std::endl;
                std::cout << *rectangle << std::endl;
                // std::cout << "Its area is: "
                //           << calculate_area(rectangle->primary_corner,
                //                             rectangle->opposite_corner)
                //           << std::endl;
                bool good_rectangle = true;

                for (int i = 0; i < outside_points.size() - 1; i++) {
                        Segment boundary_segment = {outside_points[i],
                                                    outside_points[i + 1]};
                        if (rectangle->intersects(boundary_segment)) {
                                good_rectangle = false;
                                break;
                        }
                }

                if (good_rectangle) {
                        std::cout << "Found a rectangle enclosed in the region."
                                  << std::endl;
                        std::cout << "Its area is: "
                                  << calculate_area(rectangle->primary_corner,
                                                    rectangle->opposite_corner)
                                  << std::endl;
                        break;
                }
        }
}
