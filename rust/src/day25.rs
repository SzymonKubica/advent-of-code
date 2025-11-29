use rand::Rng;
use std::collections::{BinaryHeap, HashMap};

#[derive(Debug, Clone)]
struct Node {
    name: String,
    neighbours: Vec<String>,
}

impl From<&str> for Node {
    fn from(value: &str) -> Self {
        let parts = value.split(": ").collect::<Vec<&str>>();
        let name = parts[0].to_string();

        let neighbours = parts[1]
            .split(" ")
            .map(|s| s.to_string())
            .collect::<Vec<String>>();

        Node { name, neighbours }
    }
}

impl ToString for Node {
    fn to_string(&self) -> String {
        format!("{}: {}", self.name, self.neighbours.join(" "))
    }
}

fn read_nodes(input_file: &str) -> HashMap<String, Node> {
    let contents = std::fs::read_to_string(&input_file).unwrap();

    contents
        .lines()
        .map(|l| Node::from(l))
        .map(|n| (n.name.clone(), n))
        .collect()
}
pub fn first_part(input_file: &str) {
    let mut nodes = read_nodes(input_file);
    println!("{}", "Input");
    for (name, node) in nodes.iter() {
        println!("{}", node.to_string());
    }

    for (name, node) in nodes.clone() {
        for neighbour in node.neighbours.iter() {
            if !nodes.contains_key(neighbour) {
                nodes.insert(
                    neighbour.clone(),
                    Node {
                        name: neighbour.clone(),
                        neighbours: vec![node.name.clone()],
                    },
                );
            } else {
                let neighbour_node = nodes.get_mut(neighbour).unwrap();
                if !neighbour_node.neighbours.contains(&name) {
                    neighbour_node.neighbours.push(name.to_string());
                }
            }
        }
    }

    println!("{}", "Bi-directional graph");
    for (name, node) in nodes.iter() {
        println!("{}", node.to_string());
    }

    //monte_carlo_brute_force(&nodes);
    kragers_algorithm(&nodes);
}

fn get_random_edge(nodes: &HashMap<String, Node>) -> (String, String) {
    let mut rng = rand::thread_rng();
    let index: usize = rng.gen::<usize>() % &nodes.len();
    let index2: usize = rng.gen::<usize>();
    let start = nodes.keys().nth(index % nodes.len()).unwrap();
    let neighbours = nodes.get(start).unwrap().neighbours.clone();
    let end = neighbours.get(index2 % neighbours.len()).unwrap();
    (start.to_string(), end.to_string())
}

fn kragers_algorithm(nodes: &HashMap<String, Node>) {
    let mut contracted_nodes = nodes.clone();

    'outer: loop {
        let mut contracted_nodes = nodes.clone();
        while contracted_nodes.len() > 2 {
            let (start, end) = get_random_edge(&contracted_nodes);
            let mut new_node = Node {
                name: start.clone() + &end,
                neighbours: vec![],
            };

            for (name, node) in contracted_nodes.clone() {
                if node.neighbours.contains(&start) && node.name != end {
                    let node = contracted_nodes.get_mut(&name).unwrap();

                    node.neighbours
                        .clone()
                        .iter()
                        .filter(|n| *n == &start)
                        .for_each(|_| {
                            new_node.neighbours.push(name.clone());
                            node.neighbours.push(new_node.name.clone());
                        });

                    node.neighbours.retain(|n| *n != start);
                }
                if node.neighbours.contains(&end) && node.name != start {
                    let node = contracted_nodes.get_mut(&name).unwrap();

                    node.neighbours
                        .clone()
                        .iter()
                        .filter(|n| *n == &end)
                        .for_each(|_| {
                            new_node.neighbours.push(name.clone());
                            node.neighbours.push(new_node.name.clone());
                        });

                    node.neighbours.retain(|n| *n != end);
                }
            }
            contracted_nodes.remove(&start);
            contracted_nodes.remove(&end);
            contracted_nodes.insert(new_node.name.clone(), new_node);

            println!("\nAfter contraction");
            for (name, node) in contracted_nodes.iter() {
                println!("{}", node.to_string());
            }
        }
        //println!("After contraction: {:?}", contracted_nodes);
        if contracted_nodes
            .iter()
            .all(|(_name, node)| node.neighbours.len() == 3)
        {
            println!("\nAfter successful contraction");
            let mut lengths = vec![];
            for (name, node) in contracted_nodes.iter() {
                println!("{}", node.to_string());
                lengths.push(node.name.len());
            }
            // Each node adds 3 letters to the collapsed node, hence divide by 3.
            let group_a_size = lengths[0] / 3;
            let group_b_size = lengths[1] / 3;

            println!("Size of the first group: {}", group_a_size);
            println!("Size of the second group: {}", group_b_size);

            println!(
                "Product of the two group sizes: {}",
                group_a_size * group_b_size
            );
            break;
        }
    }
}

