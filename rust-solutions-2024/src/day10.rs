pub fn part1(input_file: &str) {


}
pub fn part2(input_file: &str) {}

struct MountainLocation(u32);

fn read_topographic_map(input: &str) -> Vec<Vec<MountainLocation>> {
    input
        .lines()
        .map(|l| {
            l.chars()
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
}
