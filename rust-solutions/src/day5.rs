use std::fs;

#[derive(Debug)]
struct RangedMap {
    mappings: Vec<RangedMapping>,
}

#[derive(Debug)]
struct RangedMapping {
    destination_start: u64,
    source_start: u64,
    range_width: u64,
}

impl RangedMap {
    pub fn map(&self, source_value: u64) -> u64 {
        for mapping in &self.mappings {
            if (mapping.source_start..(mapping.source_start + mapping.range_width))
                .contains(&source_value)
            {
                return mapping.destination_start + (source_value - mapping.source_start);
            }
        }
        return source_value;
    }
}

pub fn part1(input_file: &str) {
    let (seeds, maps) = read_seeds_and_mappings(input_file);

    let mut seeds_locations: Vec<u64> = vec![];

    for seed in seeds {
        let mut current_value = seed;
        for map in &maps {
            current_value = map.map(current_value);
        }
        seeds_locations.push(current_value);
    }

    println!(
        "Closest Seed location: {}",
        seeds_locations.iter().min().unwrap()
    );
}

pub fn part2(input_file: &str) {
    let (seeds_encoding, maps) = read_seeds_and_mappings(input_file);

    let seeds_from_ranges = find_all_seeds_given_range_encoding(seeds_encoding);

    let mut seeds_locations: Vec<u64> = vec![];

    for seed in seeds_from_ranges {
        let mut current_value = seed;
        for map in &maps {
            current_value = map.map(current_value);
        }
        seeds_locations.push(current_value);
    }

    println!(
        "Closest Seed location: {}",
        seeds_locations.iter().min().unwrap()
    );
}

fn find_all_seeds_given_range_encoding(seeds_encoding: Vec<u64>) -> Vec<u64> {
    let mut seeds: Vec<u64> = vec![];
    for i in (0..seeds_encoding.len()).step_by(2) {
        for seed_number in seeds_encoding[i]..(seeds_encoding[i] + seeds_encoding[i + 1]) {
            seeds.push(seed_number)
        }
    }
    seeds
}

fn read_seeds_and_mappings(input_file: &str) -> (Vec<u64>, Vec<RangedMap>) {
    let lines = fs::read_to_string(&input_file).expect("Should have been able to read the file.");

    let input_blocks = lines.split("\n\n").collect::<Vec<&str>>();

    let seeds = input_blocks[0].split(": ").collect::<Vec<&str>>()[1]
        .split(" ")
        .filter(|s| !s.is_empty())
        .map(|s| s.parse::<u64>())
        .map(|r| r.unwrap())
        .collect::<Vec<u64>>();

    let mappings_blocks = &input_blocks[1..];

    let maps = mappings_blocks
        .iter()
        .map(|b| parse_mapping_block(b))
        .collect::<Vec<RangedMap>>();

    (seeds, maps)
}

fn parse_mapping_block(block: &str) -> RangedMap {
    let lines = block.split("\n").collect::<Vec<&str>>();

    let mappings = lines[1..]
        .iter()
        .filter(|l| !l.is_empty())
        .map(|l| {
            l.split(" ")
                .collect::<Vec<&str>>()
                .iter()
                .filter(|s| !s.is_empty())
                .map(|s| s.parse::<u64>())
                .map(|r| r.unwrap())
                .collect::<Vec<u64>>()
        })
        .map(|v| RangedMapping {
            destination_start: v[0],
            source_start: v[1],
            range_width: v[2],
        })
        .collect::<Vec<RangedMapping>>();

    RangedMap { mappings }
}
