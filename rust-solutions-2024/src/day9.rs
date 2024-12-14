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

    let disk_layout_after_compaction = disk_layout
        .into_iter()
        .filter_map(|pos| {
            if let DiskPosition::File(_b) = pos {
                Some(pos.clone())
            } else {
                None
            }
        })
        .collect::<Vec<DiskPosition>>();

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

fn calculate_checksum(disk_layout_after_compaction: Vec<DiskPosition>) -> usize {
    let mut current_position = 0;
    let mut checksum = 0;
    for pos in disk_layout_after_compaction {
        match pos {
            DiskPosition::Empty(e) => {
                current_position += e.length;
            },
            DiskPosition::File(f) => {
                checksum += f.id * current_position * f.length;
                current_position += f.length;
            }
        }
    }
    checksum

}
pub fn part2(input_file: &str) {}

fn parse_disk_layout(input: &str) -> Vec<DiskPosition> {
    return input
        .char_indices()
        .map(|(i, c)| (i, c.to_digit(10).unwrap() as usize))
        .map(|(i, d)| {
            if i % 2 == 0 {
                DiskPosition::File(BlockFile::new(i / 2, d))
            } else {
                DiskPosition::Empty(EmptySpace::new(d))
            }
        })
        .collect();
}

#[derive(Debug, Copy, Clone)]
enum DiskPosition {
    Empty(EmptySpace),
    File(BlockFile),
}

impl Display for DiskPosition {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            DiskPosition::Empty(e) => write!(f, "{}", e),
            DiskPosition::File(b) => write!(f, "{}", b),
        }
    }
}

#[derive(Debug, Copy, Clone)]
struct EmptySpace {
    length: usize,
}

impl EmptySpace {
    fn new(length: usize) -> Self {
        Self { length }
    }
}

impl Display for EmptySpace {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.write_str(&".".repeat(self.length))
    }
}

#[derive(Debug, Copy, Clone)]
struct BlockFile {
    id: usize,
    length: usize,
}

impl BlockFile {
    fn new(id: usize, length: usize) -> Self {
        Self { id, length }
    }
}

impl Display for BlockFile {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.write_str(&format!("{}", self.id).repeat(self.length))
    }
}
