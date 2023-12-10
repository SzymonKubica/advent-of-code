use std::fs;

use num::abs;

#[derive(PartialEq, Eq, Debug)]
enum PipeSegment {
    Vertical,
    Horizontal,
    NorthEast,
    NorthWest,
    SouthEast,
    SouthWest,
    Ground,
    StartingPoint,
}

#[derive(PartialEq, Eq, Debug)]
enum Direction {
    Right,
    Down,
    Left,
    Up,
}

impl PipeSegment {
    pub fn allows_down(&self) -> bool {
        [
            &PipeSegment::Vertical,
            &PipeSegment::NorthEast,
            &PipeSegment::NorthWest,
        ]
        .contains(&self)
    }
    pub fn allows_up(&self) -> bool {
        [
            &PipeSegment::Vertical,
            &PipeSegment::SouthWest,
            &PipeSegment::SouthEast,
        ]
        .contains(&self)
    }
    pub fn allows_left(&self) -> bool {
        [
            &PipeSegment::Horizontal,
            &PipeSegment::NorthEast,
            &PipeSegment::SouthEast,
        ]
        .contains(&self)
    }
    pub fn allows_right(&self) -> bool {
        [
            &PipeSegment::Horizontal,
            &PipeSegment::NorthWest,
            &PipeSegment::SouthWest,
        ]
        .contains(&self)
    }
    pub fn travel_up(&self, x: usize, y: usize) -> ((usize, usize), Direction) {
        match &self {
            PipeSegment::Vertical => ((x, y - 1), Direction::Up),
            PipeSegment::SouthEast => ((x, y - 1), Direction::Right),
            PipeSegment::SouthWest => ((x, y - 1), Direction::Left),
            _ => panic!("Cannot travel up through {:?} pipe", self),
        }
    }
    pub fn travel_down(&self, x: usize, y: usize) -> ((usize, usize), Direction) {
        match &self {
            PipeSegment::Vertical => ((x, y + 1), Direction::Down),
            PipeSegment::NorthEast => ((x, y + 1), Direction::Right),
            PipeSegment::NorthWest => ((x, y + 1), Direction::Left),
            _ => panic!("Cannot travel down through {:?} pipe", self),
        }
    }
    pub fn travel_left(&self, x: usize, y: usize) -> ((usize, usize), Direction) {
        match &self {
            PipeSegment::Horizontal => ((x - 1, y), Direction::Left),
            PipeSegment::NorthEast => ((x - 1, y), Direction::Up),
            PipeSegment::SouthEast => ((x - 1, y), Direction::Down),
            _ => panic!("Cannot travel left through {:?} pipe", self),
        }
    }

    pub fn travel_right(&self, x: usize, y: usize) -> ((usize, usize), Direction) {
        match &self {
            PipeSegment::Horizontal => ((x + 1, y), Direction::Right),
            PipeSegment::NorthWest => ((x + 1, y), Direction::Up),
            PipeSegment::SouthWest => ((x + 1, y), Direction::Down),
            _ => panic!("Cannot travel left through {:?} pipe", self),
        }
    }
}

impl From<char> for PipeSegment {
    fn from(value: char) -> Self {
        match value {
            '|' => PipeSegment::Vertical,
            '-' => PipeSegment::Horizontal,
            'L' => PipeSegment::NorthEast,
            'J' => PipeSegment::NorthWest,
            '7' => PipeSegment::SouthWest,
            'F' => PipeSegment::SouthEast,
            '.' => PipeSegment::Ground,
            'S' => PipeSegment::StartingPoint,
            _ => panic!("Unknown pipe segment: {}", value),
        }
    }
}

