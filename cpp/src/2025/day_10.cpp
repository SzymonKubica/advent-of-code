#include "day_10.hpp"
#include <climits>
#include <cstdint>
#include <queue>
#include <string>
#include <vector>
#include <set>
#include <map>
#include <cassert>
#include <istream>
#include <sstream>
#include "../utils.hpp"

struct ButtonWiringSchematic {
        std::vector<int> buttons_controlled;
};

struct IndicatorLights {
        std::vector<bool> lights_states;

      public:
        /**
         * Given a button wiring configuration it 'applies' it to the current
         * state of the indicator lights, changing the state of the toggled
         * lights.
         */
        IndicatorLights
        apply_button_press(const ButtonWiringSchematic &schematic) const
        {
                std::vector<bool> transformed(lights_states.begin(),
                                              lights_states.end());
                for (int idx : schematic.buttons_controlled) {
                        assert(idx < lights_states.size());
                        transformed[idx] = !transformed[idx];
                }
                return IndicatorLights{.lights_states = transformed};
        }

        IndicatorLights get_initial_state() const
        {
                return {std::vector<bool>(this->lights_states.size(), false)};
        }

        bool operator==(const IndicatorLights &other) const
        {
                return lights_states == other.lights_states;
        }

        bool operator<(const IndicatorLights &other) const
        {
                return lights_states < other.lights_states;
        }
};

/**
 * Parses the indicator lights given the input of the form "[.##.]",
 * where . means off and # means on.
 */
IndicatorLights parse_indicator_lights(std::string input)
{
        assert(input.at(0) == '[' && input.at(input.size() - 1) == ']');
        auto symbols_str = input.substr(1, input.size() - 2);

        std::vector<bool> states(symbols_str.size(), false);
        for (int i = 0; i < symbols_str.size(); i++) {
                if (symbols_str.at(i) == '#') {
                        states[i] = true;
                }
        }

        return IndicatorLights{states};
}

std::vector<int> parse_comma_separated_numbers(std::string input)
{
        std::vector<int> numbers;

        std::istringstream ss(input);
        std::string number;

        while (std::getline(ss, number, ',')) {
                numbers.push_back(std::stoi(number));
        }
        return numbers;
}

/**
 * Parses button wiring schematics given an input "(0,1,2,3,4)"
 */
ButtonWiringSchematic parse_button_wiring(std::string input)
{
        assert(input.at(0) == '(' && input.at(input.size() - 1) == ')');
        auto numbers_str = input.substr(1, input.size() - 2);
        return ButtonWiringSchematic{
            parse_comma_separated_numbers(numbers_str)};
}

struct JoltageRequirements {
        std::vector<int> required_joltages;
};

JoltageRequirements parse_joltage_requirements(std::string input)
{
        assert(input.at(0) == '{' && input.at(input.size() - 1) == '}');
        auto requirements_values_str = input.substr(1, input.size() - 2);
        return JoltageRequirements{
            parse_comma_separated_numbers(requirements_values_str)};
}

struct MachineSpecification {
        IndicatorLights lights;
        std::vector<ButtonWiringSchematic> buttons;
        JoltageRequirements joltage_requirements;
};

/* Parsed output formatting */
std::ostream &operator<<(std::ostream &os, const IndicatorLights &lights)
{
        os << "[";
        for (bool status : lights.lights_states) {
                os << (status ? "#" : ".");
        }
        os << "]";
        return os;
}

std::ostream &operator<<(std::ostream &os, const std::vector<int> &numbers)
{
        for (int i = 0; i < numbers.size(); i++) {
                os << numbers[i];
                if (i < numbers.size() - 1) {
                        os << ",";
                }
        }
        return os;
}

std::ostream &operator<<(std::ostream &os,
                         const ButtonWiringSchematic &button_wiring)
{
        return os << "(" << button_wiring.buttons_controlled << ")";
}

std::ostream &operator<<(std::ostream &os,
                         const JoltageRequirements &joltage_requirements)
{
        return os << "{" << joltage_requirements.required_joltages << "}";
}

