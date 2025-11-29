use std::{collections::HashSet, fmt::Display, fs, os::unix::thread, thread::sleep};

use crate::utilities::Point;

pub fn first_part(input_file: &str) {
    let mut aggregate_trailhead_score: u32 = 0;
    let input = fs::read_to_string(input_file).unwrap();
    let topographic_map = read_topographic_map(&input);
    print_map(&topographic_map);
    for (y, row) in topographic_map.iter().enumerate() {
        for (x, loc) in row.iter().enumerate() {
            if loc.is_trailhead() {
                println!("Found trailhead at: ({}, {})", x, y);
                let mut visited = HashSet::new();
                let trailhead = Point::new(x as i32, y as i32);
                visited.insert(trailhead.clone());
                let peaks = find_unique_peaks(
                    trailhead,
                    &topographic_map,
                    visited
                );
                aggregate_trailhead_score += peaks.len() as u32;
            }
        }
    }
    println!("Total trailhead score: {}", aggregate_trailhead_score);
}

pub fn second_part(input_file: &str) {
    let mut aggregate_trailhead_score: u32 = 0;
    let input = fs::read_to_string(input_file).unwrap();
    let topographic_map = read_topographic_map(&input);
    print_map(&topographic_map);
    for (y, row) in topographic_map.iter().enumerate() {
        for (x, loc) in row.iter().enumerate() {
            if loc.is_trailhead() {
                println!("Found trailhead at: ({}, {})", x, y);
                let mut visited = HashSet::new();
                let trailhead = Point::new(x as i32, y as i32);
                visited.insert(trailhead.clone());
                let sub_rating = traverse_trailhead(
                    trailhead,
                    &topographic_map,
                    visited
                );
                aggregate_trailhead_score += sub_rating;
            }
        }
    }
    println!("Total trailhead score: {}", aggregate_trailhead_score);

}

fn print_map(topographic_map: &Vec<Vec<MountainLocation>>) {
    println!("Topographic map:");
    println!(
        "{}",
        topographic_map
            .iter()
            .map(|l| l
                .iter()
                .map(|loc| format!("{}", loc))
                .collect::<Vec<String>>()
                .join(""))
            .collect::<Vec<String>>()
            .join("\n")
    );
}

fn traverse_trailhead(
    point: Point,
    topographic_map: &Vec<Vec<MountainLocation>>,
    visited: HashSet<Point>,
) -> u32 {
    if topographic_map[point.y()][point.x()].is_peak() {
        return 1;
    }

    let mut output: u32 = 0;
    for neighbour in point.get_neighbours() {
        if neighbour.is_within_grid(topographic_map) && !visited.contains(&neighbour) && is_uphill_slope(&point, &neighbour, topographic_map) {
            //println!("Checking neighbour: {:?}", neighbour);
            //println!("Visited: {:?}", visited);
            let mut visited_neighbour: HashSet<Point> = HashSet::new();
            visited_neighbour.insert(neighbour.clone());
            let new_visited: HashSet<Point> = visited
                .union(&visited_neighbour)
                .collect::<Vec<&Point>>()
                .iter()
                .map(|p| (*p).clone())
                .collect();
            output += traverse_trailhead(neighbour, topographic_map, new_visited)
        }
    }
    return output;
}

fn find_unique_peaks(
    point: Point,
    topographic_map: &Vec<Vec<MountainLocation>>,
    visited: HashSet<Point>,
) -> HashSet<Point> {
    if topographic_map[point.y()][point.x()].is_peak() {
        return vec![point].into_iter().collect::<HashSet<Point>>()
    }

    let mut peaks_found = HashSet::new();
    for neighbour in point.get_neighbours() {
        if neighbour.is_within_grid(topographic_map) && !visited.contains(&neighbour) && is_uphill_slope(&point, &neighbour, topographic_map) {
            //println!("Checking neighbour: {:?}", neighbour);
            //println!("Visited: {:?}", visited);
            let mut visited_neighbour: HashSet<Point> = HashSet::new();
            visited_neighbour.insert(neighbour.clone());
            let new_visited: HashSet<Point> = visited
                .union(&visited_neighbour)
                .collect::<Vec<&Point>>()
                .iter()
                .map(|p| (*p).clone())
                .collect();
            for peak in find_unique_peaks(neighbour, topographic_map, new_visited) {
                peaks_found.insert(peak);
            }
        }
    }
    return peaks_found;
}

fn is_uphill_slope(point: &Point, neighbour: &Point, topographic_map: &Vec<Vec<MountainLocation>>) -> bool {
    topographic_map[neighbour.y()][neighbour.x()].0 == topographic_map[point.y()][point.x()].0 + 1
}


struct MountainLocation(u32);

fn read_topographic_map(input: &str) -> Vec<Vec<MountainLocation>> {
    input
        .lines()
        .map(|l| {
            l.chars()
                .filter(|c| c.is_digit(10))
                .map(|c| c.to_digit(10))
                .map(Option::unwrap)
                .map(MountainLocation::new)
                .collect::<Vec<MountainLocation>>()
        })
        .collect()
}

impl MountainLocation {
    fn new(elevation: u32) -> Self {
        MountainLocation(elevation)
    }

    pub fn is_trailhead(&self) -> bool {
        self.0 == 0
    }

    pub fn is_peak(&self) -> bool {
        self.0 == 9
    }
}

impl Display for MountainLocation {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.0)
    }
}
