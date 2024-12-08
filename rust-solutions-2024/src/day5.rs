use std::collections::{HashMap, HashSet};

pub fn part1(input_file: &str) {
    let (rules, updates) = parse_rules_and_updates(input_file);
    println!("Rules:\n{:?}", rules);
    println!("Updates:\n{:?}", updates);

    let correct_updates: Vec<&Update> = updates.iter().filter(|u| is_correct(u, &rules)).collect();

    println!("Correct updates:\n{:?}", correct_updates);

    let middle_elements = correct_updates
        .iter()
        .map(|u| u.0[u.0.len() / 2])
        .collect::<Vec<usize>>();
    println!("Middle elements of correct updates:\n{:?}", middle_elements);

    let sum: usize = middle_elements.iter().sum();
    println!("Sum of middle elements: {}", sum);
}

pub fn part2(input_file: &str) {
    let (rules, updates) = parse_rules_and_updates(input_file);
    println!("Rules:\n{:?}", rules);
    println!("Updates:\n{:?}", updates);

    let incorrect_updates: Vec<&Update> = updates.iter().filter(|u| !is_correct(u, &rules)).collect();

    println!("Incorrect updates:\n{:?}", incorrect_updates);
    let mut cannot_happen_before_map: HashMap<usize, HashSet<usize>> = HashMap::new();
    for r in rules {
        cannot_happen_before_map
            .entry(r.left_page)
            .or_insert_with(|| HashSet::new())
            .insert(r.right_page);
    };

    let mut updates_to_fix: Vec<Vec<Page>> = incorrect_updates.iter()
        .map(|u| u.0.iter().map(|p| Page::new(*p, &cannot_happen_before_map)).collect()).collect();


}

#[derive(Debug, Eq, PartialEq)]
struct Page<'a> {
    number: usize,
    ordering_map: &'a HashMap<usize, HashSet<usize>>
}

impl<'a> PartialOrd for Page<'a> {
    fn partial_cmp(&self, other: &Self) -> Option<std::cmp::Ordering> {
        // TODO: figure out how to compare properly before sorting
        //match self.number.partial_cmp(&other.number) {
        //    Some(core::cmp::Ordering::Equal) => {}
        //    ord => return ord,
        //}
        //self.ordering_map.partial_cmp(&other.ordering_map)
    }
}


impl <'a> Page<'a> {
    fn new(number: usize, ordering_map: &'a HashMap<usize, HashSet<usize>>) -> Self {
        Self { number, ordering_map }
    }
}

fn is_correct(u: &Update, rules: &Vec<Rule>) -> bool {
    let mut cannot_happen_before_map: HashMap<usize, HashSet<usize>> = HashMap::new();
    for r in rules {
        cannot_happen_before_map
            .entry(r.left_page)
            .or_insert_with(|| HashSet::new())
            .insert(r.right_page);
    };
    let mut already_seen: HashSet<usize> = HashSet::new();
    for page in u.0.iter() {
        if let Some(not_before) = cannot_happen_before_map.get(page) {
            for p in already_seen.iter() {
                if not_before.contains(p) {
                    return false;
                }
            }
        }
        already_seen.insert(*page);
    }
    true
}
fn parse_rules_and_updates(input_file: &str) -> (Vec<Rule>, Vec<Update>) {
    let content = std::fs::read_to_string(input_file).unwrap();
    let parts = content.split("\n\n").collect::<Vec<&str>>();
    let rules = parts[0];
    let updates = parts[1];

    (
        rules.lines().map(|r| Rule::from(r)).collect(),
        updates.lines().map(|u| Update::from(u)).collect(),
    )
}

#[derive(Debug)]
struct Update(Vec<usize>);

impl From<&str> for Update {
    fn from(s: &str) -> Self {
        let pages: Vec<usize> = s.split(',').map(|s| s.parse().unwrap()).collect();
        Self(pages)
    }
}

#[derive(Debug)]
struct Rule {
    left_page: usize,
    right_page: usize,
}

impl Rule {
    fn new(left_page: usize, right_page: usize) -> Self {
        Self {
            left_page,
            right_page,
        }
    }
}

impl From<&str> for Rule {
    fn from(s: &str) -> Self {
        let pages: Vec<usize> = s.split('|').map(|s| s.parse().unwrap()).collect();
        Self::new(pages[0], pages[1])
    }
}
