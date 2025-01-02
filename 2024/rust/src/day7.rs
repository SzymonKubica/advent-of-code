use std::{fs, vec};

pub fn part1(input_file: &str) {
    let input = fs::read_to_string(input_file).unwrap();
    let equations: Vec<CalibrationEquation> =
        input.lines().map(CalibrationEquation::from).collect();
    println!("Parsed calibration equations:\n{:?}", equations);

    let satisfiable: Vec<CalibrationEquation> = equations
        .into_iter()
        .filter(CalibrationEquation::is_satisfiable)
        .collect();

    println!("Satisfiable calibration equations:\n{:?}", satisfiable);
    let calibration_sum: usize = satisfiable.iter().map(|eq| eq.expected_output).sum();
    println!("Calibration sum: {}", calibration_sum);
}
pub fn part2(input_file: &str) {
    let input = fs::read_to_string(input_file).unwrap();
    let equations: Vec<CalibrationEquation> =
        input.lines().map(CalibrationEquation::from).collect();
    println!("Parsed calibration equations:\n{:?}", equations);

    let satisfiable: Vec<CalibrationEquation> = equations
        .into_iter()
        .filter(CalibrationEquation::is_satisfiable_part_2)
        .collect();

    println!("Satisfiable calibration equations:\n{:?}", satisfiable);
    let calibration_sum: usize = satisfiable.iter().map(|eq| eq.expected_output).sum();
    println!("Calibration sum: {}", calibration_sum);
}

#[derive(Debug)]
struct CalibrationEquation {
    expected_output: usize,
    arguments: Vec<usize>,
}

impl CalibrationEquation {
    pub fn is_satisfiable(&self) -> bool {
        let all_possible_operator_assignments =
            Self::get_all_possible_operator_assignments(self.arguments.len() - 1);
        all_possible_operator_assignments
            .iter()
            .any(|assignment| Self::assignment_matches(self.expected_output, &self.arguments, assignment))
    }

    pub fn is_satisfiable_part_2(&self) -> bool {
        let all_possible_operator_assignments =
            Self::get_all_possible_operator_assignments_part_2(self.arguments.len() - 1);
        all_possible_operator_assignments
            .iter()
            .any(|assignment| Self::assignment_matches(self.expected_output, &self.arguments, assignment))
    }

    fn assignment_matches(
        expected: usize,
        arguments: &Vec<usize>,
        operations: &Vec<Operation>,
    ) -> bool {
        let result: usize = Self::evaluate(arguments, operations);
        expected == result
    }

    fn evaluate(arguments: &Vec<usize>, operations: &Vec<Operation>) -> usize {
        let mut output: usize = arguments[0];

        for (i, arg) in arguments[1..].iter().enumerate() {
            output = operations[i].apply(output, *arg);
        }
        output
    }

    fn get_all_possible_operator_assignments(operator_num: usize) -> Vec<Vec<Operation>> {
        if operator_num == 1 {
            return vec![vec![Operation::Addition], vec![Operation::Multiplication]];
        }
        let recursive_result = Self::get_all_possible_operator_assignments(operator_num - 1);
        let mut output = vec![];
        for sub_assignment in recursive_result {
            let mut with_addition = vec![Operation::Addition];
            with_addition.extend(sub_assignment.clone());
            output.push(with_addition);

            let mut with_multiplication = vec![Operation::Multiplication];
            with_multiplication.extend(sub_assignment);
            output.push(with_multiplication);
        }

        output
    }

    fn get_all_possible_operator_assignments_part_2(operator_num: usize) -> Vec<Vec<Operation>> {
        if operator_num == 1 {
            return vec![vec![Operation::Addition], vec![Operation::Multiplication], vec![Operation::Concatenation]];
        }
        let recursive_result = Self::get_all_possible_operator_assignments_part_2(operator_num - 1);
        let mut output = vec![];
        for sub_assignment in recursive_result {
            let mut with_addition = vec![Operation::Addition];
            with_addition.extend(sub_assignment.clone());
            output.push(with_addition);

            let mut with_multiplication = vec![Operation::Multiplication];
            with_multiplication.extend(sub_assignment.clone());
            output.push(with_multiplication);

            let mut with_concatenation = vec![Operation::Concatenation];
            with_concatenation.extend(sub_assignment);
            output.push(with_concatenation);
        }

        output
    }
}

impl From<&str> for CalibrationEquation {
    fn from(value: &str) -> Self {
        let parts: Vec<&str> = value.split(":").collect();
        let expected_output = parts[0].parse::<usize>().unwrap();
        let arguments: Vec<usize> = parts[1]
            .split(" ")
            .filter_map(|s| s.parse::<usize>().map_or(None, |r| Some(r)))
            .collect();

        CalibrationEquation {
            expected_output,
            arguments,
        }
    }
}

#[derive(Copy, Clone)]
enum Operation {
    Addition,
    Multiplication,
    Concatenation,
}

impl Operation {
    pub fn apply(&self, x: usize, y: usize) -> usize {
        match *self {
            Operation::Addition => x + y,
            Operation::Multiplication => x * y,
            Operation::Concatenation => (x.to_string() + &y.to_string()).parse::<usize>().unwrap()
        }
    }
}
