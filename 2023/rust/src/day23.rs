use std::{
    cmp::Ordering,
    collections::{BinaryHeap, HashMap, HashSet, VecDeque},
};

#[derive(PartialEq, Eq, Hash, Copy, Clone)]
enum Tile {
    Path,
    Forest,
    Slope(Direction),
}

impl From<char> for Tile {
    fn from(value: char) -> Self {
        match value {
            '.' => Tile::Path,
            '#' => Tile::Forest,
            '^' => Tile::Slope(Direction::Up),
            'v' => Tile::Slope(Direction::Down),
            '<' => Tile::Slope(Direction::Left),
            '>' => Tile::Slope(Direction::Right),
            _ => panic!("Invalid tile"),
        }
    }
}

impl Tile {
    fn from_no_slopes(value: char) -> Self {
        match value {
            '.' => Tile::Path,
            '#' => Tile::Forest,
            '^' => Tile::Path,
            'v' => Tile::Path,
            '<' => Tile::Path,
            '>' => Tile::Path,
            _ => panic!("Invalid tile"),
        }
    }
}

impl ToString for Tile {
    fn to_string(&self) -> String {
        match self {
            Tile::Path => ".".to_string(),
            Tile::Forest => "#".to_string(),
            Tile::Slope(Direction::Up) => "^".to_string(),
            Tile::Slope(Direction::Down) => "v".to_string(),
            Tile::Slope(Direction::Left) => "<".to_string(),
            Tile::Slope(Direction::Right) => ">".to_string(),
        }
    }
}

#[derive(PartialEq, Eq, Hash, Copy, Clone, Debug)]
enum Direction {
    Up,
    Down,
    Left,
    Right,
}

impl Direction {
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

    fn is_opposite(&self, direction: Direction) -> bool {
        match direction {
            Direction::Up => self == &Direction::Down,
            Direction::Down => self == &Direction::Up,
            Direction::Left => self == &Direction::Right,
            Direction::Right => self == &Direction::Left,
        }
    }
}

#[derive(PartialEq, Eq, Hash, Copy, Clone, Debug)]
struct Position {
    point: Point,
    direction: Direction,
}

impl Position {
    fn get_available_positions(&self, map: &Vec<Vec<Tile>>, visited: &Vec<Point>) -> Vec<Position> {
        let mut positions = vec![];
        let curr_cell = &map[self.point.y as usize][self.point.x as usize];
        for direction in self.available_directions(curr_cell.clone()) {
            let next_point = direction.translate_point(&self.point);
            if next_point.x < 0
                || next_point.x >= map[0].len() as i32
                || next_point.y < 0
                || next_point.y >= map.len() as i32
            {
                continue;
            };

            if visited.contains(&next_point) {
                continue;
            }
            let next_cell = &map[next_point.y as usize][next_point.x as usize];
            match next_cell {
                Tile::Path => {
                    positions.push(Position {
                        point: next_point,
                        direction,
                    });
                }
                Tile::Forest => {}
                Tile::Slope(other_direction) => {
                    if other_direction.is_opposite(direction) {
                        continue;
                    }
                    positions.push(Position {
                        point: next_point,
                        direction,
                    });
                }
            }
        }
        positions
    }

    fn available_directions(&self, current_cell: Tile) -> Vec<Direction> {
        if let Tile::Slope(direction) = current_cell {
            return [direction].to_vec();
        }
        match self.direction {
            Direction::Up => [Direction::Up, Direction::Right, Direction::Left].to_vec(),
            Direction::Down => [Direction::Down, Direction::Right, Direction::Left].to_vec(),
            Direction::Right => [Direction::Right, Direction::Up, Direction::Down].to_vec(),
            Direction::Left => [Direction::Left, Direction::Up, Direction::Down].to_vec(),
        }
    }
}

#[derive(PartialEq, Eq, Hash, Copy, Clone, Debug, Ord, PartialOrd)]
struct Point {
    x: i32,
    y: i32,
}

impl ToString for Point {
    fn to_string(&self) -> String {
        format!("({},{})", self.x, self.y)
    }
}

