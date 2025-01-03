use std::{collections::HashMap, fmt::Display, fs};

pub fn first_part(input_file: &str) {
    let steps = 25;
    let input = fs::read_to_string(input_file).unwrap();
    let mut stones: HashMap<MagicStone, u64> =
        parse_stones(&input).into_iter().map(|s| (s, 1)).collect();
    calculate_stones_after(steps, &mut stones)
}

pub fn second_part(input_file: &str) {
    let steps = 75;
    let input = fs::read_to_string(input_file).unwrap();
    let mut stones: HashMap<MagicStone, u64> =
        parse_stones(&input).into_iter().map(|s| (s, 1)).collect();
    calculate_stones_after(steps, &mut stones)
}


fn calculate_stones_after(steps: u32, stones: &mut HashMap<MagicStone, u64>) {
    println!("Initial stones: {:?}", stones);
    let mut mutated_stones = stones.clone();
    for _ in 0..steps {
        print_stones(&stones);
        mutated_stones = mutated_stones
            .iter()
            .map(|(stone, count)| {
                stone
                    .transform()
                    .into_iter()
                    .map(|s| (s, *count))
                    .collect::<Vec<(MagicStone, u64)>>()
            })
            .flatten()
            .fold(HashMap::new(), |mut acc, stone| {
              let count = acc.entry(stone.0).or_insert(0);
              *count += stone.1;
              acc
            })
    }
    println!("Unique stones after {} steps:", steps);
    print_stones(&mutated_stones);
    let stones_count: u64 = mutated_stones.iter().map(|(_, count)| count).sum();
    println!("Total stones: {}", stones_count);

}

fn print_stones(stones: &HashMap<MagicStone, u64>) {
    println!(
        "{}",
        stones
            .iter()
            .map(|s| format!("{}", s.0))
            .collect::<Vec<String>>()
            .join(" ")
    );
}

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

#[derive(Debug, Eq, PartialEq, Hash, Clone, Copy)]
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
