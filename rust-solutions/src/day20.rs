use std::collections::{HashMap, HashSet, VecDeque};

trait Module {
    fn receive_pulse(&mut self, pulse: Pulse) -> Vec<Pulse>;
    fn get_type(&self) -> ModuleType;
    fn to_string(&self) -> String;
    fn get_outputs(&self) -> Vec<String>;
}

enum ModuleType {
    Broadcaster,
    FlipFlop,
    Conjunction,
}

struct Pulse {
    pulse_type: PulseType,
    sender: String,
    receiver: String,
}

#[derive(Clone, Copy, Eq, PartialEq)]
enum PulseType {
    Low,
    High,
}

struct Broadcaster {
    outputs: Vec<String>,
}

impl Module for Broadcaster {
    fn receive_pulse(&mut self, pulse: Pulse) -> Vec<Pulse> {
        self.outputs
            .iter()
            .map(|o| Pulse {
                pulse_type: pulse.pulse_type.clone(),
                sender: "broadcaster".to_string(),
                receiver: o.clone(),
            })
            .collect()
    }

    fn get_type(&self) -> ModuleType {
        ModuleType::Broadcaster
    }
    fn to_string(&self) -> String {
        format!("broadcaster -> {}", self.outputs.join(", "))
    }

    fn get_outputs(&self) -> Vec<String> {
        self.outputs.clone()
    }
}

impl From<&str> for Broadcaster {
    fn from(value: &str) -> Self {
        let parts = value.split(" -> ").collect::<Vec<&str>>();
        let outputs = parts[1].split(", ").map(|s| s.to_string()).collect();
        Broadcaster { outputs }
    }
}

struct FlipFlop {
    name: String,
    is_on: bool,
    outputs: Vec<String>,
}

impl FlipFlop {
    pub fn new(name: String, outputs: Vec<String>) -> FlipFlop {
        FlipFlop {
            name,
            is_on: false,
            outputs,
        }
    }
}

impl From<&str> for FlipFlop {
    fn from(value: &str) -> Self {
        let parts = value.split(" -> ").collect::<Vec<&str>>();
        let name = parts[0][1..].to_string();
        let outputs = parts[1].split(", ").map(|s| s.to_string()).collect();
        FlipFlop::new(name, outputs)
    }
}

impl Module for FlipFlop {
    fn receive_pulse(&mut self, pulse: Pulse) -> Vec<Pulse> {
        if let PulseType::Low = pulse.pulse_type {
            let emitted_pulse = if self.is_on {
                PulseType::Low
            } else {
                PulseType::High
            };
            self.is_on = !self.is_on;

            self.outputs
                .iter()
                .map(|o| Pulse {
                    pulse_type: emitted_pulse,
                    sender: self.name.clone(),
                    receiver: o.clone(),
                })
                .collect()
        } else {
            vec![]
        }
    }

    fn get_type(&self) -> ModuleType {
        ModuleType::FlipFlop
    }
    fn to_string(&self) -> String {
        format!("%{} -> {}", self.name, self.outputs.join(", "))
    }
    fn get_outputs(&self) -> Vec<String> {
        self.outputs.clone()
    }
}

struct Conjunction {
    name: String,
    inputs: Vec<String>,
    outputs: Vec<String>,
    memory: HashMap<String, PulseType>,
}

impl Conjunction {
    pub fn new(name: String, inputs: Vec<String>, outputs: Vec<String>) -> Conjunction {
        let memory = inputs.iter().map(|s| (s.clone(), PulseType::Low)).collect();
        Conjunction {
            name,
            inputs,
            outputs,
            memory,
        }
    }
}

impl Module for Conjunction {
    fn receive_pulse(&mut self, pulse: Pulse) -> Vec<Pulse> {
        *self.memory.get_mut(&pulse.sender).unwrap() = pulse.pulse_type;
        let emitted_pulse = if self.memory.iter().all(|(_, p)| p == &PulseType::High) {
            PulseType::Low
        } else {
            PulseType::High
        };

        self.outputs
            .iter()
            .map(|o| Pulse {
                pulse_type: emitted_pulse,
                sender: self.name.clone(),
                receiver: o.clone(),
            })
            .collect()
    }

    fn get_type(&self) -> ModuleType {
        ModuleType::Conjunction
    }
    fn to_string(&self) -> String {
        format!("&{} -> {}", self.name, self.outputs.join(", "))
    }
    fn get_outputs(&self) -> Vec<String> {
        self.outputs.clone()
    }
}

