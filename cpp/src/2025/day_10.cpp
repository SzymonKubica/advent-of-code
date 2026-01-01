#include "day_10.hpp"
#include <queue>
#include <string>
#include <vector>
#include <map>
#include <cassert>
#include <sstream>
#include <algorithm>
#include <iomanip>
#include "../utils.hpp"
#include <z3++.h>

struct ButtonWiringSchematic {
        std::vector<int> buttons_controlled;
};

class GaussianEliminationMatrix
{

      public:
        std::vector<std::vector<double>> augmented_matrix;
        void swap_rows(int first_row_idx, int second_row_idx)
        {
                augmented_matrix[first_row_idx].swap(
                    augmented_matrix[second_row_idx]);
        }
        void scale_row(int row_idx, double multiplier)
        {
                for (int i = 0; i < augmented_matrix[row_idx].size(); i++) {
                        augmented_matrix[row_idx][i] *= multiplier;
                }
        }
        void add_multiple(int source_row_idx, int destination_row_idx,
                          double scale)
        {
                for (int i = 0; i < augmented_matrix[source_row_idx].size();
                     i++) {
                        augmented_matrix[destination_row_idx][i] +=
                            augmented_matrix[source_row_idx][i] * scale;
                }
        }

        void pick_pivot_and_make_remaining_rows_zero(int target_column_index)
        {
                int pivot_index = -1;
                for (int i = target_column_index; i < augmented_matrix.size();
                     i++) {
                        if (augmented_matrix[i][target_column_index] != 0) {
                                pivot_index = i;
                                break;
                        }
                }
                std::cout << "Found pivot index: " << pivot_index << std::endl;

                if (pivot_index == -1) {
                        return;
                }

                double pivot_value =
                    augmented_matrix[pivot_index][target_column_index];
                scale_row(pivot_index, 1 / pivot_value);

                for (int i = 0; i < augmented_matrix.size(); i++) {
                        if (i == pivot_index) {
                                continue;
                        }

                        double target_value =
                            augmented_matrix[i][target_column_index];
                        if (augmented_matrix[i][target_column_index] != 0) {
                                add_multiple(pivot_index, i, -1 * target_value);
                        }
                }

                if (pivot_index != -1 &&
                    target_column_index < augmented_matrix.size()) {
                        std::cout << "Swapping rows " << pivot_index << " "
                                  << target_column_index << std::endl;
                        swap_rows(pivot_index, target_column_index);
                }
        }
};

std::ostream &operator<<(std::ostream &os,
                         const GaussianEliminationMatrix &matrix)
{
        unsigned long max_width = 0;
        int default_double_decimals = 5;
        for (auto &row : matrix.augmented_matrix) {
                for (int i = 0; i < row.size(); i++) {

                        max_width = std::max(max_width,
                                             std::to_string(row[i]).length());
                }
        }
        for (auto &row : matrix.augmented_matrix) {
                for (int i = 0; i < row.size(); i++) {
                        if (i == row.size() - 1) {
                                os << " | ";
                        }
                        os << std::setw(max_width - default_double_decimals)
                           << row[i];
                }
                os << std::endl;
        }
        return os;
}

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
                std::vector<bool> transformed(lights_states);
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

struct JoltageRequirements {
        std::vector<int> required_joltages;

      public:
        JoltageRequirements
        apply_button_press(const ButtonWiringSchematic &schematic) const
        {
                std::vector<int> transformed(required_joltages);
                for (int idx : schematic.buttons_controlled) {
                        assert(idx < required_joltages.size());
                        transformed[idx]++;
                }
                return JoltageRequirements{.required_joltages = transformed};
        }

        JoltageRequirements get_initial_state() const
        {
                return {std::vector<int>(this->required_joltages.size(), 0)};
        }

        bool operator==(const JoltageRequirements &other) const
        {
                return required_joltages == other.required_joltages;
        }

        bool operator<(const JoltageRequirements &other) const
        {
                return required_joltages < other.required_joltages;
        }

