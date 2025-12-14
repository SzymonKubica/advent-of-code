#include "day_11.hpp"
#include <string>
#include <vector>
#include <istream>
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

int find_paths_between_through(Device *start, Device *destination,
                               std::vector<std::string> to_visit,
                               std::set<std::string> visited)
{
        if (start == destination) {
                for (auto &device_to_visit : to_visit) {
                        if (!visited.contains(device_to_visit)) {
                                return 0;
                        }
                }
                return 1;
        }

        visited.insert(start->identifier);
        int paths = 0;
        for (auto output : start->outputs) {
                paths += find_paths_between_through(output, destination,
                                                    to_visit, visited);
        }
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
        for (auto &[identifier, device] : device_graph) {
                std::cout << *device << std::endl;
        }

        auto svr = device_graph["svr"];
        auto out = device_graph["out"];

        auto dac = device_graph["dac"];
        auto fft = device_graph["fft"];

        int possible_paths_from_dac = find_paths_between(dac, out, {});
        // int fft_to_dac = find_paths_between(fft, dac, {});
        int dac_to_fft = find_paths_between(dac, fft, {});
        int fft_to_dac = find_paths_between(fft, dac, {});
        // int possible_paths_from_fft = find_paths_between(fft, out, {});
        //  find_paths_between_through(you, out, {"dac", "fft"}, {});
        std::cout << "Found " << possible_paths_from_dac
                  << " paths between 'dac' and 'out'." << std::endl;
        // This gives 0 which means that fft is in front of dac
        // Whatever path goes through
        std::cout << "Found " << dac_to_fft
                  << " paths between 'dac' and 'fft'." << std::endl;
}
