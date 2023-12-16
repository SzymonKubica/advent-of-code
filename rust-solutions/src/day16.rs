use std::fs;

#[derive(Copy, Clone, Eq, PartialEq, Debug)]
enum Direction {
    Up,
    Down,
    Left,
    Right,
}

#[derive(Eq, PartialEq, Clone, Debug)]
enum GridElement {
    Empty,
    LeftMirror,
    RightMirror,
    HorizontalSplitter,
    VerticalSplitter,
}

#[derive(Clone, Eq, PartialEq, Debug)]
struct GridCell {
    pub element: GridElement,
    pub is_energised: bool,
}

#[derive(Copy, Clone, Eq, PartialEq)]
struct Ray {
    pub x_pos: i32,
    pub y_pos: i32,
    pub direction: Direction,
}

impl Ray {
    pub fn is_within_bounds(&self, grid: &Vec<Vec<GridCell>>) -> bool {
        0 <= self.x_pos
            && self.x_pos < grid[0].len() as i32
            && 0 <= self.y_pos
            && self.y_pos < grid.len() as i32
    }

    pub fn take_step(&mut self) {
        match self.direction {
            Direction::Up => self.y_pos -= 1,
            Direction::Down => self.y_pos += 1,
            Direction::Left => self.x_pos -= 1,
            Direction::Right => self.x_pos += 1,
        }
    }

    fn reflect_from(&mut self, mirror: GridElement) {
        match mirror {
            GridElement::LeftMirror => match self.direction {
                Direction::Up => self.direction = Direction::Left,
                Direction::Down => self.direction = Direction::Right,
                Direction::Left => self.direction = Direction::Up,
                Direction::Right => self.direction = Direction::Down,
            },
            GridElement::RightMirror => match self.direction {
                Direction::Up => self.direction = Direction::Right,
                Direction::Down => self.direction = Direction::Left,
                Direction::Left => self.direction = Direction::Down,
                Direction::Right => self.direction = Direction::Up,
            },
            _ => panic!("Can only reflect from mirrors"),
        }
    }
}

impl From<char> for GridElement {
    fn from(value: char) -> Self {
        match value {
            '.' => GridElement::Empty,
            '\\' => GridElement::LeftMirror,
            '/' => GridElement::RightMirror,
            '-' => GridElement::HorizontalSplitter,
            '|' => GridElement::VerticalSplitter,
            _ => panic!("{} cannot be parsed as a valid grid element.", value),
        }
    }
}

impl ToString for GridElement {
    fn to_string(&self) -> String {
        match self {
            GridElement::Empty => ".".to_string(),
            GridElement::LeftMirror => "\\".to_string(),
            GridElement::RightMirror => "/".to_string(),
            GridElement::HorizontalSplitter => "-".to_string(),
            GridElement::VerticalSplitter => "|".to_string(),
        }
    }
}

impl ToString for GridCell {
    fn to_string(&self) -> String {
        if self.is_energised {
            "#".to_string()
        } else {
            self.element.to_string()
        }
    }
}

fn read_grid(input_file: &str) -> Vec<Vec<GridCell>> {
    let contents = fs::read_to_string(input_file).expect("Unable to read the file.");

    contents
        .trim()
        .split("\n")
        .map(|l| {
            l.chars()
                .map(|c| GridCell {
                    element: GridElement::from(c),
                    is_energised: false,
                })
                .collect::<Vec<GridCell>>()
        })
        .collect()
}

