use std::{collections::HashMap, fs};

#[derive(Debug, Copy, Clone, Eq, PartialEq, Hash)]
enum Spring {
    Unknown,
    Broken,
    Operational,
}

impl From<char> for Spring {
    fn from(value: char) -> Self {
        match value {
            '#' => Spring::Broken,
            '.' => Spring::Operational,
            '?' => Spring::Unknown,
            _ => panic!("'{}' is not a valid spring symbol.", value),
        }
    }
}

impl ToString for Spring {
    fn to_string(&self) -> String {
        match &self {
            Spring::Unknown => "?".to_string(),
            Spring::Broken => "#".to_string(),
            Spring::Operational => ".".to_string(),
        }
    }
}

#[derive(Debug, Eq, PartialEq, Hash, Clone)]
struct ConditionRecord {
    springs: Vec<Spring>,
    damaged_groups: Vec<usize>,
}

impl ToString for ConditionRecord {
    fn to_string(&self) -> String {
        self.springs
            .iter()
            .map(|s| s.to_string())
            .collect::<String>()
            + &"groups: "
            + &self
                .damaged_groups
                .iter()
                .map(|x| x.to_string())
                .collect::<Vec<String>>()
                .join(" ")
    }
}
impl From<String> for ConditionRecord {
    fn from(value: String) -> Self {
        let parts = value.split(" ").collect::<Vec<&str>>();
        let (springs, damaged_groups) = (
            parts[0]
                .chars()
                .map(|c| Spring::from(c))
                .collect::<Vec<Spring>>(),
            parts[1]
                .split(",")
                .map(|c| c.parse::<usize>().unwrap())
                .collect::<Vec<usize>>(),
        );
        ConditionRecord {
            springs,
            damaged_groups,
        }
    }
}

impl From<&str> for ConditionRecord {
    fn from(value: &str) -> Self {
        let parts = value.split(" ").collect::<Vec<&str>>();
        let (springs, damaged_groups) = (
            parts[0]
                .chars()
                .map(|c| Spring::from(c))
                .collect::<Vec<Spring>>(),
            parts[1]
                .split(",")
                .map(|c| c.parse::<usize>().unwrap())
                .collect::<Vec<usize>>(),
        );
        ConditionRecord {
            springs,
            damaged_groups,
        }
    }
}
fn read_condition_records(input_file: &str) -> Vec<ConditionRecord> {
    let contents = fs::read_to_string(&input_file).expect("Unable to read the file.");
    contents
        .lines()
        .map(|l| ConditionRecord::from(l))
        .collect::<Vec<ConditionRecord>>()
}

fn read_unfolded_condition_records(input_file: &str) -> Vec<ConditionRecord> {
    let contents = fs::read_to_string(&input_file).expect("Unable to read the file.");

    contents
        .lines()
        .map(|l| {
            println!("{}", unfold_line(l));
            unfold_line(l)
        })
        .map(|l| ConditionRecord::from(l))
        .collect::<Vec<ConditionRecord>>()
}

fn unfold_line(line: &str) -> String {
    let parts = line.split(" ").collect::<Vec<&str>>();
    let springs = [parts[0]; 5].join("?");
    let damaged_parts = [parts[1]; 5].join(",");
    [springs.to_string(), damaged_parts].join(" ")
}

fn determine_feasible_combinations_memo(
    condition_record: &ConditionRecord,
    memo_map: &mut HashMap<ConditionRecord, usize>,
) -> usize {
    if memo_map.contains_key(condition_record) {
        return memo_map[condition_record];
    }

    if condition_record.damaged_groups.len() == 0 {
        if condition_record.springs.contains(&Spring::Broken) {
            memo_map.insert(condition_record.clone(), 0);
            return 0;
        }
        memo_map.insert(condition_record.clone(), 1);
        return 1;
    }

    if let Some((i, l)) = get_first_contiguous_block_length_and_index(&condition_record.springs) {
        // Here we drop the contiguous block and associated damaged group information
        // provided that the block length matches the spec
        if l == condition_record.damaged_groups[0] {
            // If we encounter a correctly specified group at the start, we
            // drop it and its specification and continue recursively for the simpler
            // case
            let springs = condition_record.springs[(i + l)..]
                .to_vec()
                .into_iter()
                .skip(1)
                .collect::<Vec<Spring>>();
            let damaged_groups = condition_record.damaged_groups[1..].to_vec();
            determine_feasible_combinations_memo(
                &ConditionRecord {
                    springs,
                    damaged_groups,
                },
                memo_map,
            )
        } else {
            memo_map.insert(condition_record.clone(), 0);
            0
        }
    } else {
        // Here we need to set the first encountered ? in two ways and return the
        // sum of the outcomes
        if let Some(next_unknown) = get_first_unknown_index(&condition_record.springs) {
            let mut springs1: Vec<Spring> = condition_record.springs.clone();
            let mut springs2: Vec<Spring> = condition_record.springs.clone();

            springs1[next_unknown] = Spring::Broken;
            springs2[next_unknown] = Spring::Operational;
            let count1 = determine_feasible_combinations_memo(
                &ConditionRecord {
                    springs: springs1,
                    damaged_groups: condition_record.damaged_groups.clone(),
                },
                memo_map,
            );
            let count2 = determine_feasible_combinations_memo(
                &ConditionRecord {
                    springs: springs2,
                    damaged_groups: condition_record.damaged_groups.clone(),
                },
                memo_map,
            );

            memo_map.insert(condition_record.clone(), count1 + count2);
            let _ = memo_map
                .get_mut(condition_record)
                .insert(&mut (count1 + count2));
            count1 + count2
        } else {
            memo_map.insert(condition_record.clone(), 0);
            0
        }
    }
}

