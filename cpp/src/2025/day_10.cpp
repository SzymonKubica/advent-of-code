#include "day_10.hpp"
#include <string>
#include <vector>
#include <cassert>
#include <istream>
#include <sstream>
#include "../utils.hpp"

struct IndicatorLights {
        std::vector<bool> lights_states;
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

struct ButtonWiringSchematic {
        std::vector<int> buttons_controlled;
};

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

std::ostream &operator<<(std::ostream &os,
                         const ButtonWiringSchematic &button_wiring)
{
        os << "(";
        for (int i = 0; i < button_wiring.buttons_controlled.size(); i++) {
                os << button_wiring.buttons_controlled[i];
                if (i < button_wiring.buttons_controlled.size() - 1) {
                        os << ",";
                }
        }
        os << ")";
        return os;
}

std::ostream &operator<<(std::ostream &os,
                         const JoltageRequirements &joltage_requirements)
{
        os << "{";
        for (int i = 0; i < joltage_requirements.required_joltages.size();
             i++) {
                os << joltage_requirements.required_joltages[i];
                if (i < joltage_requirements.required_joltages.size() - 1) {
                        os << ",";
                }
        }
        os << "}";
        return os;
}

std::ostream &operator<<(std::ostream &os,
                         const MachineSpecification &machine_spec)
{

        os << machine_spec.lights << " ";
        for (auto button_spec : machine_spec.buttons) {
                os << button_spec << " ";
        }
        os << machine_spec.joltage_requirements;

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

        // we assume that there input here is well formed and contains at least
        // 3 elemnts.
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

void Year2025Day10::first_part(std::string input_file)
{
        auto specifications = read_lines_from_file(input_file);
        for (auto &spec : specifications) {
                std::cout << spec << std::endl;
        }
}

void Year2025Day10::second_part(std::string input_file) {}
