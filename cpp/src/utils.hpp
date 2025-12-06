#include <vector>
#include <string>
#include <iostream>

std::vector<std::string> read_lines_from_file(const std::string &file_path);

template <typename T> struct Grid {
        std::vector<std::vector<T>> cells;

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