impl Point {
    fn get_adjacent_points(&self) -> Vec<Point> {
        vec![
            Point {
                x: self.x,
                y: self.y - 1,
            },
            Point {
                x: self.x,
                y: self.y + 1,
            },
            Point {
                x: self.x - 1,
                y: self.y,
            },
            Point {
                x: self.x + 1,
                y: self.y,
            },
        ]
    }
    fn is_inside(&self, map: &Vec<Vec<Tile>>) -> bool {
        0 <= self.x && self.x < map[0].len() as i32 && 0 <= self.y && self.y < map.len() as i32
    }
}

#[derive(PartialEq, Eq, Hash, Clone, Debug)]
struct State {
    pub position: Position,
    tiles_traversed: Vec<Point>,
}

#[derive(Eq, Hash)]
struct HikePath {
    pub location: Point,
    // Cost is usize::MAX - distance as we are trying to maximise the total
    // distane -> minimize the cost
    cost: usize,
    visited_nodes: Vec<Point>,
}

impl PartialEq for HikePath {
    fn eq(&self, other: &Self) -> bool {
        self.cost == other.cost
    }
}

impl Ord for HikePath {
    fn cmp(&self, other: &Self) -> std::cmp::Ordering {
        self.cost
            .cmp(&other.cost)
            .then_with(|| self.location.cmp(&other.location))
    }
}

impl PartialOrd for HikePath {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        Some(self.cmp(other))
    }
}

fn read_map(input_file: &str) -> Vec<Vec<Tile>> {
    let contents = std::fs::read_to_string(input_file).unwrap();

    contents
        .lines()
        .map(|l| l.chars().map(|c| Tile::from(c)).collect::<Vec<Tile>>())
        .collect()
}

fn read_map_no_slopes(input_file: &str) -> Vec<Vec<Tile>> {
    let contents = std::fs::read_to_string(input_file).unwrap();

    contents
        .lines()
        .map(|l| {
            l.chars()
                .map(|c| Tile::from_no_slopes(c))
                .collect::<Vec<Tile>>()
        })
        .collect()
}

fn find_max_hike_length(map: &Vec<Vec<Tile>>) {
    for line in map {
        println!("{}", line.iter().map(|t| t.to_string()).collect::<String>());
    }

    let start = Position {
        point: Point { x: 1, y: 0 },
        direction: Direction::Down,
    };

    let end = Position {
        point: Point {
            x: map[0].len() as i32 - 2,
            y: map.len() as i32 - 1,
        },
        direction: Direction::Down,
    };

    let mut states = VecDeque::new();
    states.push_back(State {
        position: start,
        tiles_traversed: vec![],
    });

    let mut final_states = Vec::new();

    while !states.is_empty() {
        let curr_state = states.pop_front().unwrap();
        if curr_state.position == end {
            final_states.push(curr_state);
            continue;
        }
        for position in curr_state
            .position
            .get_available_positions(&map, &curr_state.tiles_traversed)
        {
            let mut tiles_traversed = curr_state.tiles_traversed.clone();
            tiles_traversed.push(position.point);
            states.push_back(State {
                position,
                tiles_traversed,
            });
        }
    }

    for state in &final_states {
        println!("Tiles traversed: {}", state.tiles_traversed.len());
    }

    // We subtract 1 because the original destination isn't counted
    let max_tiles_traversed = final_states
        .iter()
        .map(|s| s.tiles_traversed.len())
        .max()
        .unwrap();

    println!("Lenght of the longest hike: {}", max_tiles_traversed);
}

