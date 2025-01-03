use std::vec;

extern crate enum_iterator;
extern crate nom;
extern crate num;
extern crate num_integer;
extern crate rand;

extern crate utilities;

mod day1;
mod day10;
mod day11;
mod day12;
mod day13;
mod day2;
mod day3;
mod day4;
mod day5;
mod day6;
mod day7;
mod day8;
mod day9;

fn main() {
    let selected_day = std::env::args().nth(1).expect("no day selected");
    let selected_puzzle = std::env::args().nth(2).expect("no puzzle selected");
    let input_file = std::env::args().nth(3).expect("no input file given");

    let available_days: Vec<(fn(&str) -> (), fn(&str) -> ())> = vec![
        (day1::first_part, day1::second_part),
        (day2::first_part, day2::second_part),
        (day3::first_part, day3::second_part),
        (day4::first_part, day4::second_part),
        (day5::first_part, day5::second_part),
        (day6::first_part, day6::second_part),
        (day7::first_part, day7::second_part),
        (day8::first_part, day8::second_part),
        (day9::first_part, day9::second_part),
        (day10::first_part, day10::second_part),
        (day11::first_part, day11::second_part),
        (day12::first_part, day12::second_part),
        (day13::first_part, day13::second_part),
    ];

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