fn get_pipes(input_file: &str) -> Vec<Vec<PipeSegment>> {
    let contents = fs::read_to_string(&input_file).expect("Unable to read the file");

    contents
        .lines()
        .map(|line| line.chars().map(|c| PipeSegment::from(c)).collect())
        .collect()
}
pub fn part1(input_file: &str) {
    let pipe_map = get_pipes(input_file);

    // Find the starting point
    let mut position: (usize, usize) = (0, 0);
    // We don't know the direction yet
    let mut direction;

    println!("Pipe map: {:?}", pipe_map);
    for (i, row) in pipe_map.iter().enumerate() {
        if row.contains(&PipeSegment::StartingPoint) {
            position = (
                row.iter()
                    .position(|s| s == &PipeSegment::StartingPoint)
                    .unwrap(),
                i,
            );
            break;
        }
    }

    println!("Starting position: {:?}", position);
    // Find the entrance to the loop
    // for part 2 we mark the loop segments
    let height = pipe_map.len();
    let width = pipe_map[0].len();
    let mut inside_outside_map: Vec<Vec<char>> = vec![];

    for _ in 0..(2 * height - 1) {
        let mut row: Vec<char> = vec![];
        for _ in 0..(2 * width - 1) {
            row.push('.');
        }
        inside_outside_map.push(row);
    }

    // # denotes the loop
    set_entry(&position, &mut inside_outside_map);
    let right = &pipe_map[position.1][position.0 + 1];
    let down = &pipe_map[position.1 + 1][position.0];
    let left = if position.0 > 0 { &pipe_map[position.1][position.0 - 1] } else { &PipeSegment::Ground };
    let up = if position.1 > 0 { &pipe_map[position.1 - 1][position.0] } else { &PipeSegment::Ground };

    if right.allows_right() {
        (position, direction) = right.travel_right(position.0, position.1);
    } else if down.allows_down() {
        (position, direction) = down.travel_down(position.0, position.1);
    } else if left.allows_left() {
        (position, direction) = left.travel_left(position.0, position.1);
    } else {
        (position, direction) = up.travel_up(position.0, position.1);
    }

    set_entry(&position, &mut inside_outside_map);

    // Now we have the next position in the pipe so we can just follow it
    // and assume that everything will be connected properly.

    // We have already taken 1 step
    let mut steps = 1;
    let mut next = get_adjacent_position(&position, &direction);


    while &pipe_map[next.1][next.0] != &PipeSegment::StartingPoint {
        println!("Current position: {:?}", position);
        println!("Current direction: {:?}", direction);
        println!("Next position: {:?}", next);
        println!("-------");
        let next_pipe = &pipe_map[next.1][next.0];
        let temp = next.clone();
        // Interpolate with the current direction
        match direction {
            Direction::Right => set_entry_exact(&(2*position.0 + 1, 2*position.1), &mut inside_outside_map),
            Direction::Down => set_entry_exact(&(2*position.0, 2*position.1 + 1), &mut inside_outside_map),
            Direction::Left => set_entry_exact(&(2*position.0 - 1, 2*position.1), &mut inside_outside_map),
            Direction::Up => set_entry_exact(&(2*position.0, 2*position.1 - 1), &mut inside_outside_map),

        }
        (position, direction) = match direction {
            Direction::Right => next_pipe.travel_right(position.0, position.1),
            Direction::Down => next_pipe.travel_down(position.0, position.1),
            Direction::Left => next_pipe.travel_left(position.0, position.1),
            Direction::Up => next_pipe.travel_up(position.0, position.1),
        };
        next = get_adjacent_position(&position, &direction);
        set_entry(&temp, &mut inside_outside_map);
        position = temp;


        steps += 1;
    }

    println!("The loop takes a total of {} steps.", steps);
    println!("The farthest path along the loop is {} steps long.", (steps + 1)/2);
    println!("Inside outside map");
    print_in_out(&inside_outside_map);

}

fn set_entry(position: &(usize, usize), in_out_map: &mut Vec<Vec<char>>) {
    in_out_map[position.1 * 2][position.0 * 2] = '#';
}

fn set_entry_exact(position: &(usize, usize), in_out_map: &mut Vec<Vec<char>>) {
    in_out_map[position.1][position.0] = '#';
}

fn print_in_out(in_out_map: &Vec<Vec<char>>) {
    for row in in_out_map {
        println!("{}", row.iter().collect::<String>());
    }
}


fn get_adjacent_position(position: &(usize, usize), direction: &Direction) -> (usize, usize) {
    match direction {
        Direction::Right => (position.0 + 1, position.1),
        Direction::Down => (position.0, position.1 + 1),
        Direction::Left => (position.0 - 1, position.1),
        Direction::Up => (position.0, position.1 - 1),
    }
}

pub fn part2(input_file: &str) {}
