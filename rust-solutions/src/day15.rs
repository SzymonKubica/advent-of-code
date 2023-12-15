use std::fs;

fn read_steps(input_file: &str) -> Vec<String> {
    let contents = fs::read_to_string(input_file).expect("Unable to read the file");
    contents.trim().split(",").map(|s| s.to_string()).collect()
}

fn hash_string(string: &str) -> u32 {
    let mut value: u32 = 0;

    for c in string.chars() {
        value += c as u32;
        value *= 17;
        value %= 256;
    }
    value
}
pub fn part1(input_file: &str) {
    let steps = read_steps(input_file);

    let sum: u32 = steps.iter().map(|s| hash_string(s)).sum();

    println!("Verification sum {}", sum);
}
pub fn part2(input_file: &str) {}
