use std::{collections::HashMap, fs};

pub fn first_part(input_file: &str) {
    let reports = read_reports(input_file);
    let safe_reports_count = reports.into_iter().filter(|r| is_safe(r)).count();
    println!("Safe reports: {}", safe_reports_count)
}

fn is_safe(report: &Report) -> bool {
    return (is_all_increasing(report) || is_all_decreasing(report)) && step_check(report);
}

fn is_all_increasing(report: &Report) -> bool {
    for (i, level) in report.0[1..].iter().enumerate() {
        if report.0[i] >= *level {
            return false;
        }
    }
    return true;
}

fn is_all_decreasing(report: &Report) -> bool {
    for (i, level) in report.0[1..].iter().enumerate() {
        if report.0[i] <= *level {
            return false;
        }
    }
    return true;
}

fn step_check(report: &Report) -> bool {
    for (i, level) in report.0[1..].iter().enumerate() {
        let diff = (level - report.0[i]).abs();
        if diff < 1 || 3 < diff {
            return false;
        }
    }
    return true;
}

pub fn second_part(input_file: &str) {
    let reports = read_reports(input_file);
    let safe_reports_count = reports
        .into_iter()
        .filter(|r| is_safe_tolerate_1(r))
        .count();
    println!("Safe reports: {}", safe_reports_count)
}

fn is_safe_tolerate_1(report: &Report) -> bool {
    let mut reports_to_check: Vec<Report> = vec![];
    for i in 1..(report.0.len()+1) {
        reports_to_check.push(Report(
            report.0[..i-1]
                .to_vec()
                .into_iter()
                .chain(report.0[i..].to_vec().into_iter())
                .collect(),
        ));
    }

    println!("Testing report: {:?}", report);
    for report in &reports_to_check {
        println!("Testing report: {:?}", report)
    }

    return is_safe(report) || reports_to_check.iter().any(|r| is_safe(r));
}

#[derive(Debug)]
struct Report(Vec<i32>);
fn read_reports(input_file: &str) -> Vec<Report> {
    let file_content = fs::read_to_string(input_file).unwrap();
    let lines = file_content.lines();

    let mut reports = vec![];
    for line in lines {
        reports.push(Report(
            line.split(" ")
                .map(|s| s.trim().parse::<i32>())
                .filter_map(|x| Some(x.unwrap()))
                .collect(),
        ));
    }
    reports
}
