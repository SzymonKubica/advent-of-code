use std::{collections::HashMap, fs};

fn read_steps(input_file: &str) -> Vec<String> {
    let contents = fs::read_to_string(input_file).expect("Unable to read the file");
    contents.trim().split(",").map(|s| s.to_string()).collect()
}

#[derive(Clone)]
struct Lens {
    pub label: String,
    pub focal_length: u32,
}

impl PartialEq for Lens {
    fn eq(&self, other: &Self) -> bool {
        self.label == other.label
    }
}

impl ToString for Lens {
    fn to_string(&self) -> String {
        return format!("[{} {}]", self.label, self.focal_length);
    }
}

#[derive(Copy, Clone)]
enum OperationType {
    Insertion,
    Removal,
}

#[derive(Clone)]
struct Operation {
    pub label: String,
    pub operation_type: OperationType,
    pub lens_to_insert: Option<Lens>,
}

impl From<&String> for Operation {
    fn from(value: &String) -> Self {
        if value.contains("=") {
            let parts = value.split("=").collect::<Vec<&str>>();
            Operation {
                label: parts[0].to_string(),
                operation_type: OperationType::Insertion,
                lens_to_insert: Some(Lens {
                    label: parts[0].to_string(),
                    focal_length: parts[1].parse::<u32>().unwrap(),
                }),
            }
        } else {
            let parts = value.split("-").collect::<Vec<&str>>();
            Operation {
                label: parts[0].to_string(),
                operation_type: OperationType::Removal,
                lens_to_insert: None,
            }
        }
    }
}

impl ToString for Operation {
    fn to_string(&self) -> String {
        match self.operation_type {
            OperationType::Insertion => {
                format!(
                    "Insertion of {}",
                    self.lens_to_insert.as_ref().unwrap().to_string()
                )
            }
            OperationType::Removal => format!("Removal of lens with label {}", self.label),
        }
    }
}

fn hash_string(string: &str) -> u32 {
    let mut value: u32 = 0;

    for c in string.chars() {
        value += c as u32;
        value *= 17;
        value %= 256;
    }
    value
}

pub fn part1(input_file: &str) {
    let steps = read_steps(input_file);

    let sum: u32 = steps.iter().map(|s| hash_string(s)).sum();

    println!("Verification sum {}", sum);
}

pub fn part2(input_file: &str) {
    let steps = read_steps(input_file);

    let operations = steps
        .iter()
        .map(|s| Operation::from(s))
        .collect::<Vec<Operation>>();

    let mut boxes: HashMap<u32, Vec<Lens>> = HashMap::new();

    for operation in operations {
        let index = hash_string(&operation.label);
        match operation.operation_type {
            OperationType::Insertion => {
                if boxes.contains_key(&index)
                    && boxes[&index].contains(operation.lens_to_insert.as_ref().unwrap())
                {
                    *boxes.get_mut(&index).unwrap() = boxes[&index]
                        .iter()
                        .map(|e| {
                            if e == operation.lens_to_insert.as_ref().unwrap() {
                                operation.lens_to_insert.as_ref().unwrap().clone()
                            } else {
                                e.clone()
                            }
                        })
                        .collect::<Vec<Lens>>();
                } else if boxes.contains_key(&index) {
                    boxes
                        .get_mut(&index)
                        .unwrap()
                        .push(operation.lens_to_insert.as_ref().unwrap().clone());
                } else {
                    boxes.insert(
                        index,
                        vec![operation.lens_to_insert.as_ref().unwrap().clone()],
                    );
                }
            }
            OperationType::Removal => {
                if boxes.contains_key(&index) {
                    *boxes.get_mut(&index).unwrap() = boxes[&index]
                        .iter()
                        .filter_map(|e| {
                            if e.label == operation.label {
                                None
                            } else {
                                Some(e.clone())
                            }
                        })
                        .collect::<Vec<Lens>>();
                }
            }
        }
        for (k, v) in boxes.iter() {
            println!(
                "Box {}: {}",
                k,
                v.iter()
                    .map(|l| l.to_string())
                    .collect::<Vec<String>>()
                    .join(" ")
            );
        }

        // The focusing power is given by the number of the box, multiplied by
        // the number in the box (both start at 1) and multiplied by the focal length

        let total_focusing_power = boxes
            .iter()
            .map(|(k, v)| {
                v.iter()
                    .enumerate()
                    .map(|(i, l)| (k + 1) * (i as u32 + 1) * l.focal_length)
                    .sum::<u32>()
            })
            .sum::<u32>();

        println!("Total focusing power {}", total_focusing_power);

    }
}