std::ostream &operator<<(std::ostream &os,
                         const MachineSpecification &machine_spec)
{

        os << "Machine specification: " << std::endl;
        os << "Required indicator lights: " << machine_spec.lights << std::endl;
        os << "Available button wirings: " << std::endl;
        for (auto button_spec : machine_spec.buttons) {
                os << button_spec << std::endl;
        }
        os << "Joltage requirements: " << machine_spec.joltage_requirements
           << std::endl;

        return os;
}

MachineSpecification parse_machine_specification(std::string line)
{
        std::vector<std::string> space_separated_text;
        std::istringstream ss(line);
        std::string word;
        while (std::getline(ss, word, ' ')) {
                space_separated_text.push_back(word);
        }

        // we assume that there input here is well formed and contains
        // at least 3 elemnts.
        assert(space_separated_text.size() >= 3);
        IndicatorLights lights_spec =
            parse_indicator_lights(space_separated_text[0]);
        JoltageRequirements joltage_requirements = parse_joltage_requirements(
            space_separated_text[space_separated_text.size() - 1]);

        std::vector<ButtonWiringSchematic> button_schematics;
        for (int i = 1; i < space_separated_text.size() - 1; i++) {
                button_schematics.push_back(
                    parse_button_wiring(space_separated_text[i]));
        }
        return {lights_spec, button_schematics, joltage_requirements};
}

std::vector<MachineSpecification>
read_specifications_from_file(std::string input_file)
{
        std::vector<MachineSpecification> specifications;
        auto lines = read_lines_from_file(input_file);
        for (auto &line : lines) {
                specifications.push_back(parse_machine_specification(line));
        }

        return specifications;
}

/*
int search_lights_states(const IndicatorLights &target_state,
                         const IndicatorLights &current_state,
                         const std::vector<ButtonWiringSchematic> &schematics,
                         std::map<IndicatorLights, int> &state_distance_map)
{
        if (target_state == current_state) {
                return 0;
        }
        int minimum = 10000000;
        visited_states.insert(current_state);
        for (const auto &button : schematics) {
                auto new_state = current_state.apply_button_press(button);
                if (!visited_states.contains(new_state)) {
                  std::cout << new_state << std::endl;
                        int min_through_new =
                            search_lights_states(target_state, new_state,
                                                 schematics, visited_states);
                        minimum = std::min(minimum, min_through_new);
                        visited_states.insert(new_state);
                }
        }
        return minimum + 1;
}
*/

typedef std::pair<IndicatorLights, int> StateAndDistance;
struct DistanceCompare {
        bool operator()(const StateAndDistance &a,
                        const StateAndDistance &b) const
        {
                return a.second > b.second; // min-heap
        }
};

int find_shortest_action_sequence(
    const IndicatorLights &lights,
    const std::vector<ButtonWiringSchematic> &schematics)
{
        auto initial_state = lights.get_initial_state();

        std::priority_queue<StateAndDistance, std::vector<StateAndDistance>,
                            DistanceCompare>
            queue;

        std::map<IndicatorLights, int> distance_map;

        queue.push({initial_state, 0});
        distance_map[initial_state] = 0;

        while (!queue.empty()) {
                const auto [state, distance] = queue.top();
                for (auto &button : schematics) {
                        auto new_state = state.apply_button_press(button);
                        std::cout << new_state << std::endl;
                        int distance_through_curr = distance + 1;

                        if (!distance_map.contains(new_state)) {
                                distance_map[new_state] = distance_through_curr;
                                StateAndDistance new_entry = {
                                  std::move(new_state), distance_through_curr};
                                queue.push(new_entry);
                                continue;
                        }

                        if (distance_map.contains(new_state) &&
                            distance_map[new_state] > distance_through_curr) {
                                distance_map[new_state] = distance_through_curr;
                        }
                }
                queue.pop();
        }
        return distance_map[lights];
}

void Year2025Day10::first_part(std::string input_file)
{
        int total_button_presses = 0;
        auto specifications = read_specifications_from_file(input_file);
        for (auto &spec : specifications) {
                std::cout << spec << std::endl;
                int presses_required =
                    find_shortest_action_sequence(spec.lights, spec.buttons);
                std::cout << "Fewest button presses required: "
                          << presses_required << std::endl;
                total_button_presses += presses_required;
        }
        std::cout << "A total of " << total_button_presses
                  << " is required to correctly configure all machines."
                  << std::endl;
}

void Year2025Day10::second_part(std::string input_file) {}
