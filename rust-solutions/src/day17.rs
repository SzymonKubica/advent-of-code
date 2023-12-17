use priority_queue::PriorityQueue;

pub fn part1(input_file: &str) {
    let mut grid = read_grid(input_file);
    let mut grid_copy = grid.clone();

    print_grid(&grid);
    println!("\n");

    let height = grid.len();
    let width = grid[0].len();
    grid[0][0].tentative_distance = 0;

    let mut queue: PriorityQueue<((usize, usize), usize, Direction), u32> = PriorityQueue::new();

    queue.push(((0, 0), 0, Direction::Right), u32::max_value() - 0);
    queue.push(((0, 0), 0, Direction::Up), u32::max_value() - 0);

    'outer: while queue.is_empty() == false {
        let ((position, same_direction_count, direction), priority) = queue.pop().unwrap();

        for turn_direction in direction.available_turns() {
            let new_position = turn_direction.translate_point(position);
            if new_position.0 >= width as usize || new_position.1 >= height as usize {
                continue;
            }
            if turn_direction == direction && same_direction_count < 3 {
                let mut next = &mut grid[new_position.1 as usize][new_position.0 as usize];
                next.previous = position;
                queue.push(
                    (new_position, same_direction_count + 1, direction),
                    priority - next.heat_loss,
                );
            } else if turn_direction == direction {
                continue;
            } else {
                let next = &mut grid[new_position.1 as usize][new_position.0 as usize];
                next.previous = position;
                queue.push((new_position, 0, turn_direction), priority - next.heat_loss);
            }

            if new_position.0 == width as usize - 1 && new_position.1 == height as usize - 1 {
                let next = &mut grid[new_position.1 as usize][new_position.0 as usize];
                println!("Found the end!");
                println!("Total Heat loss: {:?}", u32::max_value() - priority - next.heat_loss);
                break 'outer;
            }
        }
    }

    /*
    let mut grid_copy = grid.clone();
    let mut prev = (width - 1, height - 1);
    while prev != (0, 0) {
        grid_copy[prev.1][prev.0].visited = true;
        prev = grid[prev.1][prev.0].previous;
        print_grid(&grid_copy);
    }

    grid_copy[prev.1][prev.0].visited = true;
    */
}

#[derive(Clone, Eq, PartialEq, Debug, Hash)]
struct Node {
    heat_loss: u32,
    visited: bool,
    tentative_distance: u32,
    previous: (usize, usize),
    last_steps: Vec<Direction>,
}

#[derive(Copy, Clone, Eq, PartialEq, Debug, Hash)]
enum Direction {
    Up,
    Down,
    Left,
    Right,
}
impl Direction {
    fn is_opposite(&self, other: Direction) -> bool {
        match self {
            Direction::Up => other == Direction::Down,
            Direction::Down => other == Direction::Up,
            Direction::Left => other == Direction::Right,
            Direction::Right => other == Direction::Left,
        }
    }

    fn available_turns(&self) -> Vec<Direction> {
        match self {
            Direction::Up => vec![Direction::Up, Direction::Left, Direction::Right],
            Direction::Down => vec![Direction::Down, Direction::Left, Direction::Right],
            Direction::Left => vec![Direction::Up, Direction::Down, Direction::Left],
            Direction::Right => vec![Direction::Up, Direction::Down, Direction::Right],
        }
    }
    fn translate_point(&self, point: (usize, usize)) -> (usize, usize) {
        match self {
            Direction::Up => (point.0, if point.1 > 0 { point.1 - 1 } else { 0 }),
            Direction::Down => (point.0, point.1 + 1),
            Direction::Left => (if point.0 > 0 { point.0 - 1 } else { 0 }, point.1),
            Direction::Right => (point.0 + 1, point.1),
        }
    }
}

impl ToString for Node {
    fn to_string(&self) -> String {
        if self.visited {
            "_".to_string()
        } else {
            self.heat_loss.to_string()
        }
    }
}

fn print_grid(grid: &Vec<Vec<Node>>) {
    for row in grid {
        println!(
            "{}",
            row.iter()
                .map(|e| e.to_string())
                .collect::<Vec<String>>()
                .join("")
        );
    }
}

fn read_grid(input_file: &str) -> Vec<Vec<Node>> {
    let contents = std::fs::read_to_string(input_file).expect("Unable to read the file.");
    contents
        .trim()
        .split("\n")
        .map(|l| {
            l.chars()
                .map(|c| c.to_string().parse::<u32>().unwrap())
                .map(|x| Node {
                    heat_loss: x,
                    visited: false,
                    tentative_distance: u32::max_value(),
                    previous: (0, 0),
                    last_steps: vec![],
                })
                .collect()
        })
        .collect()
}

pub fn part2(input_file: &str) {}
