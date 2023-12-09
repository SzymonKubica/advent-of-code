use std::fs;

pub fn part1(input_file: &str) {
    let histories = read_histories(input_file);

    let sum_extrapolated_values: i32 = histories.iter().map(|h| extrapolate_history(h)).sum();

    println!("Sum of extrapolated values: {}", sum_extrapolated_values);
}

fn extrapolate_history(history: &Vec<i32>) -> i32 {
    let derivative_histories = get_derivative_histories(history);

    let mut appended_values: Vec<i32> = vec![];
    // We append 0 to the sequence at the bottom.
    let mut curr_value = 0;

    for history in derivative_histories {
        let next_value = history.last().unwrap();
        appended_values.push(curr_value + next_value);
        curr_value = appended_values.last().unwrap().clone();
    }

    println!("{:?}", appended_values);

    *appended_values.last().unwrap()
}

pub fn part2(input_file: &str) {
    let histories = read_histories(input_file);

    let sum_extrapolated_values: i32 = histories
        .iter()
        .map(|h| extrapolate_history_backwards(h))
        .sum();

    println!("Sum of extrapolated values: {}", sum_extrapolated_values);
}

fn get_derivative_histories(history: &Vec<i32>) -> Vec<Vec<i32>> {
    let mut curr_history = history.clone();
    let mut derivative_histories = vec![history.clone()];

    while !curr_history.iter().all(|x| x == &0) {
        let mut new_derivative_history = vec![];
        for (i, x) in curr_history.iter().take(curr_history.len() - 1).enumerate() {
            new_derivative_history.push(curr_history.get(i + 1).unwrap() - x);
        }
        derivative_histories.push(new_derivative_history.clone());
        curr_history = new_derivative_history;
    }

    derivative_histories.reverse();
    derivative_histories
}

fn extrapolate_history_backwards(history: &Vec<i32>) -> i32 {
    let derivative_histories = get_derivative_histories(history);

    let mut appended_values: Vec<i32> = vec![];
    // We append 0 to the sequence at the bottom.
    let mut curr_value = 0;

    for history in derivative_histories {
        let next_value = history.first().unwrap();
        appended_values.push(next_value - curr_value);
        curr_value = appended_values.last().unwrap().clone();
    }

    println!("{:?}", appended_values);

    *appended_values.last().unwrap()
}

fn read_histories(input_file: &str) -> Vec<Vec<i32>> {
    fs::read_to_string(&input_file)
        .expect("Unable to read the file")
        .lines()
        .map(|l| {
            l.split(" ")
                .map(|s| s.parse::<i32>().unwrap())
                .collect::<Vec<i32>>()
        })
        .collect()
}
