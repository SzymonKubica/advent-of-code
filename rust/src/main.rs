use std::vec;

extern crate enum_iterator;
extern crate nom;
extern crate num;
extern crate num_integer;
extern crate rand;

extern crate utilities;

mod year_2023;
mod year_2024;

fn main() {
    let selected_year = std::env::args().nth(1).expect("no year selected");
    let selected_day = std::env::args().nth(2).expect("no day selected");
    let selected_puzzle = std::env::args().nth(3).expect("no day selected");
    let input_file = std::env::args().nth(4).expect("no input file given");

    let available_days_2023: Vec<(fn(&str) -> (), fn(&str) -> ())> = vec![
        (year_2023::day1::first_part, year_2023::day1::second_part),
        (year_2023::day2::first_part, year_2023::day2::second_part),
        (year_2023::day3::first_part, year_2023::day3::second_part),
        (year_2023::day4::first_part, year_2023::day4::second_part),
        (year_2023::day5::first_part, year_2023::day5::second_part),
        (year_2023::day6::first_part, year_2023::day6::second_part),
        (year_2023::day7::first_part, year_2023::day7::second_part),
        (year_2023::day8::first_part, year_2023::day8::second_part),
        (year_2023::day9::first_part, year_2023::day9::second_part),
        (year_2023::day10::first_part, year_2023::day10::second_part),
        (year_2023::day11::first_part, year_2023::day11::second_part),
        (year_2023::day12::first_part, year_2023::day12::second_part),
        (year_2023::day13::first_part, year_2023::day13::second_part),
        (year_2023::day14::first_part, year_2023::day14::second_part),
        (year_2023::day15::first_part, year_2023::day15::second_part),
        (year_2023::day16::first_part, year_2023::day16::second_part),
        (year_2023::day17::first_part, year_2023::day17::second_part),
        (year_2023::day18::first_part, year_2023::day18::second_part),
        (year_2023::day19::first_part, year_2023::day19::second_part),
        (year_2023::day20::first_part, year_2023::day20::second_part),
        (year_2023::day21::first_part, year_2023::day21::second_part),
        (year_2023::day22::first_part, year_2023::day22::second_part),
        (year_2023::day23::first_part, year_2023::day23::second_part),
        (year_2023::day24::first_part, year_2023::day24::second_part),
        (year_2023::day25::first_part, year_2023::day25::second_part),
    ];

    let available_days_2024: Vec<(fn(&str) -> (), fn(&str) -> ())> = vec![
        (year_2024::day1::first_part, year_2024::day1::second_part),
        (year_2024::day2::first_part, year_2024::day2::second_part),
        (year_2024::day3::first_part, year_2024::day3::second_part),
        (year_2024::day4::first_part, year_2024::day4::second_part),
        (year_2024::day5::first_part, year_2024::day5::second_part),
        (year_2024::day6::first_part, year_2024::day6::second_part),
        (year_2024::day7::first_part, year_2024::day7::second_part),
        (year_2024::day8::first_part, year_2024::day8::second_part),
        (year_2024::day9::first_part, year_2024::day9::second_part),
        (year_2024::day10::first_part, year_2024::day10::second_part),
        (year_2024::day11::first_part, year_2024::day11::second_part),
        (year_2024::day12::first_part, year_2024::day12::second_part),
        (year_2024::day13::first_part, year_2024::day13::second_part),
    ];

    let Ok(year) = selected_year.parse::<usize>() else {
        panic!("Unable to parse selected year");
    };

    let available_days = if year == 2024 {
        available_days_2024
    } else if year == 2023 {
        available_days_2023
    } else {
        panic!("Unsupported year.");
    };

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
