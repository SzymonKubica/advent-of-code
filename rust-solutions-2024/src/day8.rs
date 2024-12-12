use std::{collections::HashMap, fmt::Display, fs};

pub fn part1(input_file: &str) {
    let input = fs::read_to_string(input_file).unwrap();
    let mut map = parse_grid(&input);

    println!("Input grid:");
    print_grid(&map);

    let mut frequency_to_antennas: HashMap<char, Vec<&Location>> = HashMap::new();

    let grid = map.clone();
    for row in grid.iter() {
        for location in row {
            if let Location::Antenna(c, _pos) = location {
                if frequency_to_antennas.contains_key(c) {
                    frequency_to_antennas.get_mut(c).unwrap().push(location)
                } else {
                    frequency_to_antennas.insert(*c, vec![location]);
                }
            }
        }
    }

    for antennas in frequency_to_antennas.values() {
        create_antinodes(antennas, &mut map);
    }

    println!("Grid after tracing antinodes:");
    print_grid(&map);

    let antinodes_number: usize = map
        .iter()
        .map(|r| {
            r.iter()
                .filter(|l| {
                    return if let Location::Antinode(_p) = l {
                        true
                    } else {
                        false
                    };
                })
                .count()
        })
        .sum();

    println!("Unique antinodes: {}", antinodes_number);
}
pub fn part2(input_file: &str) {
    let input = fs::read_to_string(input_file).unwrap();
    let mut map = parse_grid(&input);

    println!("Input grid:");
    print_grid(&map);

    let mut frequency_to_antennas: HashMap<char, Vec<&Location>> = HashMap::new();

    let grid = map.clone();
    for row in grid.iter() {
        for location in row {
            if let Location::Antenna(c, _pos) = location {
                if frequency_to_antennas.contains_key(c) {
                    frequency_to_antennas.get_mut(c).unwrap().push(location)
                } else {
                    frequency_to_antennas.insert(*c, vec![location]);
                }
            }
        }
    }

    for antennas in frequency_to_antennas.values() {
        create_antinodes_part2(antennas, &mut map);
    }

    println!("Grid after tracing antinodes:");
    print_grid(&map);

    let antinodes_number: usize = map
        .iter()
        .map(|r| {
            r.iter()
                .filter(|l| {
                    return if let Location::Antinode(_p) = l {
                        true
                    } else {
                        false
                    };
                })
                .count()
        })
        .sum();

    println!("Unique antinodes: {}", antinodes_number);
}

fn create_antinodes(antennas: &Vec<&Location>, map: &mut Vec<Vec<Location>>) {
    for (i, a1) in antennas.iter().enumerate() {
        for (j, a2) in antennas.iter().enumerate() {
            if i != j {
                let antinodes: Vec<Position> = calculate_antinode_positions(a1, a2);
                //println!("Antinode positions: {:?}", &antinodes);
                for a in antinodes {
                    if is_within_bounds(map, a) {
                        map[a.y as usize][a.x as usize] = Location::Antinode(a)
                    }
                }
            }
        }
    }
}

fn create_antinodes_part2(antennas: &Vec<&Location>, map: &mut Vec<Vec<Location>>) {
    for (i, a1) in antennas.iter().enumerate() {
        for (j, a2) in antennas.iter().enumerate() {
            if i != j {
                let antinodes: Vec<Position> = calculate_antinode_positions_part2(a1, a2, map);
                //println!("Antinode positions: {:?}", &antinodes);
                for a in antinodes {
                    if is_within_bounds(map, a) {
                        map[a.y as usize][a.x as usize] = Location::Antinode(a)
                    }
                }
            }
        }
    }
}

fn is_within_bounds(map: &Vec<Vec<Location>>, a: Position) -> bool {
    if a.x < 0 || a.y < 0 {
        return false;
    }

    (a.x as usize) < map[0].len() && (a.y as usize) < map.len()
}

fn calculate_antinode_positions(a1: &Location, a2: &Location) -> Vec<Position> {
    let Location::Antenna(_c, pos1) = a1 else {
        return vec![];
    };

    let Location::Antenna(_c, pos2) = a2 else {
        return vec![];
    };

    let displacement: Position = Position::new(pos1.x - pos2.x, pos1.y - pos2.y);

    let antinode1: Position = Position::new(pos2.x - displacement.x, pos2.y - displacement.y);
    let antinode2: Position = Position::new(pos1.x + displacement.x, pos1.y + displacement.y);

    vec![antinode1, antinode2]
}

fn calculate_antinode_positions_part2(a1: &Location, a2: &Location, map: &mut Vec<Vec<Location>>) -> Vec<Position> {
    let Location::Antenna(_c, pos1) = a1 else {
        return vec![];
    };

    let Location::Antenna(_c, pos2) = a2 else {
        return vec![];
    };

    let x_displacement = pos1.x - pos2.x;
    let y_displacement = pos1.y - pos2.y;
    let gcd = num_integer::gcd(x_displacement, y_displacement);
    let displacement: Position = Position::new((pos1.x - pos2.x)/gcd, (pos1.y - pos2.y)/gcd);

    let antinode1: Position = Position::new(pos2.x - displacement.x, pos2.y - displacement.y);
    let antinode2: Position = Position::new(pos1.x + displacement.x, pos1.y + displacement.y);

    let mut positions = vec![];

    let mut curr_position = Position::new(pos1.x, pos1.y);

    while is_within_bounds(map, curr_position) {
        positions.push(curr_position);
        curr_position = curr_position.add_displacement(displacement);
    }

    let mut curr_position = Position::new(pos1.x, pos1.y).subtract_displacement(displacement);
    while is_within_bounds(map, curr_position) {
        positions.push(curr_position);
        curr_position = curr_position.add_displacement(displacement);
    }

    positions
}

pub fn print_grid(grid: &Vec<Vec<Location>>) {
    println!(
        "{}",
        grid.iter()
            .map(|r| r.iter().map(|l| format!("{}", l)).collect::<String>())
            .collect::<Vec<String>>()
            .join("\n")
    )
}

fn parse_grid(input: &str) -> Vec<Vec<Location>> {
    let char_grid: Vec<Vec<char>> = input.lines().map(|l| l.chars().collect()).collect();

    return char_grid
        .iter()
        .enumerate()
        .map(|(y, row)| {
            row.iter()
                .enumerate()
                .map(|(x, c)| Location::from((c, x, y)))
                .collect()
        })
        .collect();
}

#[derive(Debug, Copy, Clone)]
enum Location {
    Antenna(char, Position),
    Empty,
    Antinode(Position),
}

impl Display for Location {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.write_str(
            match self {
                Location::Antenna(c, _) => format!("{}", c),
                Location::Empty => ".".to_string(),
                Location::Antinode(_) => "#".to_string(),
            }
            .as_str(),
        )
    }
}

impl From<(&char, usize, usize)> for Location {
    fn from(value: (&char, usize, usize)) -> Self {
        match *value.0 {
            '.' => Location::Empty,
            c => Location::Antenna(c, Position::new(value.1 as i32, value.2 as i32)),
        }
    }
}

#[derive(Debug, Copy, Clone)]
struct Position {
    x: i32,
    y: i32,
}

impl Position {
    fn new(x: i32, y: i32) -> Self {
        Self { x, y }
    }

    pub fn add_displacement(&self, displacement: Position) -> Position {
        return Position::new(self.x + displacement.x, self.y + displacement.y);
    }

    pub fn subtract_displacement(&self, displacement: Position) -> Position {
        return Position::new(self.x - displacement.x, self.y - displacement.y);
    }
}