fn monte_carlo_brute_force(nodes: &HashMap<String, Node>) {
    // Idea: sample two nodes at random, find the shortest path between them
    // and then increment the 'traffic' counters for each edge. The edges
    // to remove are the ones where the traffic is the highest.
    let mut set_b_size = 0;
    while set_b_size == 0 {
        let mut nodes_inner = nodes.clone();
        for _ in 0..3 {
            let highest_edge = find_highest_traffic_edge(&nodes_inner);
            println!("Highest traffic edge found: {:?}", highest_edge);
            let node_a = nodes_inner.get_mut(&highest_edge.0).unwrap();
            node_a.neighbours.retain(|n| *n != highest_edge.1);
            let node_b = nodes_inner.get_mut(&highest_edge.1).unwrap();
            node_b.neighbours.retain(|n| *n != highest_edge.0);
        }

        // Now we should have separated the two graphs so we just need to count the
        // elements inside the two disjoint graphs.

        let mut visited: Vec<String> = vec![];
        let mut queue: Vec<String> = vec![];

        let start = nodes_inner.keys().nth(0).unwrap();
        queue.push(start.clone());

        while !queue.is_empty() {
            let curr = queue.pop().unwrap();
            if visited.contains(&curr) {
                continue;
            }
            visited.push(curr.clone());
            for neighbour in nodes_inner.get(&curr).unwrap().neighbours.iter() {
                if !visited.contains(neighbour) {
                    queue.push(neighbour.clone());
                }
            }
        }

        let set_a_size = visited.len();
        set_b_size = nodes_inner.len() - set_a_size;

        println!("Set A size: {}", set_a_size);
        println!("Set B size: {}", set_b_size);

        println!("Product of the two set sizes: {}", set_a_size * set_b_size);
    }
}

fn find_highest_traffic_edge(nodes: &HashMap<String, Node>) -> (String, String) {
    let mut traffic: HashMap<(String, String), usize> = HashMap::new();
    let mut history: HashMap<(String, String), Vec<(String, String)>> = HashMap::new();
    let mut counter = 0;
    let mut rng = rand::thread_rng();
    while counter < 3000 {
        let index1: usize = rng.gen::<usize>() % &nodes.len();
        let index2: usize = rng.gen::<usize>() % &nodes.len();

        let node1 = nodes.values().nth(index1 % nodes.len()).unwrap();
        let node2 = nodes.values().nth(index2 % nodes.len()).unwrap();

        //println!("Finding path: {} -> {}", node1.name, node2.name);
        if history.contains_key(&(node1.name.clone(), node2.name.clone())) {
            continue;
        }

        let touched_edges: Vec<(String, String)> =
            if history.contains_key(&(node1.name.clone(), node2.name.clone())) {
                history
                    .get(&(node1.name.clone(), node2.name.clone()))
                    .unwrap()
                    .clone()
            } else {
                shortest_path(node1, node2, &nodes)
            };
        history.insert(
            (node1.name.clone(), node2.name.clone()),
            touched_edges.clone(),
        );

        for edge in &touched_edges {
            if touched_edges.len() > 10 {
                if traffic.contains_key(&edge) {
                    *traffic.get_mut(&edge).unwrap() += 1;
                } else {
                    traffic.insert(edge.clone(), 1);
                }
            }
        }
        counter += 1;
    }

    let mut edges = traffic
        .into_iter()
        .collect::<Vec<((String, String), usize)>>();
    edges.sort_by(|(_k1, v1), (_k2, v2)| v2.cmp(v1));

    edges[0].0.clone()
}

#[derive(Debug, Clone, Eq)]
struct State {
    node_name: String,
    distance: usize,
}

impl PartialEq for State {
    fn eq(&self, other: &Self) -> bool {
        self.node_name == other.node_name && self.distance == other.distance
    }
}

impl PartialOrd for State {
    fn partial_cmp(&self, other: &Self) -> Option<std::cmp::Ordering> {
        self.distance.partial_cmp(&other.distance)
    }
}

impl Ord for State {
    fn cmp(&self, other: &Self) -> std::cmp::Ordering {
        return self.distance.cmp(&other.distance);
    }
}

fn shortest_path(
    start: &Node,
    destination: &Node,
    nodes: &HashMap<String, Node>,
) -> Vec<(String, String)> {
    let mut parent_map: HashMap<String, String> = HashMap::new();

    let initial_state = State {
        node_name: start.name.clone(),
        distance: 0,
    };

    let mut queue: BinaryHeap<State> = BinaryHeap::new();
    queue.push(initial_state);
    let mut tentative_distances: HashMap<String, usize> = HashMap::new();
    tentative_distances.insert(start.name.clone(), 0);

    while let Some(state) = queue.pop() {
        if state.node_name == destination.name {
            break;
        }
        let node = nodes.get(&state.node_name).unwrap();
        for neighbour in node.neighbours.iter() {
            let new_distance = state.distance + 1;
            if let Some(distance) = tentative_distances.get(neighbour) {
                if new_distance >= *distance {
                    continue;
                }
            }
            let neighbour_state = State {
                node_name: neighbour.clone(),
                distance: new_distance,
            };
            parent_map.insert(neighbour.clone(), state.node_name.clone());
            tentative_distances.insert(neighbour.clone(), new_distance);
            queue.push(neighbour_state.clone());
        }
    }

    let mut path = vec![];
    let mut curr = destination.name.clone();
    while curr != start.name {
        let parent = parent_map.get(&curr).unwrap();
        path.push((parent.clone(), curr.clone()));
        curr = parent.clone();
    }

    path
}

pub fn second_part(input_file: &str) {}
