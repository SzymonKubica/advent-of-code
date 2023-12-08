use std::{collections::HashMap, fs};

#[derive(Debug, Copy, Clone)]
enum Turn {
    Left,
    Right,
}

#[derive(Debug)]
struct Node {
    pub left: String,
    pub right: String,
}

fn parse_map(input_file: &str) -> (Vec<Turn>, HashMap<String, Node>) {
    let contents = fs::read_to_string(&input_file).expect("Should have been able to read the file");
    let parts = contents.split("\n\n").collect::<Vec<&str>>();
    let (turns_line, node_lines) = (parts[0], parts[1]);

    let turns = turns_line
        .chars()
        .map(|c| if c == 'L' { Turn::Left } else { Turn::Right })
        .collect::<Vec<Turn>>();

    let nodes = node_lines
        .split("\n")
        .filter(|s| !s.is_empty() && s != &" ")
        .collect::<Vec<&str>>();
    let nodes_map = nodes
        .iter()
        .map(|l| parse_node_line(l))
        .collect::<HashMap<String, Node>>();

    (turns, nodes_map)
}

// Example node line: RJK = (DPP, JQR)
fn parse_node_line(line: &str) -> (String, Node) {
    let parts = line.split("=").map(|s| s.trim()).collect::<Vec<&str>>();
    let (node, children) = (parts[0].to_string(), parts[1]);

    // Drop the parentheses and then split.
    let child_split = children[1..(children.len() - 1)]
        .split(", ")
        .collect::<Vec<&str>>();
    let (left, right) = (child_split[0].to_string(), child_split[1].to_string());

    (node, Node { left, right })
}

pub fn part1(input_file: &str) {
    let (turns, nodes) = parse_map(&input_file);

    println!("Turns: {:?}", turns);
    println!("Nodes: {:?}", nodes);

    let mut steps: usize = 0;
    // We always start at AAA
    let mut curr: String = "AAA".to_string();
    let end: String = "ZZZ".to_string();

    while curr != end {
        let current_turn = turns[steps % turns.len()];
        let current_node = nodes.get(&curr).unwrap();
        match current_turn {
            Turn::Left => curr = current_node.left.clone(),
            Turn::Right => curr = current_node.right.clone(),
        }
        steps += 1;
    }

    println!("Total steps required: {}", steps);
}

pub fn part2(input_file: &str) {
    let (turns, nodes) = parse_map(&input_file);

    let curr_nodes = nodes
        .keys()
        .filter(|s| s.chars().nth(2) == Some('A'))
        .map(|s| s.clone())
        .collect::<Vec<String>>();

    println!("Starting nodes: {:?}", curr_nodes);

    // We need to find LCM of all of the times it takes for each node to reach the target
    let steps_for_each_starting_point = curr_nodes
        .iter()
        .map(|n| {
            let mut steps: usize = 0;
            // We always start at AAA
            let mut curr: String = n.clone();

            while curr.chars().nth(2) != Some('Z') {
                let current_turn = turns[steps % turns.len()];
                let current_node = nodes.get(&curr).unwrap();
                match current_turn {
                    Turn::Left => curr = current_node.left.clone(),
                    Turn::Right => curr = current_node.right.clone(),
                }
                steps += 1;
            }
            steps
        })
        .collect::<Vec<usize>>();

    let total_steps = steps_for_each_starting_point.into_iter().fold(1, |acc, s| num::integer::lcm(acc, s));

    println!("Total steps required: {}", total_steps);
}
