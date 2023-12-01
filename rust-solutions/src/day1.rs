use std::fs;

// This function takes in a list of lines, all of which containing at least one
// digit. It is supposed to return the sum of the double digit numbers (calibration values)
// created by sticking together the first and last digits on each line.
pub fn run(input_file: &str) {
    let contents = fs::read_to_string(input_file).expect("Should have been able to read the file");
    let lines = contents.split("\n");

    println!("Supplied calibration document:\n{}", contents);

    let calibration_value: i32 = lines
        .into_iter()
        .map(|line| get_calibration_value(line))
        .filter_map(Result::ok)
        .sum();

    println!("Calibration value: {}", calibration_value)
}

fn get_calibration_value(line: &str) -> Result<i32, String> {
    let clean_line = line.trim();
    if clean_line == "" {
        return Err("Empty line".to_string());
    }
    let digits = clean_line
        .chars()
        .into_iter()
        .filter(|c| c.is_digit(10))
        .collect::<Vec<char>>();

    let first_digit = digits.first().unwrap();
    let last_digit = digits.last().unwrap();

    let mut calibration_value = String::from("");
    calibration_value.push(first_digit.clone());
    calibration_value.push(last_digit.clone());

    Ok(calibration_value.parse().unwrap())
}
