use std::{
    cmp::Ordering,
    collections::{HashMap, HashSet},
};

#[derive(Eq, Hash, Clone, Copy)]
struct Brick {
    id: usize,
    start: Point,
    end: Point,
}
impl Brick {
    fn get_cubes(&self) -> Vec<Point> {
        if self.start.x != self.end.x {
            (self.start.x..=self.end.x)
                .map(|x| Point {
                    x,
                    y: self.start.y,
                    z: self.start.z,
                })
                .collect()
        } else if self.start.y != self.end.y {
            (self.start.y..=self.end.y)
                .map(|y| Point {
                    x: self.start.x,
                    y,
                    z: self.start.z,
                })
                .collect()
        } else {
            (self.start.z..=self.end.z)
                .map(|z| Point {
                    x: self.start.x,
                    y: self.start.y,
                    z,
                })
                .collect()
        }
    }
}

impl From<&str> for Brick {
    fn from(value: &str) -> Self {
        let points = value
            .split("~")
            .map(|s| Point::from(s))
            .collect::<Vec<Point>>();
        let start = points[0];
        let end = points[1];
        Brick { id: 0, start, end }
    }
}

// We need to order bricks by their z coordinate to determine which ones are
// going to fall first
impl Ord for Brick {
    fn cmp(&self, other: &Self) -> Ordering {
        self.start.z.cmp(&other.start.z)
    }
}

impl PartialOrd for Brick {
    fn partial_cmp(&self, other: &Self) -> Option<std::cmp::Ordering> {
        self.start.z.partial_cmp(&other.start.z)
    }
}

impl PartialEq for Brick {
    fn eq(&self, other: &Self) -> bool {
        self.start.z == other.start.z
    }
}

impl ToString for Brick {
    fn to_string(&self) -> String {
        format!("{}~{}", self.start.to_string(), self.end.to_string())
    }
}

#[derive(Copy, Clone, Eq, PartialEq, Hash)]
struct Point {
    x: usize,
    y: usize,
    z: usize,
}

impl From<&str> for Point {
    // Example point : 1,2,3
    fn from(value: &str) -> Self {
        let coordinates = value
            .split(",")
            .map(|s| s.parse::<usize>().unwrap())
            .collect::<Vec<usize>>();

        let x = coordinates[0];
        let y = coordinates[1];
        let z = coordinates[2];

        Point { x, y, z }
    }
}

impl ToString for Point {
    fn to_string(&self) -> String {
        format!("{},{},{}", self.x, self.y, self.z)
    }
}

fn read_brick_snapshot(input_file: &str) -> Vec<Brick> {
    std::fs::read_to_string(input_file)
        .unwrap()
        .lines()
        .enumerate()
        .map(|(i, s)| {
            let mut b = Brick::from(s);
            b.id = i;
            b
        })
        .collect()
}

fn find_support_sets(bricks: &mut Vec<Brick>) -> HashMap<usize, HashSet<usize>> {
    // Here usize is the brick id
    let mut occupancy_grid: Vec<Vec<Vec<Option<usize>>>> = Vec::new();

    let x_max = bricks.iter().map(|b| b.end.x).max().unwrap() as usize;
    let y_max = bricks.iter().map(|b| b.end.y).max().unwrap() as usize;
    let z_max = bricks.iter().map(|b| b.end.z).max().unwrap() as usize;

    for _ in 0..=z_max {
        let mut z_slice = Vec::new();
        for _ in 0..=y_max {
            let mut y_slice = Vec::new();
            for _ in 0..=x_max {
                y_slice.push(None);
            }
            z_slice.push(y_slice);
        }
        occupancy_grid.push(z_slice);
    }

    // Idea: make the bricks fall and determine the mapping from brick to the
    // set of bricks that it is supporting. Then for each brick see if there is
    // any brick that is in its support sets that isn't contained in any other
    // support set. If so, the brick can't be disintegrated.
    //
    let mut support_sets: HashMap<usize, HashSet<usize>> = HashMap::new();

    for brick in &*bricks {
        support_sets.insert(brick.id, HashSet::new());
    }

    for brick in &mut *bricks {
        // Try to move each brick as far down as possible
        loop {
            // 1 means resting on the floor -> can' go further
            if brick.start.z == 1 {
                break;
            }
            // Check the cells below the brick to see if they are occupied
            let mut can_fall = true;
            for Point { x, y, z } in brick.get_cubes() {
                if let Some(other_brick_id) = occupancy_grid[z - 1][y][x] {
                    if other_brick_id != brick.id {
                        can_fall = false;
                        support_sets
                            .get_mut(&other_brick_id)
                            .unwrap()
                            .insert(brick.id);
                    }
                }
            }

            if can_fall {
                // Move the brick down
                brick.start.z -= 1;
                brick.end.z -= 1;
            } else {
                break;
            }
        }
        for Point { x, y, z } in brick.get_cubes() {
            occupancy_grid[z][y][x] = Some(brick.id);
        }
    }

    println!("After falling: ");
    for brick in &*bricks {
        println!("{}: {}", brick.id, brick.to_string());
    }

    support_sets
}