impl From<&str> for Conjunction {
    fn from(value: &str) -> Self {
        let parts = value.split(" -> ").collect::<Vec<&str>>();
        let name = parts[0][1..].to_string();
        let outputs = parts[1].split(", ").map(|s| s.to_string()).collect();
        Conjunction::new(name, Vec::new(), outputs)
    }
}

fn read_configuration(input_file: &str) -> HashMap<String, Box<dyn Module>> {
    let contents = std::fs::read_to_string(input_file).unwrap();
    let lines = contents.trim().split("\n").collect::<Vec<&str>>();

    let mut modules: HashMap<String, Box<dyn Module>> = HashMap::new();
    for line in lines {
        match line.chars().nth(0).unwrap() {
            'b' => {
                modules.insert("broadcaster".to_string(), Box::new(Broadcaster::from(line)));
            }
            '%' => {
                let module = FlipFlop::from(line);
                modules.insert(module.name.clone(), Box::new(module));
            }

            '&' => {
                let module = Conjunction::from(line);
                modules.insert(module.name.clone(), Box::new(module));
            }
            _ => panic!("Invalid input"),
        }
    }

    let mut inputs_map: HashMap<String, Vec<String>> = HashMap::new();
    for (name, module) in modules.iter() {
        for output in module.get_outputs().iter() {
            if inputs_map.contains_key(output) {
                inputs_map.get_mut(output).unwrap().push(name.clone());
            } else {
                inputs_map.insert(output.clone(), vec![name.clone()]);
            }
        }
    }

    for (name, module) in modules.iter_mut() {
        if let ModuleType::Conjunction = module.get_type() {
            *module = Box::new(Conjunction::new(
                name.clone(),
                inputs_map[name].clone(),
                module.get_outputs(),
            ));
        }
    }

    // We model the output as a dummy broadcaster node with no outputs.
    let output = Broadcaster { outputs: vec![] };
    modules.insert("output".to_string(), Box::new(output));

    modules
}

