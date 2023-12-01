use std::vec;

mod day1;

fn main() {
    let selected_day = std::env::args().nth(1).expect("no day selected");
    let selected_puzzle = std::env::args().nth(2).expect("no day selected");
    let input_file = std::env::args().nth(3).expect("no input file given");

    let available_days = vec![(day1::part1, day1::part2)];

    if let Ok(day) = selected_day.parse::<usize>() {
        if day <= available_days.len() {
            if let Ok(1) = selected_puzzle.parse::<usize>() {
                available_days[day - 1].0(&input_file);
            } else {
                available_days[day - 1].1(&input_file);
            }
        }
    }
}
