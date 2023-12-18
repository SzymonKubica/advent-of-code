use std::collections::{BinaryHeap, HashMap};

pub fn part1(input_file: &str) {
    let mut grid = read_grid(input_file);

    println!("\n");

    // Initial two directions
    let mut tentative_distances: HashMap<Node, usize> = HashMap::new();
    let start = Point { x: 0, y: 0 };
    let end = Point {
        x: grid[0].len() as i32 - 1,
        y: grid.len() as i32 - 1,
    };
    tentative_distances.insert(
        Node {
            point: start.clone(),
            direction: Direction::Right,
            direction_count: 0,
        },
        0,
    );
    tentative_distances.insert(
        Node {
            point: start.clone(),
            direction: Direction::Down,
            direction_count: 0,
        },
        0,
    );

    let mut possible_directions: BinaryHeap<State> = BinaryHeap::new();

    possible_directions.push(State {
        cost: 0,
        node: Node {
            point: start.clone(),
            direction: Direction::Right,
            direction_count: 0,
        },
    });

    possible_directions.push(State {
        cost: 0,
        node: Node {
            point: start.clone(),
            direction: Direction::Down,
            direction_count: 0,
        },
    });

    while let Some(State { cost, node }) = possible_directions.pop() {
        if node.point == end {
            println!("Minimum cost to get to the end: {}", cost);
            break;
        }

        for neighbour in get_neighbours(node) {
            if !in_bounds(&neighbour.point, &grid) {
                continue;
            }
            let new_cost = cost + grid[neighbour.point.y as usize][neighbour.point.x as usize];

            if let Some(old_distance) = tentative_distances.get(&neighbour) {
                if new_cost >= *old_distance {
                    continue;
                }
            }

            // if we have found a path with lower cost we add it to the possible
            // directions
            tentative_distances.insert(neighbour.clone(), new_cost);
            possible_directions.push(State {
                cost: new_cost,
                node: neighbour,
            });
        }
    }
}

fn get_neighbours_ultra(node: Node) -> Vec<Node> {
    let mut nodes = vec![];
    for direction in node.direction.get_available_turns() {
        if node.direction != direction {
            if node.direction_count >= 4 {
                nodes.push(Node {
                    point: direction.translate_point(&node.point),
                    direction: direction.clone(),
                    direction_count: 1,
                });
            }
        } else if node.direction_count < 10 {
            nodes.push(Node {
                point: direction.translate_point(&node.point),
                direction: direction.clone(),
                direction_count: node.direction_count + 1,
            });
        }
    }

    nodes
}

fn get_neighbours(node: Node) -> Vec<Node> {
    let mut nodes = vec![];
    for direction in node.direction.get_available_turns() {
        if node.direction != direction {
            nodes.push(Node {
                point: direction.translate_point(&node.point),
                direction: direction.clone(),
                direction_count: 1,
            });
        } else if node.direction_count < 3 {
            nodes.push(Node {
                point: direction.translate_point(&node.point),
                direction: direction.clone(),
                direction_count: node.direction_count + 1,
            });
        }
    }

    nodes
}

fn in_bounds(pos: &Point, grid: &Vec<Vec<usize>>) -> bool {
    0 <= pos.x && pos.x < grid[0].len() as i32 && 0 <= pos.y && pos.y < grid.len() as i32
}

#[derive(Copy, Clone, Eq, PartialEq, Debug, Hash, Ord, PartialOrd)]
enum Direction {
    Up,
    Down,
    Left,
    Right,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Ord, PartialOrd)]
struct Point {
    x: i32,
    y: i32,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Ord, PartialOrd)]
struct Node {
    point: Point,
    direction: Direction,
    direction_count: usize,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
struct State {
    cost: usize,
    node: Node,
}

impl Ord for State {
    fn cmp(&self, other: &Self) -> std::cmp::Ordering {
        // We are using a min heap, so we are doing this backwards.
        other
            .cost
            .cmp(&self.cost)
            .then_with(|| self.node.cmp(&other.node))
    }
}

impl PartialOrd for State {
    fn partial_cmp(&self, other: &Self) -> Option<std::cmp::Ordering> {
        Some(self.cmp(other))
    }
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

    fn translate_point(&self, point: &Point) -> Point {
        match self {
            Direction::Up => Point {
                x: point.x,
                y: point.y - 1,
            },
            Direction::Down => Point {
                x: point.x,
                y: point.y + 1,
            },
            Direction::Left => Point {
                x: point.x - 1,
                y: point.y,
            },
            Direction::Right => Point {
                x: point.x + 1,
                y: point.y,
            },
        }
    }

    fn rotate_clockwise(&self) -> Self {
        match self {
            Direction::Up => Direction::Right,
            Direction::Down => Direction::Left,
            Direction::Left => Direction::Up,
            Direction::Right => Direction::Down,
        }
    }

    fn rotate_counterclockwise(&self) -> Self {
        match self {
            Direction::Up => Direction::Left,
            Direction::Down => Direction::Right,
            Direction::Left => Direction::Down,
            Direction::Right => Direction::Up,
        }
    }

    fn get_available_turns(&self) -> Vec<Direction> {
        match self {
            Direction::Up => vec![Direction::Left, Direction::Right, Direction::Up],
            Direction::Down => vec![Direction::Left, Direction::Right, Direction::Down],
            Direction::Left => vec![Direction::Left, Direction::Up, Direction::Down],
            Direction::Right => vec![Direction::Up, Direction::Right, Direction::Down],
        }
    }
}

fn read_grid(input_file: &str) -> Vec<Vec<usize>> {
    let contents = std::fs::read_to_string(input_file).expect("Unable to read the file.");
    contents
        .trim()
        .split("\n")
        .map(|l| {
            l.chars()
                .map(|c| c.to_string().parse::<usize>().unwrap())
                .collect()
        })
        .collect()
}

pub fn part2(input_file: &str) {
    let mut grid = read_grid(input_file);

    println!("\n");

    // Initial two directions
    let mut tentative_distances: HashMap<Node, usize> = HashMap::new();
    let start = Point { x: 0, y: 0 };
    let end = Point {
        x: grid[0].len() as i32 - 1,
        y: grid.len() as i32 - 1,
    };
    tentative_distances.insert(
        Node {
            point: start.clone(),
            direction: Direction::Right,
            direction_count: 0,
        },
        0,
    );
    tentative_distances.insert(
        Node {
            point: start.clone(),
            direction: Direction::Down,
            direction_count: 0,
        },
        0,
    );

    let mut possible_directions: BinaryHeap<State> = BinaryHeap::new();

    possible_directions.push(State {
        cost: 0,
        node: Node {
            point: start.clone(),
            direction: Direction::Right,
            direction_count: 0,
        },
    });

    possible_directions.push(State {
        cost: 0,
        node: Node {
            point: start.clone(),
            direction: Direction::Down,
            direction_count: 0,
        },
    });

    while let Some(State { cost, node }) = possible_directions.pop() {
        if node.point == end {
            println!("Minimum cost to get to the end: {}", cost);
            break;
        }

        for neighbour in get_neighbours_ultra(node) {
            if !in_bounds(&neighbour.point, &grid) {
                continue;
            }
            let new_cost = cost + grid[neighbour.point.y as usize][neighbour.point.x as usize];

            if let Some(old_distance) = tentative_distances.get(&neighbour) {
                if new_cost >= *old_distance {
                    continue;
                }
            }

            // if we have found a path with lower cost we add it to the possible
            // directions
            tentative_distances.insert(neighbour.clone(), new_cost);
            possible_directions.push(State {
                cost: new_cost,
                node: neighbour,
            });
        }
    }
}
