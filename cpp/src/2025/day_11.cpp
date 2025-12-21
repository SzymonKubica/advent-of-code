#include "day_11.hpp"
#include <cstdint>
#include <string>
#include <unordered_map>
#include <vector>
#include <sstream>
#include <map>
#include <set>
#include "../utils.hpp"

struct Device {
        std::string identifier;
        std::vector<Device *> outputs;
};

struct DeviceConfig {
        std::string identifier;
        std::vector<std::string> output_identifiers;
};

DeviceConfig from_string(std::string input)
{
        std::string identifier;
        std::string outputs_str;

        {
                std::istringstream ss(input);
                std::getline(ss, identifier, ':');
                std::getline(ss, outputs_str, ':');
        }

        // skip the leading space
        outputs_str = outputs_str.substr(1);

        std::vector<std::string> outputs;
        std::string output;

        {
                std::istringstream ss(outputs_str);
                while (std::getline(ss, output, ' ')) {
                        outputs.push_back(output);
                }
        }

        return {identifier, outputs};
}

std::ostream &operator<<(std::ostream &os, const Device &device)
{
        os << device.identifier << ": ";
        for (const auto &output : device.outputs) {
                os << output->identifier << " ";
        }
        return os;
}

std::ostream &operator<<(std::ostream &os, const DeviceConfig &config)
{
        os << config.identifier << ": ";
        for (const auto &output : config.output_identifiers) {
                os << output << " ";
        }
        return os;
}

std::vector<DeviceConfig> parse_device_configs_from_file(std::string input_file)
{
        std::vector<DeviceConfig> configs;
        for (auto &line : read_lines_from_file(input_file)) {
                configs.push_back(from_string(line));
        }
        return configs;
}

std::map<std::string, Device *>
assemble_device_graph(std::vector<DeviceConfig> configs)
{

        std::map<std::string, Device *> graph;

        for (const auto &config : configs) {
                Device *current;
                if (graph.contains(config.identifier)) {
                        current = graph[config.identifier];
                } else {
                        current = new Device();
                        current->identifier = config.identifier;
                        graph[config.identifier] = current;
                }

                for (const auto &output : config.output_identifiers) {
                        Device *output_device;
                        if (!graph.contains(output)) {
                                Device *output_device = new Device();
                                output_device->identifier = output;
                                graph[output] = output_device;
                        }
                        output_device = graph[output];
                        current->outputs.push_back(output_device);
                }
        }

        return graph;
}
int find_paths_between(Device *start, Device *destination,
                       std::set<Device *> visited)
{
        if (start == destination) {
                return 1;
        }

        int paths = 0;
        visited.insert(start);
        for (auto output : start->outputs) {
                if (!visited.contains(output)) {
                        paths +=
                            find_paths_between(output, destination, visited);
                }
        }
        return paths;
}

uint64_t
count_paths_between(int depth, Device *start, Device *destination,
                    std::unordered_map<std::string, int> &device_index_map,
                    std::vector<bool> visited,
                    std::unordered_map<std::string, uint64_t> &cache)
{
        if (cache.contains(start->identifier)) {
                return cache[start->identifier];
        }
        if (start == destination) {
                return 1;
        }

        for (int i = 0; i < depth; i++) {
                std::cout << " ";
        }
        std::cout << "Processing node: " << *start << std::endl;

        visited[device_index_map[start->identifier]] = true;
        uint64_t paths = 0;
        for (auto output : start->outputs) {
                if (!visited[device_index_map[output->identifier]]) {

                        paths += count_paths_between(
                            depth + 1, output, destination, device_index_map,
                            visited, cache);
                }
        }
        cache[start->identifier] = paths;
        return paths;
}

void Year2025Day11::first_part(std::string input_file)
{
        auto device_configs = parse_device_configs_from_file(input_file);
        for (auto config : device_configs) {
                std::cout << config << std::endl;
        }

        std::map<std::string, Device *> device_graph =
            assemble_device_graph(device_configs);

        std::cout << "Parsed device graph:" << std::endl;
        for (auto &[identifier, device] : device_graph) {
                std::cout << *device << std::endl;
        }

        auto you = device_graph["you"];
        auto out = device_graph["out"];

        int possible_paths = find_paths_between(you, out, {});
        std::cout << "Found " << possible_paths
                  << " paths between 'you' and 'out'." << std::endl;
}

void Year2025Day11::second_part(std::string input_file)
{

        auto device_configs = parse_device_configs_from_file(input_file);
        for (auto config : device_configs) {
                std::cout << config << std::endl;
        }

        std::map<std::string, Device *> device_graph =
            assemble_device_graph(device_configs);

        std::cout << "Parsed device graph:" << std::endl;
        std::unordered_map<std::string, int> device_index_map;
        int i = 0;
        for (auto &[identifier, device] : device_graph) {
                std::cout << *device << std::endl;
                device_index_map[identifier] = i;
                i++;
        }

        auto svr = device_graph["svr"];
        auto out = device_graph["out"];

        auto dac = device_graph["dac"];
        auto fft = device_graph["fft"];

        auto find_paths_between = [&device_index_map, &device_graph](
                                      Device *start, Device *destination,
                                      std::string first_name,
                                      std::string second_name) {
                std::vector<bool> visited(device_graph.size(), false);
                std::unordered_map<std::string, uint64_t> cache;
                uint64_t paths = count_paths_between(
                    0, start, destination, device_index_map, visited, cache);
                std::cout << "Found " << paths << " paths between '"
                          << first_name << "' and '" << second_name << "'"
                          << std::endl;
                return paths;
        };

        uint64_t svr_dac = find_paths_between(svr, dac, "svr", "dac");
        uint64_t svr_fft = find_paths_between(svr, fft, "svr", "ftt");
        uint64_t dac_fft = find_paths_between(dac, fft, "dac", "fft");
        uint64_t fft_dac = find_paths_between(fft, dac, "fft", "dac");
        uint64_t dac_out = find_paths_between(dac, out, "dac", "out");
        uint64_t fft_out = find_paths_between(fft, out, "fft", "out");

        uint64_t total_paths_dac_fft = svr_dac * dac_fft * fft_out;
        uint64_t total_paths_fft_dac = svr_fft * fft_dac * dac_out;

        std::cout << "Total paths through first dac and then fft: "
                  << total_paths_dac_fft << std::endl;
        std::cout << "Total paths through first fft and then dac: "
                  << total_paths_fft_dac << std::endl;

        std::cout << "Total paths: "
                  << total_paths_dac_fft + total_paths_fft_dac << std::endl;
}
