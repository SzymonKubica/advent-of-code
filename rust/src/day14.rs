use std::fs;

#[derive(Debug, Copy, Clone, Eq, PartialEq)]
enum Rock {
    Rounded,
    Cube,
}

impl Rock {
    fn from_char(value: char) -> Option<Rock> {
        match value {
            'O' => Some(Rock::Rounded),
            '#' => Some(Rock::Cube),
            _ => None,
        }
    }
}

impl ToString for Rock {
    fn to_string(&self) -> String {
        match self {
            Rock::Rounded => "O".to_string(),
            Rock::Cube => "#".to_string(),
        }
    }
}

fn print_platform(platform: &Vec<Vec<Option<Rock>>>) {
    for row in platform {
        for rock in row {
            match rock {
                Some(rock) => print!("{}", rock.to_string()),
                None => print!("."),
            }
        }
        println!("");
    }
}

fn load_platform(input_file: &str) -> Vec<Vec<Option<Rock>>> {
    let content = fs::read_to_string(input_file).expect("Should have been able to read the file");

    content
        .split("\n")
        .map(|line| {
            line.chars()
                .map(|c| Rock::from_char(c))
                .collect::<Vec<Option<Rock>>>()
        })
        .collect::<Vec<Vec<Option<Rock>>>>()
}

fn shift_north(platform: &Vec<Vec<Option<Rock>>>) -> Vec<Vec<Option<Rock>>> {
    let transposed = transpose(platform);
    let result = shift_west(&transposed);
    transpose(&result)
}

fn shift_east(platform: &Vec<Vec<Option<Rock>>>) -> Vec<Vec<Option<Rock>>> {
    let mut result = Vec::new();

    for row in platform {
        let mut temp = row.clone();
        temp.reverse();
        temp = shift_row_west(&temp);
        temp.reverse();
        result.push(temp);
    }
    result
}

fn shift_south(platform: &Vec<Vec<Option<Rock>>>) -> Vec<Vec<Option<Rock>>> {
    let mut temp = platform.clone();
    temp.reverse();
    temp = shift_north(&temp);
    temp.reverse();
    temp
}

fn shift_west(platform: &Vec<Vec<Option<Rock>>>) -> Vec<Vec<Option<Rock>>> {
    let mut result = Vec::new();

    for row in platform {
        result.push(shift_row_west(&row));
    }
    result
}

fn shift_row_west(row: &Vec<Option<Rock>>) -> Vec<Option<Rock>> {
    let mut result = Vec::new();
    for (i, elem) in row.iter().enumerate() {
        if let Some(rock) = elem {
            match rock {
                Rock::Rounded => result.push(Some(Rock::Rounded)),
                Rock::Cube => {
                    for _ in 0..(i - result.len()) {
                        result.push(None);
                    }
                    result.push(Some(Rock::Cube));
                }
            };
        };
    }
    while result.len() < row.len() {
        result.push(None);
    }
    result
}

fn transpose(platform: &Vec<Vec<Option<Rock>>>) -> Vec<Vec<Option<Rock>>> {
    let mut result = Vec::new();
    for row in 0..platform.len() {
        for col in 0..platform[row].len() {
            if result.len() <= col {
                result.push(Vec::new());
            }
            if let Some(rock) = platform[row][col] {
                result[col].push(Some(rock.clone()));
            } else {
                result[col].push(None);
            }
        }
    }
    result
}
pub fn first_part(input_file: &str) {
    println!("Original platform: ");
    let platform = load_platform(input_file);
    print_platform(&platform);
    let shifted = shift_north(&platform);

    println!("Shifted platform: ");
    print_platform(&shifted);

    let load = calculate_load(&shifted);
    println!("Load on the north support beams: {}", load);
}

fn calculate_load(shifted: &Vec<Vec<Option<Rock>>>) -> usize {
    let mut load = 0;
    for (i, row) in shifted.iter().enumerate() {
        load += (shifted.len() - i)
            * row
                .iter()
                .filter(|o| o.is_some())
                .filter(|o| o.unwrap() == Rock::Rounded)
                .count();
    }
    load
}

fn convergence_check(
    platform: &Vec<Vec<Option<Rock>>>,
    history: &Vec<Vec<Vec<Option<Rock>>>>,
) -> bool {
    if history.contains(&platform) {
        let match_index = history
            .iter()
            .enumerate()
            .filter_map(|(i, x)| if &x == &platform { Some(i) } else { None })
            .nth(0)
            .unwrap();
        println!("Converged, equal to the iteration: {}", match_index);
        true
    } else {
        false
    }
}

pub fn second_part(input_file: &str) {
    println!("Original platform: ");
    let mut platform = load_platform(input_file);

    let mut history = vec![platform.clone()];
    let mut counter = 0;
    let iterations = 1000000000;
    for i in 0..iterations {
        println!("Iteration: {i}");
        platform = shift_north(&platform);
        platform = shift_west(&platform);
        platform = shift_south(&platform);
        platform = shift_east(&platform);
        if convergence_check(&platform, &history) {
            break;
        }
        print_platform(&platform);
        history.push(platform.clone());
        counter += 1;
    }

    let match_index = history
        .iter()
        .enumerate()
        .filter_map(|(i, x)| if x == &platform { Some(i) } else { None })
        .nth(0)
        .unwrap();

    // We need to figure out the period and then just repeat that
    let period = counter - match_index + 1;
    println!("Period: {}", period);

    // We skip the periodic iterations:
    let remainder_iterations = (iterations - match_index) % period;

    let platform = history[match_index + remainder_iterations].clone();


    // We need to find the index of the 10000

    println!("Shifted platform: ");
    print_platform(&platform);

    let load = calculate_load(&platform);
    println!("Load on the north support beams: {}", load);
}
