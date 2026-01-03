#include <vector>
#include <string>
#include <cstdint>
#include <optional>
#include <iostream>

std::vector<std::string> read_lines_from_file(const std::string &file_path);
std::string read_file_as_string(const std::string &file_path);

enum class Direction : uint8_t { Up, Down, Left, Right };

Direction opposite_direction(Direction direction);
Direction turn_clockwise(Direction direction);
Direction turn_anticlockwise(Direction direction);

/**
 * Represents a point on a grid.
 */
struct Point {
        int64_t x;
        int64_t y;

      public:
        Point translate(Direction direction) const;
        Point translate(Direction direction, int64_t distance) const;
        Point translate(const Point &translation_vector) const;
        Point translate(const Point &translation_vector, int64_t repeat) const;
        std::vector<Point> get_neighbours() const;
        std::vector<Point> get_adjacent() const;
        bool operator<(const Point &other) const;
        Point operator+(const Point &other) const;
        Point operator-(const Point &other) const;
        Point modulus() const;
};

typedef std::pair<Point, Point> Segment;

bool point_lies_on_segment(const Point&point, const Segment&segment);
Direction segment_direction(const Segment&segment);
Direction absolute_direction(Direction current_direction, Direction relative);

struct Point3d {
        int64_t x;
        int64_t y;
        int64_t z;

      public:
        Point3d operator+(const Point3d &other) const;
        Point3d operator-(const Point3d &other) const;
        Point3d operator*(int64_t scalar) const;
        double magnitude() const;
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
        std::vector<GridPoint<T>>
        get_neighbours(const GridPoint<T> &location) const
        {
                std::vector<GridPoint<T>> neighbours;
                for (auto nb_point : location.location.get_neighbours()) {
                        if (this->is_within_bounds(nb_point)) {
                                neighbours.emplace_back(
                                    nb_point, this->index(nb_point).value());
                        }
                }
                return neighbours;
        }

        std::optional<T> index(const Point &location) const
        {
                if (!this->is_within_bounds(location)) {
                        return std::nullopt;
                }
                return cells[location.y][location.x];
        }

        void set(const Point &location, T value)
        {
                if (!this->is_within_bounds(location)) {
                        throw std::invalid_argument(
                            "Tried to set a grid value that is out of bounds.");
                }
                cells[location.y][location.x] = value;
        }

        std::vector<std::vector<GridPoint<T>>> get_grid_of_points() const
        {
                std::vector<std::vector<GridPoint<T>>> grid;
                for (int y = 0; y < this->cells.size(); y++) {
                        std::vector<GridPoint<T>> row;
                        // We assume the grid is well-formed i.e rectangular
                        for (int x = 0; x < this->cells[0].size(); x++) {
                                Point p = {x, y};
                                row.push_back({p, this->cells[y][x]});
                        }
                        grid.push_back(row);
                }
                return grid;
        }

        /**
         * Checks if a given point lies inside the grid. Note that here we
         * assume that the grid is rectangular and all rows have the same
         * length. Because of this we check the x coordinate against the size of
         * the first row.
         */
        bool is_within_bounds(const Point &point) const
        {
                return 0 <= point.y && point.y < this->cells.size() &&
                       0 <= point.x && point.x < this->cells[0].size();
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

std::ostream &operator<<(std::ostream &os, const Point3d &point);
std::ostream &operator<<(std::ostream &os, const Point &point);