fn determine_feasible_combinations(condition_record: &ConditionRecord) -> usize {
    if condition_record.damaged_groups.len() == 0 {
        if condition_record.springs.contains(&Spring::Broken) {
            return 0;
        }
        return 1;
    }

    if let Some((i, l)) = get_first_contiguous_block_length_and_index(&condition_record.springs) {
        // Here we drop the contiguous block and associated damaged group information
        // provided that the block length matches the spec
        if l == condition_record.damaged_groups[0] {
            // If we encounter a correctly specified group at the start, we
            // drop it and its specification and continue recursively for the simpler
            // case
            let springs = condition_record.springs[(i + l)..]
                .to_vec()
                .into_iter()
                .skip(1)
                .collect::<Vec<Spring>>();
            let damaged_groups = condition_record.damaged_groups[1..].to_vec();
            determine_feasible_combinations(&ConditionRecord {
                springs,
                damaged_groups,
            })
        } else {
            0
        }
    } else {
        // Here we need to set the first encountered ? in two ways and return the
        // sum of the outcomes
        if let Some(next_unknown) = get_first_unknown_index(&condition_record.springs) {
            let mut springs1: Vec<Spring> = condition_record.springs.clone();
            let mut springs2: Vec<Spring> = condition_record.springs.clone();

            springs1[next_unknown] = Spring::Broken;
            springs2[next_unknown] = Spring::Operational;
            determine_feasible_combinations(&ConditionRecord {
                springs: springs1,
                damaged_groups: condition_record.damaged_groups.clone(),
            }) + determine_feasible_combinations(&ConditionRecord {
                springs: springs2,
                damaged_groups: condition_record.damaged_groups.clone(),
            })
        } else {
            0
        }
    }
}

fn get_first_contiguous_block_length_and_index(springs: &Vec<Spring>) -> Option<(usize, usize)> {
    let mut curr = 0;
    // first we skip all operational parts
    while curr < springs.len() && springs[curr] == Spring::Operational {
        curr += 1;
    }

    // if the current char is unknown, we return None as in this case we can't
    // unambiguously remove that group
    if curr == springs.len() || springs[curr] == Spring::Unknown {
        return None;
    }

    // Here the current spring must be broken
    let index = curr;
    while curr < springs.len() && springs[curr] == Spring::Broken {
        curr += 1;
    }

    // Now we check
    if curr == springs.len() || springs[curr] == Spring::Operational {
        Some((index, curr - index))
    } else {
        None
    }
}

fn get_first_unknown_index(springs: &Vec<Spring>) -> Option<usize> {
    springs
        .iter()
        .enumerate()
        .filter_map(|(i, s)| if s == &Spring::Unknown { Some(i) } else { None })
        .nth(0)
}
pub fn first_part(input_file: &str) {
    let condition_records = read_condition_records(input_file);

    let mut combinations: Vec<usize> = vec![];
    for condition_record in &condition_records {
        let combs = determine_feasible_combinations(condition_record);
        combinations.push(combs)
    }

    let sum_of_combinations: usize = combinations.iter().sum();
    println!("Total sum of combinations: {}", sum_of_combinations);
}

pub fn second_part(input_file: &str) {
    let condition_records = read_unfolded_condition_records(input_file);

    let mut combinations: Vec<usize> = vec![];
    let mut memo = HashMap::new();
    for record in &condition_records {
        let combs = determine_feasible_combinations_memo(record, &mut memo);
        println!("condition record: {}", record.to_string());
        println!("combinations: {}", combs);
        combinations.push(combs);
    }

    let sum_of_combinations: usize = combinations.iter().sum();
    println!("Total sum of combinations: {}", sum_of_combinations);
}
