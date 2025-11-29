use std::collections::VecDeque;

#[derive(Debug, Copy, Clone)]
enum Direction {
    North,
    South,
    East,
    West,
}
impl Direction {
    fn translate(&self, curr: &Position) -> Position {
        match self {
            Direction::North => Position {
                x: curr.x,
                y: curr.y - 1,
                direction: *self,
                distance_from_origin: curr.distance_from_origin + 1,
            },
            Direction::South => Position {
                x: curr.x,
                y: curr.y + 1,
                direction: *self,
                distance_from_origin: curr.distance_from_origin + 1,
            },
            Direction::East => Position {
                x: curr.x + 1,
                y: curr.y,
                direction: *self,
                distance_from_origin: curr.distance_from_origin + 1,
            },
            Direction::West => Position {
                x: curr.x - 1,
                y: curr.y,
                direction: *self,
                distance_from_origin: curr.distance_from_origin + 1,
            },
        }
    }
    fn translate_point(&self, point: (i32, i32)) -> (i32, i32) {
        match self {
            Direction::North => (point.0, point.1 - 1),
            Direction::South => (point.0, point.1 + 1),
            Direction::East => (point.0 + 1, point.1),
            Direction::West => (point.0 - 1, point.1),
        }
    }

    fn available_directions(&self) -> [Direction; 3] {
        match self {
            Direction::North => [Direction::North, Direction::East, Direction::West],
            Direction::South => [Direction::South, Direction::East, Direction::West],
            Direction::East => [Direction::East, Direction::North, Direction::South],
            Direction::West => [Direction::West, Direction::North, Direction::South],
        }
    }
}

struct Position {
    x: i32,
    y: i32,
    direction: Direction,
    distance_from_origin: i32,
}

impl PartialEq for Position {
    fn eq(&self, other: &Position) -> bool {
        self.x == other.x && self.y == other.y
    }
}

impl Position {
    fn available_steps(&self, garden_map: &Vec<Vec<char>>) -> Vec<Position> {
        let directions = self.direction.available_directions();

        let mut positions: Vec<Position> = Vec::new();

        for direction in directions {
            let new_position = direction.translate_point((self.x, self.y));

            // Bounds check
            if new_position.0 < 0
                || new_position.1 < 0
                || new_position.0 >= garden_map[0].len() as i32
                || new_position.1 >= garden_map.len() as i32
            {
                continue;
            }

            // Check for walls
            if garden_map[new_position.1 as usize][new_position.0 as usize] == '#' {
                continue;
            }

            // Check for visited
            if ['O', ','].contains(&garden_map[new_position.1 as usize][new_position.0 as usize]) {
                continue;
            }

            positions.push(direction.translate(&self));
        }
        positions
    }
}

fn read_garden_map(input_file: &str) -> Vec<Vec<char>> {
    let contents = std::fs::read_to_string(&input_file).expect("could not read file");
    contents
        .trim()
        .lines()
        .map(|l| l.chars().collect::<Vec<char>>())
        .collect()
}

pub fn first_part(input_file: &str) {
    part1_helper(input_file, 64);
}

pub fn part1_helper(input_file: &str, steps: i32) {
    let garden_map = read_garden_map(input_file);
    let mut output_map = garden_map.clone();
    //print_garden_map(&garden_map);

    let Some((x, y)) = find_start(&garden_map) else {
        println!("Unable to find the start location");
        return;
    };

    let mut positions: VecDeque<Position> = VecDeque::new();

    for direction in [
        Direction::North,
        Direction::South,
        Direction::East,
        Direction::West,
    ]
    .iter()
    {
        let position = Position {
            x,
            y,
            direction: *direction,
            distance_from_origin: 0,
        };
        positions.push_back(position);
    }

    let target_distance = steps;

    while !positions.is_empty() {
        let curr = positions.pop_front().unwrap();

        if curr.distance_from_origin % 2 == steps % 2 {
            output_map[curr.y as usize][curr.x as usize] = 'O';
        } else {
            // , stands for visited but odd distance so cannot be reached
            output_map[curr.y as usize][curr.x as usize] = ',';
        }

        if curr.distance_from_origin >= target_distance {
            continue;
        }

        for position in curr.available_steps(&output_map) {
            if !positions.contains(&position) {
                positions.push_back(position);
            }
        }
    }

    //print_garden_map(&output_map);

    let total_positions = output_map
        .iter()
        .map(|row| row.iter().filter(|c| **c == 'O').count())
        .sum::<usize>();

    println!("Total reachable positions: {}", total_positions);
}

fn print_garden_map(garden_map: &Vec<Vec<char>>) {
    for row in garden_map {
        for col in row {
            print!("{}", col);
        }
        println!();
    }
    println!();
}

fn find_start(garden_map: &Vec<Vec<char>>) -> Option<(i32, i32)> {
    for y in 0..garden_map.len() {
        for x in 0..garden_map[0].len() {
            if garden_map[y][x] == 'S' {
                return Some((x as i32, y as i32));
            }
        }
    }
    None
}
pub fn second_part(input_file: &str) {
    part1_helper(input_file, 65);
    part1_helper(input_file, 64);
    part1_helper(input_file, 131);
    part1_helper(input_file, 130);

    // Each diamond gives us 3666 positions, the grid has a huge gap so
    // the diamonds stack nicely. now the first diamond costs us 65 and
    // then for each 130 we add a ring of diamonds around.
    //
    let steps = 26501365;
    println!("Total steps to take {}", steps);

    let odd_diamond_positions = 3752;
    let even_diamond_positions = 3666;
    let odd_rectangle = 7407;
    let even_rectangle = 7481;
    println!(
        "Available positions in each diamond {}",
        even_diamond_positions
    );

    let rings = (steps as i64 - 65) / 131;
    println!("Rings of diamonds added {}", rings);

    let n = rings;

    let o = if n % 2 == 0 {
        (n + 1) * (n + 1)
    } else {
        (n) * (n)
    };

    let e = if n % 2 == 1 {
        (n + 1) * (n + 1)
    } else {
        (n) * (n)
    };

    let m = (2 * n + 1) * (2 * n + 1) - o - e;

    let total_positions = o * odd_diamond_positions
        + e * even_diamond_positions
        + m / 2
            * ((odd_rectangle - odd_diamond_positions) + (even_rectangle - even_diamond_positions))
                as i64;

    // If n is even
    // The number of odd insides is given by 2n + 1
    // The number of even insides is given by 2n
    // The number of mixed pieces is given by
    // If n is odd, the number of even insides is given by 2(n+1)
    // the number of odd insides = 2n - 1

    println!("Total positions: {}", total_positions);
}
