use std::{fmt::Display, fs};

pub fn part1(input_file: &str) {
    let input = fs::read_to_string(input_file).unwrap();
    let disk_layout = parse_disk_layout(input.lines().nth(0).unwrap());
    println!("Parsed disk layout representation: {:?}", disk_layout);
    println!(
        "Disk layout visualization:\n{}",
        disk_layout
            .iter()
            .map(|pos| format!("{}", pos))
            .collect::<Vec<String>>()
            .join("")
    );

    let disk_layout_after_compaction = perform_compaction(disk_layout);

    println!(
        "Disk layout after compaction:\n{}",
        disk_layout_after_compaction
            .iter()
            .map(|pos| format!("{}", pos))
            .collect::<Vec<String>>()
            .join("")
    );

    let checksum = calculate_checksum(disk_layout_after_compaction);
    println!("Checksum: {}", checksum);
}

fn perform_compaction(disk_layout: Vec<DiskPosition>) -> Vec<DiskPosition> {
    let mut back_index = disk_layout.len() - 1;
    let mut front_index = 0;

    let mut output: Vec<DiskPosition> = vec![];

    while front_index <= back_index {
        match disk_layout[front_index] {
            DiskPosition::Empty => {
                while !matches!(disk_layout[back_index], DiskPosition::FileBlock(_)) {
                    back_index -= 1;
                }
                output.push(disk_layout[back_index]);
                back_index -= 1;
            }
            DiskPosition::FileBlock(_) => {
                output.push(disk_layout[front_index]);
            }
            DiskPosition::ContiguousFileStart(_, _) => panic!("This should not happen"),
        };
        front_index += 1;
    }
    output
}

fn perform_compaction_part2(disk_layout: Vec<DiskPosition>) -> Vec<DiskPosition> {
    let mut output: Vec<DiskPosition> = disk_layout.clone();

    let files_indices: Vec<usize> = output
        .iter()
        .enumerate()
        .filter(|(i, pos)| matches!(pos, DiskPosition::ContiguousFileStart(_, _)))
        .map(|(i, pos)| i)
        .rev()
        .collect();

    'outer: for i in files_indices {
        let DiskPosition::ContiguousFileStart(id, len) = disk_layout[i] else {
            panic!("This should not happen")
        };
        // We only try to move the file to the left
        for j in 0..i {
            if output[j..(j + len)]
                .iter()
                .all(|pos| matches!(pos, DiskPosition::Empty))
            {
                output[j] = DiskPosition::ContiguousFileStart(id, len);
                for k in 1..len {
                    output[j + k] = DiskPosition::FileBlock(id);
                }
                for k in 0..len {
                    output[i + k] = DiskPosition::Empty;
                }
                continue 'outer;
            }
        }
    }
    output
}

fn calculate_checksum(disk_layout_after_compaction: Vec<DiskPosition>) -> usize {
    disk_layout_after_compaction
        .iter()
        .enumerate()
        .map(|(i, pos)| match pos {
            DiskPosition::Empty => 0,
            DiskPosition::FileBlock(id) => i * id,
            DiskPosition::ContiguousFileStart(id, len) => i * id,
        })
        .sum::<usize>()
}
pub fn part2(input_file: &str) {
    let input = fs::read_to_string(input_file).unwrap();
    let disk_layout = parse_disk_layout_part2(input.lines().nth(0).unwrap());
    println!("Parsed disk layout representation: {:?}", disk_layout);
    println!(
        "Disk layout visualization:\n{}",
        disk_layout
            .iter()
            .map(|pos| format!("{}", pos))
            .collect::<Vec<String>>()
            .join("")
    );

    let disk_layout_after_compaction = perform_compaction_part2(disk_layout);

    println!(
        "Disk layout after compaction:\n{}",
        disk_layout_after_compaction
            .iter()
            .map(|pos| format!("{}", pos))
            .collect::<Vec<String>>()
            .join("")
    );

    let checksum = calculate_checksum(disk_layout_after_compaction);
    println!("Checksum: {}", checksum);
}

fn parse_disk_layout(input: &str) -> Vec<DiskPosition> {
    return input
        .char_indices()
        .map(|(i, c)| (i, c.to_digit(10).unwrap() as usize))
        .map(|(i, d)| {
            if i % 2 == 0 {
                vec![DiskPosition::FileBlock(i / 2)].repeat(d)
            } else {
                vec![DiskPosition::Empty].repeat(d)
            }
        })
        .flatten()
        .collect();
}

fn parse_disk_layout_part2(input: &str) -> Vec<DiskPosition> {
    return input
        .char_indices()
        .map(|(i, c)| (i, c.to_digit(10).unwrap() as usize))
        .map(|(i, d)| {
            if i % 2 == 0 {
                let mut file = vec![DiskPosition::ContiguousFileStart(i / 2, d)];
                file.append(&mut vec![DiskPosition::FileBlock(i / 2)].repeat(d - 1));
                file
            } else {
                vec![DiskPosition::Empty].repeat(d)
            }
        })
        .flatten()
        .collect();
}

#[derive(Debug, Copy, Clone)]
enum DiskPosition {
    Empty,
    FileBlock(usize),
    ContiguousFileStart(Id, Length),
}

type Id = usize;
type Length = usize;

impl Display for DiskPosition {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            DiskPosition::Empty => write!(f, "."),
            DiskPosition::FileBlock(b) => write!(f, "{}", b),
            DiskPosition::ContiguousFileStart(id, _len) => write!(f, "{}", id),
        }
    }
}
