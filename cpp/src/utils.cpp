#include <cstdint>
#include <sstream>
#include <fstream>
#include <cmath>
#include <iostream>
#include "./utils.hpp"

const Point ORIGIN = {0, 0};
const Point NORTH = ORIGIN.translate(Direction::Up);
const Point SOUTH = ORIGIN.translate(Direction::Down);
const Point EAST = ORIGIN.translate(Direction::Right);
const Point WEST = ORIGIN.translate(Direction::Left);
const Point NORTH_EAST = NORTH.translate(Direction::Left);
const Point NORTH_WEST = NORTH.translate(Direction::Right);
const Point SOUTH_EAST = SOUTH.translate(Direction::Left);
const Point SOUTH_WEST = SOUTH.translate(Direction::Right);

std::vector<std::string> read_lines_from_file(const std::string &file_path)
{

        std::ifstream file(file_path);
        if (!file.is_open()) {
                throw std::runtime_error("Could not open file");
        }

        std::ostringstream ss;
        ss << file.rdbuf();
        file.close();

        std::string file_contents = ss.str();

        std::istringstream iss(file_contents);

        std::vector<std::string> lines;
        std::string line;
        while (std::getline(iss, line)) {
                lines.emplace_back(line);
        }

        return lines;
}

Point Point::translate(Direction direction) const
{
        return this->translate(direction, 1);
}
Point Point::translate(Direction direction, int distance) const
{
        switch (direction) {
        case Direction::Up:
                return {.x = this->x, .y = this->y - distance};
        case Direction::Down:
                return {.x = this->x, .y = this->y + distance};
        case Direction::Left:
                return {.x = this->x - distance, .y = this->y};
        case Direction::Right:
                return {.x = this->x + distance, .y = this->y};
        default:
                throw std::invalid_argument("Invalid direction.");
        }
}
Point Point::translate(const Point &translation_vector) const
{
        return this->translate(translation_vector, 1);
}
Point Point::translate(const Point &translation_vector, int repeat) const
{

        return {.x = this->x + repeat * translation_vector.x,
                .y = this->y + repeat * translation_vector.y};
}
std::vector<Point> Point::get_neighbours() const
{
        std::vector<Point> neighbours;

        for (auto adjacent : this->get_adjacent()) {
                neighbours.push_back(adjacent);
        }

        neighbours.push_back(this->translate(NORTH_EAST));
        neighbours.push_back(this->translate(NORTH_WEST));
        neighbours.push_back(this->translate(SOUTH_EAST));
        neighbours.push_back(this->translate(SOUTH_WEST));

        return neighbours;
}
std::vector<Point> Point::get_adjacent() const
{
        return {
            this->translate(Direction::Up),
            this->translate(Direction::Down),
            this->translate(Direction::Left),
            this->translate(Direction::Right),
        };
}

/**
 * We need to implement this comparator so that maps from points work correctly.
 */
bool Point::operator<(const Point &other) const
{
        if (x != other.x)
                return x < other.x;
        return y < other.y;
}

Point3d Point3d::operator+(const Point3d &other) const
{
        return {.x = x + other.x, .y = y + other.y, .z = z + other.z};
}
Point3d Point3d::operator-(const Point3d &other) const
{
        return {.x = x - other.x, .y = y - other.y, .z = z - other.z};
}
Point3d Point3d::operator*(int64_t scalar) const
{
        return {scalar * x, scalar * y, scalar * z};
}

double Point3d::magnitude() const
{
        return std::sqrt((double)x * x + (double)y * y + (double)z * z);
}

std::ostream &operator<<(std::ostream &os, const Point3d &point)
{
        return os << "{x: " << point.x << ", y: " << point.y
                  << ", z: " << point.z << "}";
}