fn trace_ray_through_grid(ray: &mut Ray, grid: &mut Vec<Vec<GridCell>>, history: &mut Vec<Ray>) {
    while ray.is_within_bounds(&grid) && !history.contains(ray) {
        grid[ray.y_pos as usize][ray.x_pos as usize].is_energised = true;
        history.push(ray.clone());
        let current_element = &grid[ray.y_pos as usize][ray.x_pos as usize].element;
        match current_element {
            GridElement::Empty => ray.take_step(),
            GridElement::LeftMirror => {
                ray.reflect_from(GridElement::LeftMirror);
                ray.take_step()
            }
            GridElement::RightMirror => {
                ray.reflect_from(GridElement::RightMirror);
                ray.take_step()
            }
            GridElement::HorizontalSplitter => {
                // Here we'll split recursively or do nothing if we go though the
                // pointy end
                match ray.direction {
                    Direction::Left | Direction::Right => ray.take_step(), // pointy ends
                    Direction::Up | Direction::Down => {
                        // Change the direction of the current ray and spawn the
                        // new one
                        ray.direction = Direction::Left;
                        let mut split_ray = ray.clone();
                        split_ray.direction = Direction::Right;
                        ray.take_step();
                        split_ray.take_step();
                        trace_ray_through_grid(&mut split_ray, grid, history);
                    }
                }
            }
            GridElement::VerticalSplitter => {
                // Same for the vertical splitter
                match ray.direction {
                    Direction::Up | Direction::Down => ray.take_step(), // pointy ends
                    Direction::Left | Direction::Right => {
                        ray.direction = Direction::Up;
                        let mut split_ray = ray.clone();
                        split_ray.direction = Direction::Down;
                        ray.take_step();
                        split_ray.take_step();
                        trace_ray_through_grid(&mut split_ray, grid, history);
                    }
                }
            }
        }
    }
}

pub fn part1(input_file: &str) {
    let mut grid = read_grid(input_file);

    for row in &grid {
        println!(
            "{}\n",
            row.iter()
                .map(|e| e.to_string())
                .collect::<Vec<String>>()
                .join("")
        );
    }

    let mut initial_ray = Ray {
        x_pos: 0,
        y_pos: 0,
        direction: Direction::Right,
    };

    let mut history: Vec<Ray> = vec![];
    trace_ray_through_grid(&mut initial_ray, &mut grid, &mut history);

    for row in &grid {
        println!(
            "{}",
            row.iter()
                .map(|e| e.to_string())
                .collect::<Vec<String>>()
                .join("")
        );
    }

    let energised_tiles_count = grid
        .iter()
        .map(|r| r.iter().filter(|c| c.is_energised).count())
        .sum::<usize>();

    println!("Energised tiles: {}", energised_tiles_count);
}

pub fn part2(input_file: &str) {
    let grid = read_grid(input_file);

    let mut initial_configurations: Vec<Ray> = vec![];

    for i in 0..grid.len() {
        initial_configurations.push(Ray {
            x_pos: 0,
            y_pos: i as i32,
            direction: Direction::Right,
        });
        initial_configurations.push(Ray {
            x_pos: i as i32,
            y_pos: 0,
            direction: Direction::Down,
        });
        initial_configurations.push(Ray {
            x_pos: (grid[0].len() - 1) as i32,
            y_pos: i as i32,
            direction: Direction::Left,
        });
        initial_configurations.push(Ray {
            x_pos: i as i32,
            y_pos: (grid.len() - 1) as i32,
            direction: Direction::Up,
        });
    }
    let mut energised_counts: Vec<usize> = vec![];

    for ray in initial_configurations.iter_mut() {
        let mut history: Vec<Ray> = vec![];
        let mut grid_copy = grid.clone();
        trace_ray_through_grid(ray, &mut grid_copy, &mut history);

        for row in &grid_copy {
            println!(
                "{}",
                row.iter()
                    .map(|e| e.to_string())
                    .collect::<Vec<String>>()
                    .join("")
            );
        }

        let energised_tiles_count = grid_copy
            .iter()
            .map(|r| r.iter().filter(|c| c.is_energised).count())
            .sum::<usize>();

        println!(
            "Starting at ({}, {}) direction: {:?} Energised tiles: {}",
            ray.x_pos, ray.y_pos, ray.direction, energised_tiles_count
        );
        energised_counts.push(energised_tiles_count)
    }

    println!("{}", energised_counts.iter().max().unwrap());
}
