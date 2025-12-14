#include "day_9.hpp"
#include <string>
#include <vector>
#include <ostream>
#include <sstream>
#include <algorithm>
#include <string>
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
};

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

void Year2025Day9::second_part(std::string input_file)
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

        std::cout << "Top 10 pairs:" << std::endl;
        for (int i = 0; i < 10; i++) {
                std::cout << pairs_with_areas[i].first.first << std::endl;
                std::cout << pairs_with_areas[i].first.second << std::endl;
                std::cout << "Area: " << pairs_with_areas[i].second
                          << std::endl;
        }

        std::vector<Rectangle *> rectangles;
        for (auto pair_with_area : pairs_with_areas) {
                rectangles.push_back(new Rectangle(
                    pair_with_area.first.first, pair_with_area.first.second));
        }
}
