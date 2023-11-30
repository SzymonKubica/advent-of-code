use std::fs;

pub fn run(input_file: &str) {
    let contents = fs::read_to_string(input_file).expect("Should have been able to read the file");

    println!("{}", contents)
}
