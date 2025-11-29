use std::{collections::HashMap, fs};

pub fn first_part(input_file: &str) {
    let (mut nums1, mut nums2) = read_lists(input_file);
    nums1.sort();
    nums2.sort();

    let difference: i64 = nums1.iter().zip(nums2.iter()).map(|(x, y)| i64::abs(x - y)).sum();

    print!("Difference: {}\n", difference)
}

pub fn second_part(input_file: &str) {
    let (nums1, nums2) = read_lists(input_file);


    let mut occurrences: HashMap<i64, usize> = HashMap::new();
    for x in &nums2 {
        let new_value = occurrences.get(&x).unwrap_or(&0) + 1;
        occurrences.insert(*x, new_value);
    }

    let total: i64 = nums1.iter().map(|x| x * (*occurrences.get(x).unwrap_or(&0) as i64)).sum();
    print!("Similarity score: {}\n", total)
}

fn read_lists(input_file: &str) -> (Vec<i64>, Vec<i64>) {
    let file_content = fs::read_to_string(input_file).unwrap();
    let lines = file_content.lines();

    let mut list1 = vec![];
    let mut list2 = vec![];
    for line in lines {
        let numbers: Vec<&str> = line.split("  ").map(|s| s.trim()).collect();
        list1.push(*(numbers.get(0).unwrap()));
        list2.push(*(numbers.get(1).unwrap()));
    }

    let nums1: Vec<i64> = list1.iter().map(|s| s.parse::<i64>()).map(|r| r.unwrap()).collect();
    let nums2: Vec<i64> = list2.iter().map(|s| s.parse::<i64>()).map(|r| r.unwrap()).collect();
    return (nums1, nums2)
}