fn explore_path(
    curr: &mut State,
    map: &Vec<Vec<Tile>>,
    paths: &mut HashMap<Point, Vec<(Point, usize)>>,
) {
    let start = curr.clone();
    // First we take 1 step in the specified direction by the parent call.

    curr.tiles_traversed.push(curr.position.point);
    curr.position.point = curr
        .position
        .direction
        .translate_point(&curr.position.point);

    let mut tiles_traversed_count = 1;
    let mut positions = curr
        .position
        .get_available_positions(&map, &curr.tiles_traversed);
    while positions.len() == 1 {
        tiles_traversed_count += 1;
        if curr.tiles_traversed.contains(&positions[0].point) {
            return;
        }
        curr.tiles_traversed.push(positions[0].point);
        curr.position = positions[0];
        positions = curr
            .position
            .get_available_positions(&map, &curr.tiles_traversed);
    }
    // Here we have either reached a crossing or there are 0 paths available.
    // If we have reached the end then the exploration is complete.
    let end = Position {
        point: Point {
            x: map[0].len() as i32 - 2,
            y: map.len() as i32 - 1,
        },
        direction: Direction::Down,
    };
    if positions.len() > 1 || curr.position == end {
        // First insert the current node to the map of crossings.
        if !paths.contains_key(&start.position.point) {
            paths.insert(
                start.position.point,
                vec![(curr.position.point, tiles_traversed_count)],
            );
        } else {
            let vec = paths.get_mut(&start.position.point).unwrap();
            if !vec.contains(&(curr.position.point, tiles_traversed_count)) {
                vec.push((curr.position.point, tiles_traversed_count));
            } else {
                return;
            }
        }

        for position in positions {
            let mut new_state = curr.clone();
            new_state.position.direction = position.direction;
            new_state.tiles_traversed.push(position.point);
            explore_path(&mut new_state, map, paths);
        }
    }
}

fn find_max_hike_length_no_slope(map: &Vec<Vec<Tile>>) {
    for line in map {
        println!("{}", line.iter().map(|t| t.to_string()).collect::<String>());
    }

    let start = Position {
        point: Point { x: 1, y: 0 },
        direction: Direction::Down,
    };

    let end = Position {
        point: Point {
            x: map[0].len() as i32 - 2,
            y: map.len() as i32 - 1,
        },
        direction: Direction::Down,
    };

    let mut nodes: HashMap<Point, Vec<(Point, usize)>> = HashMap::new();

    explore_path(
        &mut State {
            position: start,
            tiles_traversed: vec![],
        },
        &map,
        &mut nodes,
    );

    for (k, v) in &nodes {
        println!("{}: {:?}", k.to_string(), v);
    }

    // Make nodes undrected edges
    for (k, v) in &nodes.clone() {
        for (k2, v2) in v {
            if !nodes.contains_key(k2) {
                continue;
            }
            let vec = nodes.get_mut(k2).unwrap();
            if !vec.contains(&(*k, *v2)) {
                vec.push((*k, *v2));
            }
        }
    }
    println!("Undirected nodes");
    for (k, v) in &nodes {
        println!("{}: {:?}", k.to_string(), v);
    }

    // Now we need to do Dijkstra maximising distance.
    let mut hike_paths = BinaryHeap::new();
    hike_paths.push(HikePath {
        location: start.point,
        cost: 0,
        visited_nodes: vec![],
    });
    let mut tentative_distances: HashMap<Point, usize> = HashMap::new();
    tentative_distances.insert(start.point, 0);

    let mut parent_map: HashMap<Point, Point> = HashMap::new();

    println!("End: {:?}", end);
    let mut end_distances = Vec::new();

    while !hike_paths.is_empty() {
        let curr = hike_paths.pop().unwrap();
        /*
        println!("Processing: {:?}", curr.location);
        println!(
            "Tentative distances:\n{}",
            tentative_distances
                .iter()
                .map(|(k, v)| format!("{} {}", k.to_string(), v))
                .collect::<Vec<String>>()
                .join("\n")
        );
        */
        if curr.location == end.point {
            end_distances.push(curr.cost);
        }

        if let Some(next_nodes) = nodes.get(&curr.location) {
            for (location, distance) in next_nodes {
                if curr.visited_nodes.contains(location) {
                    continue;
                }
                let new_cost = curr.cost + distance;
                let mut new_visited_nodes = curr.visited_nodes.clone();
                new_visited_nodes.push(curr.location);

                hike_paths.push(HikePath {
                    location: *location,
                    cost: new_cost,
                    visited_nodes: new_visited_nodes,
                });
                parent_map.insert(curr.location, *location);
            }
        }
    }

    //println!("Possible distances: {:?}", end_distances);

    println!(
        "Maximum distance found: {}",
        end_distances.iter().max().unwrap()
    );
}

pub fn first_part(input_file: &str) {
    let map = read_map(input_file);
    find_max_hike_length(&map);
}
pub fn second_part(input_file: &str) {
    let map = read_map_no_slopes(input_file);
    find_max_hike_length_no_slope(&map);
}
