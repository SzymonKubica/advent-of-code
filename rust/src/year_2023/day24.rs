use std::collections::{HashMap, HashSet};

use num::abs;

#[derive(Debug, Clone, Hash, Eq, PartialEq)]
struct Vector {
    x: i64,
    y: i64,
    z: i64,
}

impl From<&str> for Vector {
    fn from(value: &str) -> Self {
        let values = value
            .trim()
            .split(", ")
            .map(|s| s.trim().parse::<i64>().unwrap())
            .collect::<Vec<i64>>();
        let x = values[0];
        let y = values[1];
        let z = values[2];

        Vector { x, y, z }
    }
}

impl ToString for Vector {
    fn to_string(&self) -> String {
        format!("{}, {}, {}", self.x, self.y, self.z)
    }
}

struct HailStone {
    id: u32,
    position: Vector,
    velocity: Vector,
}

impl PartialEq for HailStone {
    fn eq(&self, other: &Self) -> bool {
        self.id == other.id
    }
}

impl From<&str> for HailStone {
    fn from(value: &str) -> Self {
        let values = value.split(" @ ").collect::<Vec<&str>>();
        let position = Vector::from(values[0]);
        let velocity = Vector::from(values[1]);

        HailStone {
            id: 0,
            position,
            velocity,
        }
    }
}

impl ToString for HailStone {
    fn to_string(&self) -> String {
        format!(
            "{} @ {}",
            self.position.to_string(),
            self.velocity.to_string()
        )
    }
}

impl HailStone {
    fn find_intersection_3(&self, other: &HailStone) -> Option<(i64, i64, i64)> {
        let self_translations: Vec<Vector> = (-1000000..1000000)
            .map(|i| Vector {
                x: self.position.x + i * self.velocity.x,
                y: self.position.y + i * self.velocity.y,
                z: self.position.z + i * self.velocity.z,
            })
            .collect();

        let other_translations: Vec<Vector> = (-1000000..1000000)
            .map(|i| Vector {
                x: other.position.x + i * other.velocity.x,
                y: other.position.y + i * other.velocity.y,
                z: other.position.z + i * other.velocity.z,
            })
            .collect();

        for pos in self_translations {
            if other_translations.contains(&pos) {
                return Some((pos.x, pos.y, pos.z));
            }
        }
        None
    }
    fn find_intersection(&self, other: &HailStone) -> Option<(f32, f32)> {
        let x_1 = self.position.x as f32;
        let y_1 = self.position.y as f32;

        let x_2 = other.position.x as f32;
        let y_2 = other.position.y as f32;

        let v_1 = self.velocity.x as f32;
        let w_1 = self.velocity.y as f32;

        let v_2 = other.velocity.x as f32;
        let w_2 = other.velocity.y as f32;

        let denominator = (v_2 * w_1) - (w_2 * v_1);
        if denominator == 0.0 {
            return None;
        }
        let beta = (v_1 * (y_2 - y_1) - w_1 * (x_2 - x_1)) / (denominator);
        let alpha = ((x_2 - x_1) + beta * v_2) / v_1;

        // We are only interested in intersections in forward direction
        if alpha < 0.0 || beta < 0.0 {
            return None;
        }

        let x_intersect = x_1 + alpha * v_1;
        let y_intersect = y_1 + alpha * w_1;

        Some((x_intersect, y_intersect))
    }
    fn find_intersection_int(&self, other: &HailStone) -> Option<(i64, i64)> {
        let x_1 = self.position.x;
        let y_1 = self.position.y;

        let x_2 = other.position.x;
        let y_2 = other.position.y;

        let v_1 = self.velocity.x;
        let w_1 = self.velocity.y;

        let v_2 = other.velocity.x;
        let w_2 = other.velocity.y;

        let denominator = (v_2 * w_1) - (w_2 * v_1);
        if denominator == 0 {
            return None;
        }
        let beta = (v_1 * (y_2 - y_1) - w_1 * (x_2 - x_1)) / (denominator);
        let alpha = ((x_2 - x_1) + beta * v_2) / v_1;

        // We are only interested in intersections in forward direction
        if alpha < 0 || beta < 0 {
            return None;
        }

        let x_intersect = x_1 + alpha * v_1;
        let y_intersect = y_1 + alpha * w_1;

        Some((x_intersect, y_intersect))
    }
}

fn read_input(input_file: &str) -> Vec<HailStone> {
    let content = std::fs::read_to_string(&input_file).unwrap();
    content
        .trim()
        .split("\n")
        .enumerate()
        .map(|(i, s)| {
            let mut stone = HailStone::from(s);
            stone.id = i as u32;
            stone
        })
        .collect()
}

