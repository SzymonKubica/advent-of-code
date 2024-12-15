use std::{collections::HashMap, fmt::Display, fs};

pub fn part1(input_file: &str) {
    let steps = 15;
    let input = fs::read_to_string(input_file).unwrap();
    let mut stones = parse_stones(&input);
    println!("Initial stones: {:?}", stones);
    let mut cache: std::collections::HashMap<MagicStone, Vec<MagicStone>> = HashMap::new();
    for i in 0..steps {
        println!("Big step: {}", i);
        let mut stones2 = vec![];
        for stone in &stones {
            if cache.contains_key(&stone) {
                stones2.extend(cache.get(&stone).unwrap().clone());
            } else {
                let mut new_stones = vec![(*stone).clone()];
                for j in 0..5 {
                    new_stones = new_stones.iter().map(MagicStone::transform).flatten().collect();
                }
                cache.insert(*stone, new_stones.clone());
                stones2.extend(new_stones.clone());
            }
        }
        stones = stones2;
    }
    println!("Found {} stones after {} steps:", stones.len(), steps);
    //print_stones(&stones);
}

fn print_stones(stones: &Vec<MagicStone>) {
    println!(
            "{}",
            stones
                .iter()
                .map(|s| format!("{}", s))
                .collect::<Vec<String>>()
                .join(" ")
        );
}
pub fn part2(input_file: &str) {}

fn parse_stones(input: &str) -> Vec<MagicStone> {
    input
        .split(" ")
        .map(str::trim)
        .map(str::parse)
        .filter(Result::is_ok)
        .map(Result::unwrap)
        .map(MagicStone::new)
        .collect()
}

#[derive(Debug, Eq, PartialEq, Hash, Copy, Clone)]
struct MagicStone(u64);

impl MagicStone {
    fn new(value: u64) -> Self {
        MagicStone(value)
    }

    pub fn transform(&self) -> Vec<MagicStone> {
        if self.0 == 0 {
            return vec![MagicStone::new(1)];
        }
        if self.0.to_string().len() % 2 == 0 {
            let str_repr = self.0.to_string();
            let first_half = &str_repr[..str_repr.len() / 2];
            let second_half = &str_repr[str_repr.len() / 2..];
            return vec![
                MagicStone::new(first_half.parse().unwrap()),
                MagicStone::new(second_half.parse().unwrap()),
            ];
        }
        return vec![MagicStone::new(self.0 * 2024)];
    }
}

impl Display for MagicStone {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.0)
    }
}