pub fn first_part(input_file: &str) {
    let mut bricks = read_brick_snapshot(input_file);
    for brick in &bricks {
        println!("{}", brick.to_string());
    }
    println!();

    bricks.sort();
    for brick in &bricks {
        println!("{}", brick.to_string());
    }

    let support_sets = find_support_sets(&mut bricks);

    // Now we have the support sets for each brick. We need to find the bricks
    // that can be disintegrated i.e. their support sets are contained in other
    // support sets
    let mut disintegrable_bricks_count = 0;
    for (brick, support_set) in &support_sets {
        if support_set.iter().all(|b| {
            support_sets
                .iter()
                .filter(|(b2, _s)| b2 != &brick)
                .map(|(_b2, s)| s)
                .any(|s| s.contains(b))
        }) {
            disintegrable_bricks_count += 1;
        }
    }
    println!("{} can be disintegrated", disintegrable_bricks_count);
}

// Idea: make the bricks fall and determine the mapping from brick to the
// set of bricks that it is supporting. Then for each brick see if there is
// any brick that is in its support sets that isn't contained in any other
// support set. If so, the brick can't be disintegrated.

pub fn second_part(input_file: &str) {
    let mut bricks = read_brick_snapshot(input_file);
    for brick in &bricks {
        println!("{}", brick.to_string());
    }
    println!();

    bricks.sort();
    for brick in &bricks {
        println!("{}", brick.to_string());
    }

    let support_sets = find_support_sets(&mut bricks);
    let mut disintegrable_bricks = Vec::new();

    // The number of bricks that would
    for (brick, support_set) in &support_sets {
        if support_set.iter().all(|b| {
            support_sets
                .iter()
                .filter(|(b2, _s)| b2 != &brick)
                .map(|(_b2, s)| s)
                .any(|s| s.contains(b))
        }) {
            disintegrable_bricks.push(brick);
        }
    }

    println!("Bricks to disintegrate: {:?}", disintegrable_bricks);

    let mut total_falling = 0;
    for (brick, _support_set) in &support_sets {
        if disintegrable_bricks.contains(&&brick) {
            continue;
        }

        let mut all_falling_bricks = HashSet::new();
        find_all_supported(*brick, &support_sets, &mut all_falling_bricks);
        //println!("Processing: {}", brick);
        //println!("Other bricks: {}", all_falling_bricks.len());
        total_falling += all_falling_bricks.len();
    }

    println!("Total falling bricks: {}", total_falling);
}

fn find_all_supported(
    brick: usize,
    support_sets: &HashMap<usize, HashSet<usize>>,
    acc: &mut HashSet<usize>,
) {
    for b in support_sets.get(&brick).unwrap() {
        // if b is supported by something else it might not fall, if that thing
        // is not in the fallen set. We need to add this check:
        if support_sets
            .iter()
            .filter(|(b2, s)| b2 != &&brick)
            .any(|(b2, s)| s.contains(&b) && !acc.contains(&b2))
        {
            continue;
        }
        acc.insert(*b);
        find_all_supported(*b, support_sets, acc);
    }
}