pub fn part1(input_file: &str) {
    let mut configuration = read_configuration(input_file);

    for (_name, module) in configuration.iter() {
        println!("{}", module.to_string());
    }

    let mut low_pulses_count = 0;
    let mut high_pulses_count = 0;

    for _ in 0..1000 {
        // Press the button to initiate the broadcast
        let broadcaster = configuration.get_mut("broadcaster").unwrap();
        low_pulses_count += 1;
        let emitted_pulses = broadcaster.receive_pulse(Pulse {
            pulse_type: PulseType::Low,
            sender: "".to_string(),
            receiver: "".to_string(),
        });

        let mut pulses: VecDeque<Pulse> = VecDeque::new();
        for pulse in emitted_pulses {
            match pulse.pulse_type {
                PulseType::Low => low_pulses_count += 1,
                PulseType::High => high_pulses_count += 1,
            }
            pulses.push_back(pulse);
        }

        while !pulses.is_empty() {
            let current_pulse = pulses.pop_front().unwrap();

            let Some(receiver) = configuration.get_mut(&current_pulse.receiver) else {
                continue;
            };
            let emitted_pulses = receiver.receive_pulse(current_pulse);

            for pulse in emitted_pulses {
                match pulse.pulse_type {
                    PulseType::Low => low_pulses_count += 1,
                    PulseType::High => high_pulses_count += 1,
                }
                pulses.push_back(pulse);
            }
        }
    }

    println!("Low pulses: {}", low_pulses_count);
    println!("High pulses: {}", high_pulses_count);
    println!(
        "Product of pulses counts: {}",
        low_pulses_count as u64 * high_pulses_count
    );
}
pub fn part2(input_file: &str) {
    // this solution relies on the fact that rx is connected to a conjunction
    // so we can just check when all of its inputs will be high we can deduce that
    // rx will get a low pulse.
    let mut configuration = read_configuration(input_file);

    for (_name, module) in configuration.iter() {
        println!("{}", module.to_string());
    }

    let mut button_presses = 0;

    let rx_input = configuration
        .iter()
        .filter(|(_, m)| m.get_outputs().contains(&"rx".to_string()))
        .nth(0)
        .unwrap()
        .0
        .clone();

    println!("rx input: {}", rx_input);

    let mut conjunction_inputs = configuration
        .iter()
        .filter(|(_, m)| m.get_outputs().contains(&rx_input))
        .map(|(n, _)| n.clone())
        .collect::<Vec<String>>();

    let conj_inputs_children = conjunction_inputs
        .iter()
        .map(|i| {
            (
                i.clone(),
                configuration
                    .iter()
                    .filter(|(_, m)| m.get_outputs().contains(&i))
                    .map(|(n, _)| n.clone())
                    .collect::<Vec<String>>(),
            )
        })
        .collect::<HashMap<String, Vec<String>>>();

    let conj_inputs_granchildren: HashMap<String, Vec<String>> = conj_inputs_children
        .iter()
        .map(|(i, ins)| ins.clone())
        .flatten()
        .collect::<Vec<String>>()
        .iter()
        .map(|v| {
            (
                v.clone(),
                configuration
                    .iter()
                    .filter(|(_, m)| m.get_outputs().contains(&v))
                    .map(|(n, _)| n.clone())
                    .collect::<Vec<String>>(),
            )
        })
        .collect();

    // Records how often each grandchild wraps around
    let mut multiples: HashMap<String, (usize, usize)> = HashMap::new();

    println!("conjunction_inputs children: {:?}", conj_inputs_children);
    println!(
        "conjunction_inputs granchildren: {:?}",
        conj_inputs_granchildren
    );

    for (input, children) in &conj_inputs_children {
        for g in children {
            multiples.insert(g.clone(), (0, 0));
        }
    }
    let mut conj_children_emmitted_signals: HashMap<String, HashMap<String, PulseType>> =
        HashMap::new();

    for (input, children) in &conj_inputs_children {
        for child in children {
            conj_children_emmitted_signals.insert(child.to_string(), HashMap::new());
            for g in conj_inputs_granchildren.get(child).unwrap() {
                conj_children_emmitted_signals
                    .get_mut(child)
                    .unwrap()
                    .insert(g.clone(), PulseType::Low);
            }
        }
    }

    let mut conj_children_emmitted_signals: HashMap<String, HashMap<String, PulseType>> =
        HashMap::new();

    for (input, children) in &conj_inputs_children {
        for child in children {
            conj_children_emmitted_signals.insert(child.to_string(), HashMap::new());
            for g in conj_inputs_granchildren.get(child).unwrap() {
                conj_children_emmitted_signals
                    .get_mut(child)
                    .unwrap()
                    .insert(g.clone(), PulseType::Low);
            }
        }
    }

    while multiples.iter().any(|(_k, v)| v.1 == 0) {
        // Press the button to initiate the broadcast
        let broadcaster = configuration.get_mut("broadcaster").unwrap();
        button_presses += 1;
        let emitted_pulses = broadcaster.receive_pulse(Pulse {
            pulse_type: PulseType::Low,
            sender: "".to_string(),
            receiver: "".to_string(),
        });

        let mut pulses: VecDeque<Pulse> = VecDeque::new();
        for pulse in emitted_pulses {
            pulses.push_back(pulse);
        }

        while !pulses.is_empty() {
            let current_pulse = pulses.pop_front().unwrap();

            if conj_inputs_children
                .iter()
                .any(|(i, ins)| ins.contains(&current_pulse.receiver))
            {
                *conj_children_emmitted_signals
                    .get_mut(&current_pulse.receiver)
                    .unwrap()
                    .get_mut(&current_pulse.sender)
                    .unwrap() = current_pulse.pulse_type;
            }

            let Some(receiver) = configuration.get_mut(&current_pulse.receiver) else {
                continue;
            };
            let emitted_pulses = receiver.receive_pulse(current_pulse);

            for pulse in emitted_pulses {
                pulses.push_back(pulse);
            }

            for (child, grandchildren) in &conj_children_emmitted_signals {
                if grandchildren.values().all(|v| v == &PulseType::High) {
                    let m = multiples.get_mut(child).unwrap();
                    if m == &(0, 0) {
                        m.0 = button_presses;
                    } else if let (first, 0) = *m {
                        m.1 = button_presses;
                    println!("Child {} wrapped around: ({}, {})", child, first, button_presses);
                    }
                }
            }
        }
    }

    let total_button_presses = conj_inputs_children
        .values()
        .map(|v| {
            multiples
                .iter()
                .filter(|(k, v1)| conj_inputs_granchildren[&v[0]].contains(k))
                .map(|(k, v)| v)
                .map(|v| v.1 - v.0)
                .fold(1, |acc, s| num::integer::lcm(acc, s))
        })
        .collect::<Vec<usize>>();

    println!("Partial lcms: {:?}", total_button_presses);
    println!(
        "Total lcm: {}",
        total_button_presses
            .iter()
            .fold(1, |acc, s| num::integer::lcm(acc, *s))
    );

    let total_button_presses = multiples
        .iter()
        .map(|(_k, v)| v.1)
        .fold(1, |acc, s| num::integer::lcm(acc, s));

    println!(
        "Minimum button presses required to activate the machine: {}",
        total_button_presses
    );
}
