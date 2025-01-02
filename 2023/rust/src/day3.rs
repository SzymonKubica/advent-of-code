use std::{cmp::min, collections::HashMap, fs};

fn get_engine_schematic(input_file: &str) -> Vec<Vec<char>> {
    let lines = fs::read_to_string(&input_file).expect("Should have been able to open a file");

    lines
        .split("\n")
        .filter(|s| !s.is_empty())
        .into_iter()
        .map(|s| s.chars().collect::<Vec<char>>())
        .collect()
}

pub fn part1(input_file: &str) {
    let engine_schematic = get_engine_schematic(input_file);
    // The schematic is rectangular so the row length is uniform for all rows.
    let row_length: usize = engine_schematic[0].len();

    let mut part_numbers: Vec<i32> = vec![];
    for j in 0..engine_schematic.len() {
        let mut i = 0;
        // Iterate over the numbers in the row and see if they are part numbers.
        while i < row_length {
            if !engine_schematic[j][i].is_digit(10) {
                i += 1;
                continue;
            }

            // Once we have found a digit, we have a number and we need to check
            // all of its digits.
            let begin = i;
            let mut part_number_found = false;

            while i < row_length && engine_schematic[j][i].is_digit(10) {
                // Stops checking when we have found that any of the digits touches
                // a symbol
                if !part_number_found {
                    part_number_found = has_symbol_in_neighbourhood(i, j, &engine_schematic);
                }
                i += 1;
            }

            let end = i;

            if part_number_found {
                part_numbers.push(
                    engine_schematic[j][begin..end]
                        .into_iter()
                        .collect::<String>()
                        .parse::<i32>()
                        .unwrap(),
                )
            }
            i += 1;
        }
    }

    let part_number_sum: i32 = part_numbers.iter().sum();
    println!("Part numbers found: {:?}", part_numbers);
    println!("Sum of part numbers: {}", part_number_sum);
}

#[derive(Copy, Clone, Eq, Hash, PartialEq, Debug)]
struct Part {
    pub x: usize,
    pub y: usize,
    pub symbol: char,
}

pub fn part2(input_file: &str) {
    let engine_schematic = get_engine_schematic(input_file);
    // The schematic is rectangular so the row length is uniform for all rows.
    let row_length: usize = engine_schematic[0].len();

    let mut part_numbers: Vec<i32> = vec![];
    let mut parts_map: HashMap<Part, Vec<i32>> = HashMap::new();
    for j in 0..engine_schematic.len() {
        let mut i = 0;
        // Iterate over the numbers in the row and see if they are part numbers.
        while i < row_length {
            if !engine_schematic[j][i].is_digit(10) {
                i += 1;
                continue;
            }

            // Once we have found a digit, we have a number and we need to check
            // all of its digits.
            let begin = i;
            let mut part_number_found = false;
            let mut part: Part = Part {
                x: 0,
                y: 0,
                symbol: '.',
            };

            while i < row_length && engine_schematic[j][i].is_digit(10) {
                // Stops checking when we have found that any of the digits touches
                // a symbol
                if !part_number_found {
                    if let Some(p) = find_symbol_in_neighbourhood(i, j, &engine_schematic) {
                        part_number_found = true;
                        part = p;
                    }
                }
                i += 1;
            }

            let end = i;

            if part_number_found {
                let part_number = engine_schematic[j][begin..end]
                    .into_iter()
                    .collect::<String>()
                    .parse::<i32>()
                    .unwrap();
                part_numbers.push(part_number);
                if parts_map.contains_key(&part) {
                    parts_map.get_mut(&part).unwrap().push(part_number);
                } else {
                    parts_map.insert(part, vec![part_number]);
                }
            }
            i += 1;
        }
    }

    // A gear is a '*' adjacent to exactly two digits
    let sum_of_gear_ratios: i32 = parts_map
        .iter()
        .filter(|e| e.0.symbol == '*' && e.1.len() == 2)
        .map(|e| e.1[0] * e.1[1])
        .sum();

    println!("Part numbers found: {:?}", part_numbers);
    println!("Part numbers map: {:?}", parts_map);
    println!("Sum of gear ratios: {}", sum_of_gear_ratios);
}

fn find_symbol_in_neighbourhood(
    x: usize,
    y: usize,
    engine_schematic: &Vec<Vec<char>>,
) -> Option<Part> {
    let lower_x_bound = if x == 0 { x } else { x - 1 };
    let lower_y_bound = if y == 0 { y } else { y - 1 };

    // The schematic is rectangular so the x bound is uniform for all rows.
    let upper_x_bound = min(x + 2, engine_schematic[0].len());
    let upper_y_bound = min(y + 2, engine_schematic.len());

    for j in lower_y_bound..upper_y_bound {
        for i in lower_x_bound..upper_x_bound {
            // Avoid checking (x,y) as we know it is a digit.
            if i == x && j == y {
                continue;
            }
            if is_symbol(engine_schematic[j][i]) {
                return Some(Part {
                    x: i,
                    y: j,
                    symbol: engine_schematic[j][i],
                });
            }
        }
    }
    return None;
}

// Checks if the neighbourhood of point (x,y) contains a symbol.
fn has_symbol_in_neighbourhood(x: usize, y: usize, engine_schematic: &Vec<Vec<char>>) -> bool {
    let lower_x_bound = if x == 0 { x } else { x - 1 };
    let lower_y_bound = if y == 0 { y } else { y - 1 };

    // The schematic is rectangular so the x bound is uniform for all rows.
    let upper_x_bound = min(x + 2, engine_schematic[0].len());
    let upper_y_bound = min(y + 2, engine_schematic.len());

    for j in lower_y_bound..upper_y_bound {
        for i in lower_x_bound..upper_x_bound {
            // Avoid checking (x,y) as we know it is a digit.
            if i == x && j == y {
                continue;
            }
            if is_symbol(engine_schematic[j][i]) {
                return true;
            }
        }
    }
    false
}

fn is_symbol(c: char) -> bool {
    c != '.' && !c.is_digit(10)
}
