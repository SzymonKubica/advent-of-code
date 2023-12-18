use std::vec;

extern crate enum_iterator;
extern crate nom;
extern crate num;

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

fn main() {
    let selected_day = std::env::args().nth(1).expect("no day selected");
    let selected_puzzle = std::env::args().nth(2).expect("no day selected");
    let input_file = std::env::args().nth(3).expect("no input file given");

    let available_days: Vec<(fn(&str) -> (), fn(&str) -> ())> = vec![
        (day1::part1, day1::part2),
        (day2::part1, day2::part2),
        (day3::part1, day3::part2),
        (day4::part1, day4::part2),
        (day5::part1, day5::part2),
        (day6::part1, day6::part2),
        (day7::part1, day7::part2),
        (day8::part1, day8::part2),
        (day9::part1, day9::part2),
        (day10::part1, day10::part2),
        (day11::part1, day11::part2),
        (day12::part1, day12::part2),
        (day13::part1, day13::part2),
        (day14::part1, day14::part2),
        (day15::part1, day15::part2),
        (day16::part1, day16::part2),
        (day17::part1, day17::part2),
        (day18::part1, day18::part2),
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
