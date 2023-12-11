use std::fs;

use num::abs;

fn read_galaxy_map(input_file: &str) -> Vec<Vec<char>> {
    fs::read_to_string(&input_file)
        .expect("Unable to read the file")
        .lines()
        .map(|l| l.chars().collect::<Vec<char>>())
        .collect::<Vec<Vec<char>>>()
}

fn show_galaxy_map(map: &Vec<Vec<char>>) -> String {
    map.iter()
        .map(|r| r.iter().collect::<String>())
        .collect::<Vec<String>>()
        .join("\n")
}
pub fn part1(input_file: &str) {
    let galaxy_map = read_galaxy_map(input_file);

    println!("Galaxy map: \n{}", show_galaxy_map(&galaxy_map));

    let expanded_galaxy_map = expand_galaxy_map(galaxy_map);
    println!(
        "Expanded galaxy map: \n{}",
        show_galaxy_map(&expanded_galaxy_map)
    );

    let galaxies = expanded_galaxy_map
        .iter()
        .enumerate()
        .map(|(y, x)| {
            x.iter()
                .enumerate()
                .filter_map(|(i, c)| if c == &'#' { Some((i, y)) } else { None })
                .collect::<Vec<(usize, usize)>>()
        })
        .flatten()
        .collect::<Vec<(usize, usize)>>();

    println!("Galaxies found: {:?}", galaxies);

    let sum_of_distances: i32 = galaxies
        .iter()
        .enumerate()
        .map(|(i, g)| {
            galaxies
                .iter()
                .enumerate()
                .filter_map(|(j, g2)| {
                    if i != j {
                        Some(get_distance(g, g2))
                    } else {
                        None
                    }
                })
                .sum::<i32>()
        })
        .sum::<i32>()
        / 2;

    println!("Sum of distances: {}", sum_of_distances);
}

fn get_distance(p1: &(usize, usize), p2: &(usize, usize)) -> i32 {
    let x1: i32 = p1.0.try_into().unwrap();
    let x2: i32 = p2.0.try_into().unwrap();
    let y1: i32 = p1.1.try_into().unwrap();
    let y2: i32 = p2.1.try_into().unwrap();
    return abs(x1 - x2) + abs(y1 - y2);
}

fn expand_galaxy_map(galaxy_map: Vec<Vec<char>>) -> Vec<Vec<char>> {
    let mut expanded_galaxy_map = galaxy_map.clone();

    let empty_rows = galaxy_map
        .iter()
        .enumerate()
        .filter_map(|(i, r)| {
            if r.iter().all(|c| c == &'.') {
                Some(i)
            } else {
                None
            }
        })
        .collect::<Vec<usize>>();

    let mut empty_columns: Vec<usize> = vec![];

    for i in 0..galaxy_map[0].len() {
        if galaxy_map.iter().all(|r| r.get(i) == Some(&'.')) {
            empty_columns.push(i);
        }
    }

    for (i, index) in empty_columns.iter().enumerate() {
        expanded_galaxy_map
            .iter_mut()
            .for_each(|r| r.insert(i + index, '.'))
    }

    for (i, j) in empty_rows.iter().enumerate() {
        // We need to keep track of how many we have already inserted as the
        // array shifts downwards after each insertion
        expanded_galaxy_map.insert(j + i, expanded_galaxy_map[j + i].clone());
    }

    expanded_galaxy_map
}
pub fn part2(input_file: &str) {}
