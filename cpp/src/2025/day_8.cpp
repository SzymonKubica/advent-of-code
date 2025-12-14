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
        const Point3d *first;
        const Point3d *second;
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

      public:
        DisjointSetNode()
        {
                this->parent = this;
                this->size = 1;
        }
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

int union_sets_get_resulting_size(DisjointSetNode *node1,
                                  DisjointSetNode *node2)
{
        auto parent1 = find_parent(node1);
        auto parent2 = find_parent(node2);
        if (parent1 == parent2) {
                return parent1->size;
        }
        parent2->parent = parent1;
        parent1->size += parent2->size;
        return parent1->size;
}

std::ostream &operator<<(std::ostream &os, const PointsAndDistance &points)
{
        os << *(points.first) << std::endl;
        os << *(points.second) << std::endl;
        os << "Distance: " << points.distance;
        return os;
}

void log_connection(Point3d *first, Point3d *second)
{
        std::cout << "Connecting:" << std::endl;
        std::cout << *first << std::endl;
        std::cout << *second << std::endl;
}

std::vector<PointsAndDistance>
get_point_pairs_sorted_by_distance(const std::vector<Point3d> &points)
{
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
        return distances;
}

void Year2025Day8::first_part(std::string input_file)
{
        auto points = read_points_from_file(input_file);

        auto distances = get_point_pairs_sorted_by_distance(points);

        std::map<const Point3d *, DisjointSetNode *> circuit_map;
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
                if (!circuit_map.contains(first))
                        circuit_map[first] = new DisjointSetNode();
                if (!circuit_map.contains(second))
                        circuit_map[second] = new DisjointSetNode();

                union_sets(circuit_map[first], circuit_map[second]);
                connections++;
                current_pair_idx++;
        }

        std::set<DisjointSetNode *> unique_circuits;
        for (auto &[key, value] : circuit_map) {
                unique_circuits.insert(find_parent(value));
        }

        std::vector<DisjointSetNode *> circuits(unique_circuits.begin(),
                                                unique_circuits.end());

        auto greater_size = [](DisjointSetNode *node1, DisjointSetNode *node2) {
                return node1->size > node2->size;
        };

        std::sort(circuits.begin(), circuits.end(), greater_size);

        for (auto circuit : circuits) {
                std::cout << "Found a set with: " << circuit->size
                          << " elements" << std::endl;
        }

        uint64_t output = 1;
        for (int i = 0; i < 3; i++) {
                output *= circuits[i]->size;
        }

        std::cout << "The product of the sizes of three largest circuits is: "
                  << output << std::endl;
}

void Year2025Day8::second_part(std::string input_file)
{

        auto points = read_points_from_file(input_file);

        auto distances = get_point_pairs_sorted_by_distance(points);

        std::map<const Point3d *, DisjointSetNode *> circuit_map;

        int current_pair_idx = 0;

        while (true) {
                auto &distance = distances[current_pair_idx];

                auto first = distance.first;
                auto second = distance.second;
                if (!circuit_map.contains(first))
                        circuit_map[first] = new DisjointSetNode();
                if (!circuit_map.contains(second))
                        circuit_map[second] = new DisjointSetNode();

                int new_size = union_sets_get_resulting_size(
                    circuit_map[first], circuit_map[second]);

                if (new_size == points.size()) {
                        std::cout << "Points form one circuit." << std::endl;
                        std::cout << "The last two points: " << std::endl;
                        std::cout << *first << std::endl;
                        std::cout << *second << std::endl;
                        int64_t product = first->x * second->x;
                        std::cout
                            << "Product of their X coordinates: " << product
                            << std::endl;
                        break;
                }
                current_pair_idx++;
        }
}
