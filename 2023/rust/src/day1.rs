use std::fs;

// This function takes in a list of lines, all of which containing at least one
// digit. It is supposed to return the sum of the double digit numbers (calibration values)
// created by sticking together the first and last digits on each line.

pub fn part1(input_file: &str) {
    get_calibration_value(input_file, get_digit);
}

pub fn part2(input_file: &str) {
    get_calibration_value(input_file, get_digit_or_spelled_out);
}

fn get_calibration_value(input_file: &str, digit_selector_function: fn(&str) -> Option<i32>) {
    let contents = fs::read_to_string(input_file).expect("Should have been able to read the file");
    let lines = contents.split("\n");

    println!("Supplied calibration document:\n{}", contents);

    let calibration_value: i32 = lines
        .into_iter()
        .map(|line| line.trim())
        .filter(|line| line != &"")
        .map(|line| {
            let length = line.chars().count();
            let first_digit = (0..length)
                .find_map(|i| digit_selector_function(&line[i..]))
                .unwrap();
            let last_digit = (0..length)
                .rev()
                .find_map(|i| digit_selector_function(&line[i..]))
                .unwrap();
            10 * first_digit + last_digit
        })
        .sum();

    println!("Calibration value: {}", calibration_value);
}

fn get_digit_or_spelled_out(line: &str) -> Option<i32> {
    get_digit(line).or_else(|| get_spelled_out_digit(line))
}

fn get_digit(line: &str) -> Option<i32> {
    let c = line.chars().next()?;
    if c.is_digit(10) {
        Some(c.to_string().parse().unwrap())
    } else {
        None
    }
}

fn get_spelled_out_digit(line: &str) -> Option<i32> {
    let spelled_out_digits = vec![
        (0, "zero"),
        (1, "one"),
        (2, "two"),
        (3, "three"),
        (4, "four"),
        (5, "five"),
        (6, "six"),
        (7, "seven"),
        (8, "eight"),
        (9, "nine"),
    ];

    for spelled_out_digit in &spelled_out_digits {
        if line.starts_with(spelled_out_digit.1) {
            return Some(spelled_out_digit.0);
        }
    }
    return None;
}
