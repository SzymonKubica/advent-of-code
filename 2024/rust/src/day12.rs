use std::{collections::HashSet, fmt::Display, fs};

use crate::utilities::Point;

pub fn part1(input_file: &str) {
    let input = fs::read_to_string(input_file).unwrap();
    let compact_farm_land = parse_farm_land(&input);
    println!("Original farm land: ");
    print_farm_land(&compact_farm_land);
    println!("Farm land after dilation: ");
    let dilated_farm_land = dilate(compact_farm_land);
    print_farm_land(&dilated_farm_land);
    let analyzed_land = analyze_fences(&dilated_farm_land);
    println!("Farm land after fence analysis: ");
    print_farm_land(&analyzed_land);
    let measurements: Vec<(usize, usize)> = measure_area_and_perimeter(&analyzed_land);
    let total_cost: usize = measurements
        .iter()
        .map(|(area, perimeter)| area * perimeter)
        .sum();
    println!("Total cost: {}", total_cost);
}

fn measure_area_and_perimeter(marked_land: &Vec<Vec<LandLocation>>) -> Vec<(usize, usize)> {
    let mut output = vec![];
    let mut locations_to_visit = marked_land
        .iter()
        .enumerate()
        .map(|(i, row)| {
            row.iter()
                .enumerate()
                .filter(|(_, loc)| {
                    matches!(loc, LandLocation::GardenPlot(_))
                        || matches!(loc, LandLocation::WithinLand)
                })
                .map(|(j, loc)| (Point::new(j as i32, i as i32), loc.clone()))
                .collect::<Vec<(Point, LandLocation)>>()
        })
        .flatten()
        .collect::<HashSet<(Point, LandLocation)>>();

    while locations_to_visit.len() > 0 {
        // Pick any patch of land not yet visited
        let curr = locations_to_visit
            .iter()
            .take(1)
            .map(|r| r.clone())
            .collect::<Vec<(Point, LandLocation)>>()[0];
        // bfs from it to count stuff
        let (area, enclosed_borders) = traverse(curr, &mut locations_to_visit, &marked_land);
        let perimeter = 4 * area - 2 * enclosed_borders;
        println!("Area: {}, Perimeter: {}", area, perimeter);
        output.push((area, perimeter));
    }
    output
}

fn traverse(
    curr: (Point, LandLocation),
    not_yet_seen: &mut HashSet<(Point, LandLocation)>,
    marked_land: &Vec<Vec<LandLocation>>,
) -> (usize, usize) {
    let mut area = 0;
    let mut enclosed_borders = 0;
    let mut queue = vec![curr];
    while queue.len() > 0 {
        //println!("Queue state: {:?}", &queue);
        let (p, loc) = queue.pop().unwrap();
        if !not_yet_seen.contains(&(p, loc)) {
            continue;
        }
        not_yet_seen.remove(&(p, loc));
        if matches!(loc, LandLocation::GardenPlot(_)) {
            //println!("Visiting: {:?}", &(p, loc));
            area += 1;
        } else if matches!(loc, LandLocation::WithinLand) {
            enclosed_borders += 1;
        }

        let nb_land_patches: Vec<(Point, LandLocation)> = p
            .get_neighbours()
            .iter()
            .filter(|nb| nb.is_within_grid(&marked_land))
            .map(|nb| (*nb, marked_land[nb.y()][nb.x()]))
            .collect();

        //println!("Neighbours: {:?}", &nb_land_patches);

        let accessible_tiles = nb_land_patches
            .into_iter()
            .filter(|tup| not_yet_seen.contains(tup))
            .filter(|(_pos, land)| {
                matches!(land, LandLocation::GardenPlot(_))
                    || matches!(land, LandLocation::WithinLand)
            })
            .collect::<Vec<(Point, LandLocation)>>();

        queue.extend(accessible_tiles);
    }
    println!("Area: {}, Enclosed borders: {}", area, enclosed_borders);
    (area, enclosed_borders)
}

fn analyze_fences(dilated_farm_land: &Vec<Vec<LandLocation>>) -> Vec<Vec<LandLocation>> {
    let mut output = dilated_farm_land.clone();
    for (y, row) in dilated_farm_land.iter().enumerate() {
        for (x, location) in row.iter().enumerate() {
            if matches!(location, LandLocation::Empty) {
                let p = Point::new(x as i32, y as i32);
                let nb_land_patches = p
                    .get_neighbours()
                    .iter()
                    .filter(|nb| nb.is_within_grid(&dilated_farm_land))
                    .map(|nb| dilated_farm_land[nb.y()][nb.x()])
                    .filter(|land| matches!(land, LandLocation::GardenPlot(_)))
                    .collect::<Vec<LandLocation>>();

                if nb_land_patches.len() > 1
                    && nb_land_patches
                        .iter()
                        .filter_map(|patch| {
                            if let LandLocation::GardenPlot(c) = patch {
                                Some(c)
                            } else {
                                None
                            }
                        })
                        .collect::<std::collections::HashSet<&char>>()
                        .len()
                        == 1
                {
                    output[p.y()][p.x()] = LandLocation::WithinLand;
                } else {
                    output[p.y()][p.x()] = LandLocation::Fence;
                }
            }
        }
    }
    output
}

fn print_farm_land(farm_land: &Vec<Vec<LandLocation>>) {
    println!(
        "{}",
        farm_land
            .iter()
            .map(|r| r
                .iter()
                .map(|l| format!("{}", l))
                .collect::<Vec<String>>()
                .join(""))
            .collect::<Vec<String>>()
            .join("\n")
    )
}

fn dilate(compact_farm_land: Vec<Vec<LandLocation>>) -> Vec<Vec<LandLocation>> {
    let mut output = vec![];
    let dilated_width = 2 * compact_farm_land.get(0).unwrap().len() + 1;
    output.push(vec![LandLocation::Empty].repeat(dilated_width));
    for row in compact_farm_land {
        let mut new_row = vec![LandLocation::Empty];
        for land in row {
            new_row.push(land);
            new_row.push(LandLocation::Empty);
        }
        output.push(new_row);
        output.push(vec![LandLocation::Empty].repeat(dilated_width));
    }
    output
}

pub fn part2(input_file: &str) {}

fn parse_farm_land(input: &str) -> Vec<Vec<LandLocation>> {
    input
        .lines()
        .map(|l| l.chars().map(|c| LandLocation::GardenPlot(c)).collect())
        .collect()
}

#[derive(Copy, Clone, Eq, PartialEq, Hash, Debug)]
enum LandLocation {
    GardenPlot(char),
    Fence,
    WithinLand,
    Empty,
}

impl Display for LandLocation {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            LandLocation::GardenPlot(c) => write!(f, "{}", c),
            LandLocation::Fence => write!(f, "{}", '#'),
            LandLocation::WithinLand => write!(f, "{}", '_'),
            LandLocation::Empty => write!(f, "{}", ' '),
        }
    }
}
