use std::{cmp::min, collections::HashMap, fs};

fn read_patterns(input_file: &str) -> Vec<Vec<Vec<char>>> {
    let contents = fs::read_to_string(input_file).expect("Unable to read the file");
    contents
        .split("\n\n")
        .map(|p| p.lines().map(|l| l.chars().collect()).collect())
        .collect()
}

pub fn first_part(input_file: &str) {
    let patterns = read_patterns(input_file);

    let summarized_pattern_notes: usize = patterns.iter().map(|p| get_pattern_note(p)).sum();

    println!("Summarized pattern notes: {}", summarized_pattern_notes);
}

fn get_pattern_note(pattern: &Vec<Vec<char>>) -> usize {
    // We either find a horizontal or a vertical reflection
    if let Some(row) = get_horizontal_reflection_index(&pattern) {
        100 * (row + 1)
    } else {
        get_vertical_reflection_location(&pattern).unwrap() + 1
    }
}

fn get_horizontal_reflection_index(pattern: &Vec<Vec<char>>) -> Option<usize> {
    // First find two adjacent identical rows
    let mut possible_reflection_indices = Vec::new();
    for (i, row) in pattern.iter().enumerate().take(pattern.len() - 1) {
        if row == &pattern[i + 1] {
            possible_reflection_indices.push(i);
        }
    }
    println!(
        "Possible reflection indices: {:?}",
        possible_reflection_indices
    );
    // Now for each of the possible indices we check if we indeed get a perfect
    // reflection
    for i in possible_reflection_indices {
        let mut perfect_reflection = true;
        for j in 0..=min(i, pattern.len() - 2 - i) {
            if pattern[i - j] != pattern[(i + 1) + j] {
                perfect_reflection = false;
                break;
            }
        }
        if perfect_reflection {
            return Some(i);
        }
    }
    None
}

fn get_vertical_reflection_location(pattern: &Vec<Vec<char>>) -> Option<usize> {
    let mut transpose = Vec::new();
    for i in 0..pattern[0].len() {
        let mut row = Vec::new();
        for j in 0..pattern.len() {
            row.push(pattern[j][i]);
        }
        transpose.push(row);
    }
    get_horizontal_reflection_index(&transpose)
}

pub fn second_part(input_file: &str) {
    let patterns = read_patterns(input_file);

    let summarized_pattern_notes: usize = patterns
        .iter()
        .map(|p| get_pattern_note_with_smudge(p))
        .sum();

    println!("Summarized pattern notes: {}", summarized_pattern_notes);
}

fn get_pattern_note_with_smudge(pattern: &Vec<Vec<char>>) -> usize {
    // We either find a horizontal or a vertical reflection
    if let Some(row) = get_horizontal_reflection_index_with_smudge(&pattern) {
        100 * (row + 1)
    } else {
        get_vertical_reflection_location_with_smudge(&pattern).unwrap() + 1
    }
}

fn rows_match_except_for_one(row1: &Vec<char>, row2: &Vec<char>) -> bool {
    let mut mismatch_count = 0;
    for i in 0..row1.len() {
        if row1[i] != row2[i] {
            mismatch_count += 1;
        }
    }
    mismatch_count == 1
}

fn get_horizontal_reflection_index_with_smudge(pattern: &Vec<Vec<char>>) -> Option<usize> {
    // First find two adjacent identical rows
    let mut possible_reflection_indices = Vec::new();
    for (i, row) in pattern.iter().enumerate().take(pattern.len() - 1) {
        if row == &pattern[i + 1] || rows_match_except_for_one(row, &pattern[i + 1]) {
            possible_reflection_indices.push(i);
        }
    }
    println!(
        "Possible reflection indices: {:?}",
        possible_reflection_indices
    );
    // Now for each of the possible indices we check if we indeed get a perfect
    // reflection
    for i in possible_reflection_indices {
        let mut smudge_already_used = false;
        let mut perfect_reflection = true;
        for j in 0..=min(i, pattern.len() - 2 - i) {
            if rows_match_except_for_one(&pattern[i - j], &pattern[(i + 1) + j]) {
                smudge_already_used = true;
                continue;
            }
            if pattern[i - j] != pattern[(i + 1) + j] {
                perfect_reflection = false;
                break;
            }
        }
        // We need to find the smudge
        if perfect_reflection && smudge_already_used {
            return Some(i);
        }
    }
    None
}

fn get_vertical_reflection_location_with_smudge(pattern: &Vec<Vec<char>>) -> Option<usize> {
    let mut transpose = Vec::new();
    for i in 0..pattern[0].len() {
        let mut row = Vec::new();
        for j in 0..pattern.len() {
            row.push(pattern[j][i]);
        }
        transpose.push(row);
    }
    get_horizontal_reflection_index_with_smudge(&transpose)
}
