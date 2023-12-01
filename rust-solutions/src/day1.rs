use std::fs;

// This function takes in a list of lines, all of which containing at least one
// digit. It is supposed to return the sum of the double digit numbers (calibration values)
// created by sticking together the first and last digits on each line.
pub fn part1(input_file: &str) {
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

pub fn part2(input_file: &str) {
    let contents = fs::read_to_string(input_file).expect("Should have been able to read the file");
    let lines = contents.split("\n");

    println!("Supplied calibration document:\n{}", contents);

    let calibration_value: i32 = lines
        .into_iter()
        .map(|line| get_calibration_value_with_spelled_out_digits(line))
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

fn get_calibration_value_with_spelled_out_digits(line: &str) -> Result<i32, String> {
    let clean_line = line.trim();
    if clean_line == "" {
        return Err("Empty line".to_string());
    }


    let first_digit = find_digit_front(clean_line);
    let last_digit = find_digit_back(clean_line);

    let mut calibration_value = String::from("");
    calibration_value.push(char::from_digit(first_digit.try_into().unwrap(), 10).unwrap());
    calibration_value.push(char::from_digit(last_digit.try_into().unwrap(), 10).unwrap());

    Ok(calibration_value.parse().unwrap())
}

fn find_digit_front(line: &str) -> i32 {
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

    for i in 0..line.len() {
        let current_char = line[..=i]
            .char_indices()
            .filter(|index| index.0 == i)
            .map(|index| index.1)
            .collect::<Vec<char>>()[0];

        if current_char.is_digit(10) {
            return current_char.to_string().parse().unwrap();
        }
        for spelled_out_digit in &spelled_out_digits {
            if line[i..].starts_with(spelled_out_digit.1) {
                return spelled_out_digit.0;
            }
        }
    }
    return 0;
}


fn find_digit_back(line: &str) -> i32 {
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

    for i in (0..line.len()).rev() {
        let current_char = line
            .char_indices()
            .filter(|index| index.0 == i)
            .map(|index| index.1)
            .collect::<Vec<char>>()[0];

        if current_char.is_digit(10) {
            return current_char.to_string().parse().unwrap();
        }
        for spelled_out_digit in &spelled_out_digits {
            if line[..i].ends_with(spelled_out_digit.1) {
                return spelled_out_digit.0;
            }
        }
    }
    return 0;
}