        bool is_exceeded_by(const JoltageRequirements &other) const
        {
                for (int i = 0; i < required_joltages.size(); i++) {
                        if (required_joltages[i] < other.required_joltages[i]) {
                                return true;
                        }
                }
                return false;
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

typedef std::pair<IndicatorLights, int> StateAndDistance;
typedef std::pair<JoltageRequirements, int> JoltageStateAndDistance;
struct DistanceCompare {
        bool operator()(const StateAndDistance &a,
                        const StateAndDistance &b) const
        {
                return a.second > b.second; // min-heap
        }
};

struct JoltageDistanceCompare {
        bool operator()(const JoltageStateAndDistance &a,
                        const JoltageStateAndDistance &b) const
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
                                    std::move(new_state),
                                    distance_through_curr};
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

// we need to constrain the search space somehow.

int find_shortest_action_sequence_joltages(
    const JoltageRequirements &requirements,
    const std::vector<ButtonWiringSchematic> &schematics)
{
        auto initial_state = requirements.get_initial_state();

        // we assemble the matrix representing the system of linear equations
        // and try to solve it using gaussian elimination.
        // each of the button wiring schematics is translated into a column of
        // the augmented gaussian elimination matrix.
        std::vector<std::vector<double>> augmented_matrix(
            requirements.required_joltages.size(),
            std::vector(schematics.size() + 1, 0.0));

        for (int j = 0; j < schematics.size(); j++) {
                auto &schematic = schematics[j];
                for (int button_index : schematic.buttons_controlled) {
                        augmented_matrix[button_index][j] = 1;
                }
        }

        for (int i = 0; i < requirements.required_joltages.size(); i++) {
                auto &required_joltage = requirements.required_joltages[i];
                augmented_matrix[i][schematics.size()] = required_joltage;
        }

        GaussianEliminationMatrix matrix{augmented_matrix};

        std::cout << matrix << std::endl;

        for (int i = 0; i < matrix.augmented_matrix.size(); i++) {
                std::cout << "Pivoting column " << i << std::endl;
                matrix.pick_pivot_and_make_remaining_rows_zero(i);
                std::cout << "After pivot" << std::endl;
                std::cout << matrix << std::endl;
        }

        return 0;
}

/**
 * A 'cheating' solution that translates the problem into a linear optimization
 * problem and solves it using Z3. The idea here is to explore the Z3 API.
 */
int find_shortest_action_sequence_cheating(
    const JoltageRequirements &requirements,
    const std::vector<ButtonWiringSchematic> &schematics)
{
        z3::context c;
        z3::optimize opt(c);
        std::vector<z3::expr> button_press_counts;
        std::cout << "Processing " << schematics.size() << " button schematics."
                  << std::endl;
        for (int i = 0; i < schematics.size(); i++) {
                z3::expr expr =
                    c.int_const(("button" + std::to_string(i)).c_str());
                button_press_counts.push_back(expr);
                opt.add(0 <= expr);
        }

        for (int i = 0; i < requirements.required_joltages.size(); i++) {
                int required_joltage = requirements.required_joltages[i];
                z3::expr sum = c.int_val(0);
                for (int j = 0; j < schematics.size(); j++) {
                        auto &schematic = schematics[j];
                        if (std::find(schematic.buttons_controlled.begin(),
                                      schematic.buttons_controlled.end(), i) !=
                            schematic.buttons_controlled.end()) {
                                sum = sum + button_press_counts[j];
                        }
                }
                opt.add(sum == required_joltage);
        }

        z3::expr sum = c.int_val(0);
        for (auto &button : button_press_counts) {
                sum = sum + button;
        }

        z3::optimize::handle h = opt.minimize(sum);

        if (opt.check() == z3::sat) {
                z3::model m = opt.get_model();
                int minimum_presses_required = 0;
                for (auto &button : button_press_counts) {
                        int button_value = m.eval(button).as_int64();
                        std::cout << "Presses required for: " << button << " "
                                  << button_value << std::endl;
                        minimum_presses_required += button_value;
                }

                std::cout << "Total sum: " << minimum_presses_required << std::endl;
                return minimum_presses_required;
        }
        return -1;
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

void Year2025Day10::second_part(std::string input_file)
{
        int total_button_presses = 0;
        auto specifications = read_specifications_from_file(input_file);
        for (auto &spec : specifications) {
                std::cout << spec << std::endl;
                int presses_required = find_shortest_action_sequence_cheating(
                    spec.joltage_requirements, spec.buttons);
                std::cout << "Fewest button presses required: "
                          << presses_required << std::endl;
                total_button_presses += presses_required;
        }
        std::cout << "A total of " << total_button_presses
                  << " is required to correctly configure all machines."
                  << std::endl;
}
