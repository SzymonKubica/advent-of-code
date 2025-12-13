#include "day_8.hpp"
#include <cassert>
#include <cstdint>
#include <string>
#include <algorithm>
#include <vector>
#include <sstream>
#include <sstream>
#include <map>
#include <set>
#include <vector>
#include <string>
#include "../utils.hpp"

std::vector<Point3d> read_points_from_file(std::string input_file)
{
        auto lines = read_lines_from_file(input_file);
        std::vector<Point3d> points;

        for (auto &line : lines) {
                std::istringstream ss(line);
                std::vector<int64_t> coordinates;
                std::string coordinate;
                while (std::getline(ss, coordinate, ',')) {
                        coordinates.push_back((uint64_t)std::stoi(coordinate));
                }
                assert((coordinates.size() == 3) &&
                       "Expecting 3 coordinates for a 3d point.");
                points.push_back({.x = coordinates[0],
                                  .y = coordinates[1],
                                  .z = coordinates[2]});
        }

        return points;
}

struct PointsAndDistance {
        Point3d *first;
        Point3d *second;
        double distance;

      public:
        bool operator<(const PointsAndDistance &other)
        {
                return distance < other.distance;
        }
};

struct DisjointSetNode {
        DisjointSetNode *parent;
        int size;
};

DisjointSetNode *find_parent(DisjointSetNode *node)
{
        if (node->parent == node) {
                return node;
        }
        return find_parent(node->parent);
}

void union_sets(DisjointSetNode *node1, DisjointSetNode *node2)
{
        auto parent1 = find_parent(node1);
        auto parent2 = find_parent(node2);
        if (parent1 == parent2) {
                return;
        }
        parent2->parent = parent1;
        parent1->size += parent2->size;
}

std::ostream &operator<<(std::ostream &os, const PointsAndDistance &points)
{
        os << *(points.first) << std::endl;
        os << *(points.second) << std::endl;
        os << "Distance: " << points.distance;
        return os;
}

void Year2025Day8::first_part(std::string input_file)
{
        auto points = read_points_from_file(input_file);

        std::vector<PointsAndDistance> distances;
        for (int i = 0; i + 1 < points.size(); i++) {
                for (int j = i + 1; j < points.size(); j++) {
                        const auto &first = points[i];
                        const auto &second = points[j];
                        double distance = (first - second).magnitude();
                        distances.push_back({&points[i], &points[j], distance});
                }
        }

        for (int i = 0; i < 10; i++) {
                std::cout << distances[i] << std::endl;
        }

        std::sort(distances.begin(), distances.end());
        std::cout << "Distances sorted: " << std::endl;

        for (int i = 0; i < 10; i++) {
                std::cout << distances[i] << std::endl;
        }

        std::map<Point3d *, DisjointSetNode *> circuits;

        // For exaple inputs we need to take 10 point pairs and
        // for the real puzzle input it is 1000
        int required_connections = 1000;
        if (input_file.find("example") != std::string::npos) {
                required_connections = 10;
        }

        int connections = 0;
        int current_pair_idx = 0;

        while (connections < required_connections) {
                auto &distance = distances[current_pair_idx];

                auto first = distance.first;
                auto second = distance.second;
                if (!circuits.contains(first) && !circuits.contains(second)) {
                        DisjointSetNode *first_node = new DisjointSetNode{};
                        first_node->parent = first_node;
                        first_node->size = 2;
                        DisjointSetNode *second_node = new DisjointSetNode{};
                        second_node->parent = first_node;
                        second_node->size = 1;
                        circuits[first] = first_node;
                        circuits[second] = second_node;
                        connections++;
                        current_pair_idx++;
                        continue;
                }

                if (circuits.contains(first) && !circuits.contains(second)) {
                        DisjointSetNode *first_node = circuits[first];
                        DisjointSetNode *second_node = new DisjointSetNode{};
                        second_node->parent = first_node;
                        second_node->size = 1;
                        find_parent(first_node)->size++;
                        circuits[second] = second_node;
                        connections++;
                        current_pair_idx++;
                        continue;
                }

                if (circuits.contains(second) && !circuits.contains(first)) {
                        DisjointSetNode *second_node = circuits[second];
                        DisjointSetNode *first_node = new DisjointSetNode{};
                        first_node->parent = second_node;
                        first_node->size = 1;
                        find_parent(second_node)->size++;
                        circuits[first] = first_node;
                        connections++;
                        current_pair_idx++;
                        continue;
                }

                // case where both are already in the map
                auto parent1 = find_parent(circuits[first]);
                auto parent2 = find_parent(circuits[second]);
                if (parent1 == parent2) {
                        // already in the same set
                        current_pair_idx++;
                        continue;
                }
                union_sets(parent1, parent2);
                connections++;
                current_pair_idx++;
        }

        for (auto &[key, value] : circuits) {
                std::cout << *key << "in a set with: " << value->size
                          << std::endl;
        }
}

void Year2025Day8::second_part(std::string input_file) {}
