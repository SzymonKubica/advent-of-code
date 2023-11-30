use std::vec;

mod day1;

fn main() {
    let selected_day = std::env::args().nth(1).expect("no day selected");
    let input_file = std::env::args().nth(2).expect("no input file given");

    let available_days = vec![day1::run];

    if let Ok(day) = selected_day.parse::<usize>() {
        if day <= available_days.len() {
            available_days[day - 1](&input_file);
        }
    }

}
