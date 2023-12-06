use std::fs;

pub fn part1(input_file: &str) {
    let races = read_races(input_file);

    let total_possibilities: u64 = races.iter().map(|r| r.count_winning_strategies()).product();

    println!("Total possibilities of winning all races: {}", total_possibilities);

}

pub fn part2(input_file: &str) {
    let race = read_race_bad_kerning(input_file);

    let total_possibilities = race.count_winning_strategies();

    println!("Total possibilities of winning the race: {}", total_possibilities);
}

#[derive(Debug)]
struct Race {
    duration: u64,
    farthest_distance: u64,
}

impl Race {
    pub fn count_winning_strategies(&self) -> u64 {
        let mut winning_strats: u64 = 0;
        for acceleration_period in 1..(self.duration-1) {
            if self.calculate_travelled_distance(acceleration_period) > self.farthest_distance {
                winning_strats += 1;
            }
        }
        winning_strats
    }

    fn calculate_travelled_distance(&self, acceleration_period: u64) -> u64 {
        (self.duration - acceleration_period) * acceleration_period
    }
}

fn read_race_bad_kerning(input_file: &str) -> Race {
    let input = fs::read_to_string(&input_file).expect("Should have been able to read the file.");

    let lines = input.split("\n").collect::<Vec<&str>>();

    let duration = lines[0]
        .split(" ")
        .map(|s| s.trim())
        .filter(|s| !s.is_empty())
        .skip(1).collect::<String>().parse::<u64>().unwrap();

    let farthest_distance = lines[1]
        .split(" ")
        .map(|s| s.trim())
        .filter(|s| !s.is_empty())
        .skip(1).collect::<String>().parse::<u64>().unwrap();

    Race { duration, farthest_distance }
}

fn read_races(input_file: &str) -> Vec<Race> {
    let input = fs::read_to_string(&input_file).expect("Should have been able to read the file.");

    let lines = input.split("\n").collect::<Vec<&str>>();

    let durations = lines[0]
        .split(" ")
        .filter(|s| !s.is_empty())
        .skip(1)
        .map(|s| s.parse::<u64>())
        .map(|r| r.unwrap())
        .collect::<Vec<u64>>();

    let farthest_distances = lines[1]
        .split(" ")
        .filter(|s| !s.is_empty())
        .skip(1)
        .map(|s| s.parse::<u64>())
        .map(|r| r.unwrap())
        .collect::<Vec<u64>>();

    let mut races: Vec<Race> = vec![];

    for i in 0..durations.len() {
        races.push(Race {
            duration: durations[i],
            farthest_distance: farthest_distances[i],
        });
    }

    races
}
