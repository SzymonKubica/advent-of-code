use std::{fmt::Display, fs, io::Empty};

pub fn part1(input_file: &str) {
    let contents = fs::read_to_string(input_file);

    // Grid needs to be mutable because ultimately we need to replace the
    // guard with an empty tile.
    let mut grid = Grid::from(contents.unwrap().as_str());

    let guard: Guard = find_guard(&grid).unwrap();
    replace_guard(&mut grid, guard);

    println!("Grid without the guard:\n{}", grid)
}

fn replace_guard(grid: &mut Grid, guard: Guard) {
    let Position { x, y } = guard.position;
    grid.0[y][x] = Tile::Empty
}

fn find_guard(grid: &Grid) -> Option<Guard> {
    for (y, row) in grid.0.iter().enumerate() {
        for (x, tile) in row.iter().enumerate() {
            if let Tile::Guard = tile {
                return Some(Guard::new(Position { x, y }, Direction::Up));
            }
        }
    }
    None
}
pub fn part2(input_file: &str) {}

struct Grid(Vec<Vec<Tile>>);

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

enum Tile {
    Obstacle,
    Empty,
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
}

struct Position {
    x: usize,
    y: usize,
}

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
        };
        f.write_str(repr)
    }
}
