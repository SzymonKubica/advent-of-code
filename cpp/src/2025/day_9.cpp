#include "day_9.hpp"
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

bool is_within_enclosed_region(
    const std::pair<Point, Point> &segment,
    std::vector<std::pair<const Point &, const Point &>> boundary_segments)
{
        std::map<Direction, int> direction_to_intersection_counts;

        for (auto &[left, right] : boundary_segments) {
                if (left.x != right.x) {
                        assert(left.y == right.y);
                        // Handle intersection with horizontal segment
                        if (std::min(left.x, right.x) <= segment.x &&
                            segment.x <= std::max(left.x, right.x)) {
                                if (segment.y == left.y) {
                                        // We short-circuit here as the point
                                        // lies directly on one of the boundary
                                        // segments.
                                        return true;
                                }
                                if (segment.y < left.y) {
                                        direction_to_intersection_counts
                                            [Direction::Down]++;
                                }
                                if (segment.y > left.y) {
                                        direction_to_intersection_counts
                                            [Direction::Up]++;
                                }
                        }

                        if (segment.y == left.y) {
                                // handle the intersection with an edge when the
                                // ray is travelling parallel to it.
                                if (std::min(left.x, right.x) > segment.x) {
                                        direction_to_intersection_counts
                                            [Direction::Right]--;
                                }
                                if (std::max(left.x, right.x) < segment.x) {
                                        direction_to_intersection_counts
                                            [Direction::Left]--;
                                }
                        }
                } else {
                        assert(left.y != right.y);
                        // Handle intersection with vertical segment
                        if (std::min(left.y, right.y) <= segment.y &&
                            segment.y <= std::max(left.y, right.y)) {
                                if (segment.x == left.x) {
                                        // We short-circuit here as the point
                                        // lies directly on one of the boundary
                                        // segments.
                                        return true;
                                }
                                if (segment.x < left.x) {
                                        direction_to_intersection_counts
                                            [Direction::Right]++;
                                }
                                if (segment.x > left.x) {
                                        direction_to_intersection_counts
                                            [Direction::Left]++;
                                }
                        }
                        if (segment.x == left.x) {
                                // handle the intersection with an edge when the
                                // ray is travelling parallel to it.
                                if (std::min(left.y, right.y) > segment.y) {
                                        direction_to_intersection_counts
                                            [Direction::Down]--;
                                }
                                if (std::max(left.y, right.y) < segment.y) {
                                        direction_to_intersection_counts
                                            [Direction::Up]--;
                                }
                        }
                }
        }

        std::cout << direction_to_intersection_counts[Direction::Up]
                  << std::endl;
        std::cout << direction_to_intersection_counts[Direction::Down]
                  << std::endl;
        std::cout << direction_to_intersection_counts[Direction::Left]
                  << std::endl;
        std::cout << direction_to_intersection_counts[Direction::Right]
                  << std::endl;

        return direction_to_intersection_counts[Direction::Up] % 2 == 1 &&
               direction_to_intersection_counts[Direction::Left] % 2 == 1 &&
               direction_to_intersection_counts[Direction::Right] % 2 == 1 &&
               direction_to_intersection_counts[Direction::Down] % 2 == 1;
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

        // For each of the rectangles we need to check whether all of its
        // corners lie within the enclosed area.
        for (auto &rectangle : rectangles) {
                std::cout << "Processsing rectangle:" << std::endl;
                std::cout << *rectangle << std::endl;
                std::cout << "Its area is: "
                          << calculate_area(rectangle->primary_corner,
                                            rectangle->opposite_corner)
                          << std::endl;
                bool good_rectangle = true;
                for (auto &segment : rectangle->get_segments()) {
                        bool result = is_within_enclosed_region(
                            segment, boundary_segments);
                        if (result) {
                                std::cout << "Segment between" << segment.first
                                          << " and " << segment.second
                                          << " is enclosed in the region."
                                          << std::endl;
                        } else {
                                std::cout << "Segment between" << segment.first
                                          << " and " << segment.second
                                          << " is not enclosed in the region. "
                                             "The rectangle is not valid."
                                          << std::endl;
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
