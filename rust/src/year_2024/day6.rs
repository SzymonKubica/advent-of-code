use std::{fmt::Display, fs, io::Empty};

pub fn first_part(input_file: &str) {
    let contents = fs::read_to_string(input_file);

    // Grid needs to be mutable because ultimately we need to replace the
    // guard with an empty tile.
    let mut grid = Grid::from(contents.unwrap().as_str());

    let mut guard: Guard = find_guard(&grid).unwrap();
    replace_guard(&mut grid, &guard);

    println!("Grid without the guard:\n{}", &grid);
    predict_guard_path(&mut guard, &mut grid);
    println!("Grid without after guard traversal: {}", &grid);

    let visited_nodes = grid
        .0
        .iter()
        .map(|r| r.iter().filter(|t| **t == Tile::Visited).count())
        .sum::<usize>();
    println!("Total visited nodes: {}", visited_nodes);
}

fn predict_guard_path(guard: &mut Guard, grid: &mut Grid) {
    while is_within_bounds(&grid, &guard.position) {
        grid.mark_visited(&guard.position);
        guard.take_step(&grid);
    }
}

fn predict_guard_path_count_crossing(guard: &mut Guard, grid: &mut Grid) -> usize {
    let mut intersections = 0;
    while is_within_bounds(&grid, &guard.position) {
        if grid.is_visited(&guard.position) {
            intersections += 1;
        }
        grid.mark_visited(&guard.position);
        guard.take_step(&grid);
    }
    intersections
}

fn is_within_bounds(grid: &Grid, position: &Position) -> bool {
    (position.y as usize) < grid.0.len() && (position.x as usize) < grid.0[0].len()
}

fn is_obstacle(grid: &Grid, position: &Position) -> bool {
    grid.0[position.y as usize][position.x as usize] == Tile::Obstacle
}

fn replace_guard(grid: &mut Grid, guard: &Guard) {
    let Position { x, y } = guard.position;
    grid.0[y as usize][x as usize] = Tile::Empty
}

fn find_guard(grid: &Grid) -> Option<Guard> {
    for (y, row) in grid.0.iter().enumerate() {
        for (x, tile) in row.iter().enumerate() {
            if let Tile::Guard = tile {
                return Some(Guard::new(
                    Position {
                        x: x as i32,
                        y: y as i32,
                    },
                    Direction::Up,
                ));
            }
        }
    }
    None
}
pub fn second_part(input_file: &str) {
    let contents = fs::read_to_string(input_file);

    // Grid needs to be mutable because ultimately we need to replace the
    // guard with an empty tile.
    let mut grid = Grid::from(contents.unwrap().as_str());

    let mut guard: Guard = find_guard(&grid).unwrap();
    replace_guard(&mut grid, &guard);

    println!("Grid without the guard:\n{}", &grid);
    let possible_locations = predict_guard_path_count_crossing(&mut guard, &mut grid);
    println!("Grid without after guard traversal: {}", &grid);

    println!(
        "Total possible locations to create a cycle: {}",
        possible_locations
    );
}

struct Grid(Vec<Vec<Tile>>);
impl Grid {
    fn mark_visited(&mut self, position: &Position) {
        self.0[position.y as usize][position.x as usize] = Tile::Visited;
    }

    fn is_visited(&self, position: &Position) -> bool {
        self.0[position.y as usize][position.x as usize] == Tile::Visited
    }
}

impl Display for Grid {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.write_str(
            &self
                .0
                .iter()
                .map(|v| v.into_iter().map(|t| format!("{}", t)).collect::<String>())
                .collect::<Vec<String>>()
                .join("\n"),
        )
    }
}

impl From<&str> for Grid {
    fn from(value: &str) -> Self {
        let grid = value
            .lines()
            .map(|line| line.chars().map(Tile::from).collect())
            .collect();
        Grid(grid)
    }
}

#[derive(Eq, PartialEq)]
enum Tile {
    Obstacle,
    Empty,
    Visited,
    /// Initial location of the guard on the grid
    Guard,
}

struct Guard {
    position: Position,
    direction: Direction,
}

impl Guard {
    pub fn new(position: Position, direction: Direction) -> Self {
        Self {
            position,
            direction,
        }
    }

    fn take_step(&mut self, grid: &&mut Grid) {
        let new_position = self.position.translate(self.direction);
        // If not in bounds, we allow the guard to step outside of the grid
        if !is_within_bounds(grid, &new_position) || !is_obstacle(grid, &new_position) {
            self.position = new_position;
        } else {
            self.turn_right();
            self.take_step(grid)
        }
    }

    fn turn_right(&mut self) {
        self.direction = match self.direction {
            Direction::Up => Direction::Right,
            Direction::Down => Direction::Left,
            Direction::Left => Direction::Up,
            Direction::Right => Direction::Down,
        }
    }
}

struct Position {
    x: i32,
    y: i32,
}
impl Position {
    fn new(x: i32, y: i32) -> Self {
        Self { x, y }
    }

    fn translate(&self, direction: Direction) -> Position {
        match direction {
            Direction::Up => Position::new(self.x, self.y - 1),
            Direction::Down => Position::new(self.x, self.y + 1),
            Direction::Left => Position::new(self.x - 1, self.y),
            Direction::Right => Position::new(self.x + 1, self.y),
        }
    }
}

#[derive(Copy, Clone)]
enum Direction {
    Up,
    Down,
    Left,
    Right,
}

impl From<char> for Tile {
    fn from(c: char) -> Self {
        match c {
            '#' => Tile::Obstacle,
            '.' => Tile::Empty,
            '^' => Tile::Guard,
            _ => unreachable!(),
        }
    }
}

impl Display for Tile {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        let repr = match self {
            Tile::Obstacle => "#",
            Tile::Empty => ".",
            Tile::Guard => "^",
            Tile::Visited => "X",
        };
        f.write_str(repr)
    }
}
