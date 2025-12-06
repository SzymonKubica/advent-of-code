#include <vector>
#include <string>
#include <cstdint>
#include <iostream>

std::vector<std::string> read_lines_from_file(const std::string &file_path);

enum class Direction : uint8_t { Up, Down, Left, Right };

/**
 * Represents a point on a grid.
 */
struct Point {
        int x;
        int y;

      public:
        Point translate(Direction direction) const;
        Point translate(Direction direction, int distance) const;
        Point translate(const Point &translation_vector) const;
        Point translate(const Point &translation_vector, int repeat) const;
        std::vector<Point> get_neighbours() const;
        std::vector<Point> get_adjacent() const;
};

extern const Point ORIGIN;
extern const Point NORTH;
extern const Point SOUTH;
extern const Point EAST;
extern const Point WEST;
extern const Point NORTH_EAST;
extern const Point NORTH_WEST;
extern const Point SOUTH_EAST;
extern const Point SOUTH_WEST;

template <typename T> struct GridPoint {
        Point location;
        T value;
};

template <typename T> struct Grid {
        std::vector<std::vector<T>> cells;

      public:
        std::vector<GridPoint<T>> get_neighbours(const GridPoint<T> &location)
        {
        }

        /**
         * Checks if a given point lies inside the grid. Note that here we
         * assume that the grid is rectangular and all rows have the same
         * length. Because of this we check the x coordinate against the size of
         * the first row.
         */
        bool is_within_bounds(const Point &point)
        {
                return 0 <= point.y && point.y <= this->cells.size() &&
                       0 <= point.x && point.x <= this->cells[0].size();
        }
};

template <typename T>
std::ostream &operator<<(std::ostream &os, const Grid<T> &grid)
{
        for (auto &row : grid.cells) {
                for (const auto &cell : row) {
                        os << cell;
                }
                os << std::endl;
        }
        return os;
}
