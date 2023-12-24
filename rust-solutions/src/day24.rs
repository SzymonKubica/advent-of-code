struct Vector {
    x: f32,
    y: f32,
    z: f32,
}

impl From<&str> for Vector {
    fn from(value: &str) -> Self {
        let values = value
            .trim()
            .split(", ")
            .map(|s| s.trim().parse::<f32>().unwrap())
            .collect::<Vec<f32>>();
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
    fn find_intersection(&self, other: &HailStone) -> Option<(f32, f32)> {
        let x_1 = self.position.x;
        let y_1 = self.position.y;

        let x_2 = other.position.x;
        let y_2 = other.position.y;

        let v_1 = self.velocity.x;
        let w_1 = self.velocity.y;

        let v_2 = other.velocity.x;
        let w_2 = other.velocity.y;

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



pub fn part1(input_file: &str) {
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
pub fn part2(input_file: &str) {}