pub fn first_part(input_file: &str) {
    let hail_stones = read_input(input_file);

    let lower_bound = 200000000000000.0;
    let upper_bound = 400000000000000.0;

    let mut valid_intersections = 0;
    for stone in &hail_stones {
        for other_stone in hail_stones.iter().filter(|s| s != &stone) {
            if let Some((x, y)) = stone.find_intersection(&other_stone) {
                println!("Hailstone A: {}", stone.to_string());
                println!("Hailstone B: {}", other_stone.to_string());
                if lower_bound <= x && x <= upper_bound && lower_bound <= y && y <= upper_bound {
                    println!("Intersection inside : ({}, {})", x, y);
                    valid_intersections += 1;
                } else {
                    println!("Intersection outside : ({}, {})", x, y);
                }
            }
        }
    }

    // The loop above does double counting so we need to divide by two.
    println!(
        "Total forward intersections in range: {}",
        valid_intersections / 2
    );
}
pub fn second_part(input_file: &str) {
    let hail_stones = read_input(input_file);
    // Idea: determine the velocity components in all 3 directions
    // If there are two hailstones with the same x component of the velocity,
    // it means that in the x-direction the distance between them will remain
    // constant. Thus, in order for our rock to hit them both, the difference
    // between its x velocity and the x velocity of those two hailstones
    // needs to evenly divide the distance between those two hailstones.
    //
    let mut possible_x_velocities: HashSet<i64> = HashSet::new();
    let mut possible_y_velocities: HashSet<i64> = HashSet::new();
    let mut possible_z_velocities: HashSet<i64> = HashSet::new();
    for i in -1000..1000 {
        possible_x_velocities.insert(i);
        possible_y_velocities.insert(i);
        possible_z_velocities.insert(i);
    }
    for stone in &hail_stones {
        for other_stone in hail_stones.iter().filter(|s| s != &stone) {
            if stone.velocity.x == other_stone.velocity.x {
                for v in possible_x_velocities.clone() {
                    let distance = abs(stone.position.x as i64 - other_stone.position.x as i64);
                    let delta_v = abs(stone.velocity.x as i64 - v);
                    if delta_v > 0 && distance % delta_v != 0 {
                        possible_x_velocities.remove(&v);
                    }
                }
            }

            if stone.velocity.y == other_stone.velocity.y {
                for v in possible_y_velocities.clone() {
                    let distance = abs(stone.position.y as i64 - other_stone.position.y as i64);
                    let delta_v = abs(stone.velocity.y as i64 - v);
                    if delta_v > 0 && distance % delta_v != 0 {
                        possible_y_velocities.remove(&v);
                    }
                }
            }

            if stone.velocity.z == other_stone.velocity.z {
                for v in possible_z_velocities.clone() {
                    let distance = abs(stone.position.z as i64 - other_stone.position.z as i64);
                    let delta_v = abs(stone.velocity.z as i64 - v);
                    if delta_v > 0 && distance % delta_v != 0 {
                        possible_z_velocities.remove(&v);
                    }
                }
            }
        }
    }

    println!("Possible x velocities: {:?}", possible_x_velocities);
    println!("Possible y velocities: {:?}", possible_y_velocities);
    println!("Possible z velocities: {:?}", possible_z_velocities);

    // Now we have a single velocity vector.
    // We need to find the starting position which can be done
    //
    let rock_velocity = Vector {
        x: possible_x_velocities.into_iter().collect::<Vec<i64>>()[0],
        y: possible_y_velocities.into_iter().collect::<Vec<i64>>()[0],
        z: possible_z_velocities.into_iter().collect::<Vec<i64>>()[0],
    };
    /*
    let rock_velocity = Vector {
        x: -3,
        y: 1,
        z: 2,
    };
    */

    // Now we subtract that vector from velocities of the two other hailstones
    // to get two lines of possible starting points. Their intersection gives
    // us the rock starting location.
    //
    let hailstone1 = &hail_stones[0];
    let hailstone2 = &hail_stones[2];

    let mut velocity1 = hailstone1.velocity.clone();
    velocity1.x -= rock_velocity.x;
    velocity1.y -= rock_velocity.y;
    velocity1.z -= rock_velocity.z;

    let mut velocity2 = hailstone2.velocity.clone();
    velocity2.x -= rock_velocity.x;
    velocity2.y -= rock_velocity.y;
    velocity2.z -= rock_velocity.z;

    let possible_starts_line1 = HailStone {
        id: 0,
        position: hailstone1.position.clone(),
        velocity: velocity1.clone(),
    };
    let possible_starts_line2 = HailStone {
        id: 0,
        position: hailstone2.position.clone(),
        velocity: velocity2.clone(),
    };

    let intersection = possible_starts_line1
        .find_intersection_int(&possible_starts_line2)
        .unwrap();

    let displacement = (hailstone1.position.x - intersection.0) / velocity1.x;
    println!("Displacement {:?}", displacement);
    let z = hailstone1.position.z - velocity1.z * displacement;
    println!(
        "Rock starting position: ({}, {}, {})",
        intersection.0, intersection.1, z
    );

    let displacement = (hailstone2.position.y - intersection.1) / velocity2.y;
    println!("Displacement {:?}", displacement);
    let z = hailstone2.position.z - velocity2.z * displacement;
    println!(
        "Rock starting position: ({}, {}, {})",
        intersection.0, intersection.1, z
    );

    let result = intersection.0 + intersection.1 + z;
    println!("Sum of coordinates: {}", result);
}
