use std::vec;

extern crate enum_iterator;
extern crate nom;
extern crate num;
extern crate rand;

mod day1;
mod day2;
mod day3;
mod day4;
mod day5;
mod day6;
mod day7;
mod day8;
mod day9;
mod day10;
mod day11;
mod day12;
mod day13;
mod day14;
mod day15;
mod day16;
mod day17;
mod day18;
mod day19;
mod day20;
mod day21;
mod day22;
mod day23;
mod day24;
mod day25;

fn main() {
    let selected_day = std::env::args().nth(1).expect("no day selected");
    let selected_puzzle = std::env::args().nth(2).expect("no day selected");
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
        (day14::first_part, day14::second_part),
        (day15::first_part, day15::second_part),
        (day16::first_part, day16::second_part),
        (day17::first_part, day17::second_part),
        (day18::first_part, day18::second_part),
        (day19::first_part, day19::second_part),
        (day20::first_part, day20::second_part),
        (day21::first_part, day21::second_part),
        (day22::first_part, day22::second_part),
        (day23::first_part, day23::second_part),
        (day24::first_part, day24::second_part),
        (day25::first_part, day25::second_part),
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
