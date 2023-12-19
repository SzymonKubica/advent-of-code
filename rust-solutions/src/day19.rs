use std::{collections::HashMap, mem::ManuallyDrop, cmp::{min, max}};

struct Workflow {
    name: String,
    rules: Vec<Rule>,
}

impl ToString for Workflow {
    fn to_string(&self) -> String {
        format!(
            "{}{{{}}}",
            self.name,
            self.rules
                .iter()
                .map(|r| r.to_string())
                .collect::<Vec<String>>()
                .join(",")
        )
    }
}
// Example Workflow string: px{a<2006:qkq,m>2090:A,rfg}
impl From<&str> for Workflow {
    fn from(value: &str) -> Self {
        let parts = value.trim().split("{").collect::<Vec<&str>>();
        let name = parts[0][0..].to_string();
        let rules = parts[1][0..(parts[1].len() - 1)]
            .split(",")
            .map(|s| Rule::from(s))
            .collect::<Vec<Rule>>();

        Workflow { name, rules }
    }
}

struct Rule {
    tested_property: PartProperty,
    inequality: RuleInequalityType,
    condition_bound: u32,
    outcome: RuleApplicationOutcome,
    always_satisfied: bool,
}

impl ToString for Rule {
    fn to_string(&self) -> String {
        if self.always_satisfied {
            return self.outcome.to_string();
        }
        format!(
            "{}{}{}:{}",
            self.tested_property.to_string(),
            self.inequality.to_string(),
            self.condition_bound,
            self.outcome.to_string()
        )
    }
}

impl Rule {
    fn apply_rule(&self, part: &Part) -> Option<&RuleApplicationOutcome> {
        if self.always_satisfied {
            return Some(&self.outcome);
        }
        match self.inequality {
            RuleInequalityType::LessThan => {
                if part.map[&self.tested_property] < self.condition_bound {
                    Some(&self.outcome)
                } else {
                    None
                }
            }
            RuleInequalityType::GreaterThan => {
                if part.map[&self.tested_property] > self.condition_bound {
                    Some(&self.outcome)
                } else {
                    None
                }
            }
        }
    }
    // Applying the rule to a range returns the range that satisfies the rule
    // and thus will be taken to the next workflow or accepted/rejected. It also
    // yields the range that didn't meet the condition and should thus be
    // propagated to the next rule in the current workflow.
    fn apply_rule_ranged(&self, part: &mut RangedPart) -> (RangedPart, RuleApplicationOutcome, Option<RangedPart>) {
        if self.always_satisfied {
            return (part.clone(), self.outcome.clone(), None::<RangedPart>);
        }
        match self.inequality {
            RuleInequalityType::LessThan => {
                let mut not_taken_range = part.clone();
                // -+ 1 because the inequality is strict
                let new_upper_bound = min(part.map[&self.tested_property].1, self.condition_bound - 1);
                part.map.get_mut(&self.tested_property).unwrap().1 = new_upper_bound;

                // Now we need to assemble the other range
                // Its lower bound is now equal to the upper bound above
                // The upper bound stays the same and so in case of the
                // condition_bound being larger than the current upper bound,
                // the not taken range will be empty (ranging from higher to smaller number)
                not_taken_range.map.get_mut(&self.tested_property).unwrap().0 = self.condition_bound;

                (part.clone(), self.outcome.clone(), Some(not_taken_range))

            }
            RuleInequalityType::GreaterThan => {
                let mut not_taken_range = part.clone();
                let new_lower_bound = max(part.map[&self.tested_property].0, self.condition_bound + 1);
                part.map.get_mut(&self.tested_property).unwrap().0 = new_lower_bound;

                // Now we need to assemble the other range
                // Similar as above, now the upper bound of the not taken
                // range is the condition bound whereas the lower bound stays the same
                not_taken_range.map.get_mut(&self.tested_property).unwrap().1 = self.condition_bound;
                (part.clone(), self.outcome.clone(), Some(not_taken_range))
            }
        }
    }
}
// Example Rule string: a<2006:qkq
impl From<&str> for Rule {
    fn from(value: &str) -> Self {
        if !value.contains(":") {
            let rule_outcome = match value {
                "R" => RuleApplicationOutcome::from(FinalState::Rejected),
                "A" => RuleApplicationOutcome::from(FinalState::Accepted),
                _ => RuleApplicationOutcome::from(value.to_string()),
            };
            return Rule {
                tested_property: PartProperty::ExtremelyCoolLooking,
                inequality: RuleInequalityType::LessThan,
                condition_bound: 0,
                outcome: rule_outcome,
                always_satisfied: true,
            };
        }
        let parts = value.trim().split(":").collect::<Vec<&str>>();
        let condition_str = parts[0];

        let destination = parts[1];
        let rule_outcome = match destination {
            "R" => RuleApplicationOutcome::from(FinalState::Rejected),
            "A" => RuleApplicationOutcome::from(FinalState::Accepted),
            _ => RuleApplicationOutcome::from(destination.to_string()),
        };

        if condition_str.contains("<") {
            let condition_parts = condition_str.split("<").collect::<Vec<&str>>();
            let tested_property = PartProperty::from(condition_parts[0]);
            let inequality = RuleInequalityType::LessThan;
            let condition_bound = condition_parts[1].parse::<u32>().unwrap();

            Rule {
                tested_property,
                inequality,
                condition_bound,
                outcome: rule_outcome,
                always_satisfied: false,
            }
        } else {
            // Assuming well-formed input here it will contain ">"
            let condition_parts = condition_str.split(">").collect::<Vec<&str>>();
            let tested_property = PartProperty::from(condition_parts[0]);
            let inequality = RuleInequalityType::GreaterThan;
            let condition_bound = condition_parts[1].parse::<u32>().unwrap();
            Rule {
                tested_property,
                inequality,
                condition_bound,
                outcome: rule_outcome,
                always_satisfied: false,
            }
        }
    }
}

#[derive(Clone)]
struct RuleApplicationOutcome {
    final_state: Option<FinalState>,
    next_rule: Option<String>,
}

impl From<FinalState> for RuleApplicationOutcome {
    fn from(value: FinalState) -> Self {
        RuleApplicationOutcome {
            final_state: Some(value),
            next_rule: None,
        }
    }
}

impl From<String> for RuleApplicationOutcome {
    fn from(value: String) -> Self {
        RuleApplicationOutcome {
            final_state: None,
            next_rule: Some(value),
        }
    }
}

impl ToString for RuleApplicationOutcome {
    fn to_string(&self) -> String {
        if let Some(final_state) = &self.final_state {
            final_state.to_string()
        } else {
            self.next_rule.as_ref().unwrap().to_string()
        }
    }
}

#[derive(Copy, Clone, Eq, PartialEq)]
enum FinalState {
    Accepted,
    Rejected,
}

impl ToString for FinalState {
    fn to_string(&self) -> String {
        match self {
            FinalState::Accepted => "A".to_string(),
            FinalState::Rejected => "R".to_string(),
        }
    }
}

enum RuleInequalityType {
    LessThan,
    GreaterThan,
}

impl ToString for RuleInequalityType {
    fn to_string(&self) -> String {
        match self {
            RuleInequalityType::LessThan => "<".to_string(),
            RuleInequalityType::GreaterThan => ">".to_string(),
        }
    }
}

#[derive(Copy, Clone, Hash, Eq, PartialEq)]
enum PartProperty {
    ExtremelyCoolLooking,
    Musical,
    Aerodynamic,
    Shiny,
}

impl ToString for PartProperty {
    fn to_string(&self) -> String {
        match self {
            PartProperty::ExtremelyCoolLooking => "x".to_string(),
            PartProperty::Musical => "m".to_string(),
            PartProperty::Aerodynamic => "a".to_string(),
            PartProperty::Shiny => "s".to_string(),
        }
    }
}

impl From<&str> for PartProperty {
    fn from(value: &str) -> Self {
        match value {
            "x" => PartProperty::ExtremelyCoolLooking,
            "m" => PartProperty::Musical,
            "a" => PartProperty::Aerodynamic,
            "s" => PartProperty::Shiny,
            _ => panic!("Unexpected part property"),
        }
    }
}

struct Part {
    map: HashMap<PartProperty, u32>,
}


#[derive(Clone)]
struct RangedPart {
    map: HashMap<PartProperty, (u32, u32)>,
}

impl RangedPart {
    fn ranges_valid(&self) -> bool {
        self.map.iter().all(|(_k, v)| v.0 <= v.1)
    }
    fn count_combinations(&self) -> u64 {
       if !self.ranges_valid() {
           return 0;
       }
        self.map.iter().map(|(_k, v)| (v.1 - v.0 + 1) as u64).product()
    }
}

impl ToString for Part {
    fn to_string(&self) -> String {
        format!(
            "{{x={},m={},a={},s={}}}",
            self.map[&PartProperty::ExtremelyCoolLooking],
            self.map[&PartProperty::Musical],
            self.map[&PartProperty::Aerodynamic],
            self.map[&PartProperty::Shiny]
        )
    }
}

// Example Part string {x=787,m=2655,a=1222,s=2876}
impl From<&str> for Part {
    fn from(value: &str) -> Self {
        let parts = value.trim()[1..(value.len() - 1)]
            .split(",")
            .collect::<Vec<&str>>();
        assert_eq!(parts.len(), 4);

        let x = parts[0][2..].parse::<u32>().unwrap();
        let m = parts[1][2..].parse::<u32>().unwrap();
        let a = parts[2][2..].parse::<u32>().unwrap();
        let s = parts[3][2..].parse::<u32>().unwrap();

        let mut map: HashMap<PartProperty, u32> = HashMap::new();

        map.insert(PartProperty::ExtremelyCoolLooking, x);
        map.insert(PartProperty::Musical, m);
        map.insert(PartProperty::Aerodynamic, a);
        map.insert(PartProperty::Shiny, s);

        Part { map }
    }
}
fn read_workflows_and_parts(input_file: &str) -> (HashMap<String, Workflow>, Vec<Part>) {
    let contents = std::fs::read_to_string(input_file).unwrap();
    let parts = contents.split("\n\n").collect::<Vec<&str>>();

    let workflows = parts[0]
        .split("\n")
        .filter(|s| !s.is_empty())
        .map(|s| Workflow::from(s))
        .map(|w| (w.name.clone(), w))
        .collect::<HashMap<String, Workflow>>();

    let parts = parts[1]
        .split("\n")
        .filter(|s| !s.is_empty())
        .map(|s| Part::from(s))
        .collect::<Vec<Part>>();

    (workflows, parts)
}

pub fn part1(input_file: &str) {
    let (workflows, parts) = read_workflows_and_parts(input_file);

    for (name, workflow) in &workflows {
        println!("{}", workflow.to_string());
    }

    println!("\n");

    for part in &parts {
        println!("{}", part.to_string());
    }

    let mut accepted_parts: Vec<Part> = Vec::new();
    for part in parts {
        let mut current_workflow = workflows.get("in").unwrap();
        'outer: loop {
            for rule in &current_workflow.rules {
                if let Some(outcome) = rule.apply_rule(&part) {
                    if let Some(final_state) = &outcome.final_state {
                        match final_state {
                            FinalState::Accepted => {
                                accepted_parts.push(part);
                                break 'outer;
                            }
                            FinalState::Rejected => break 'outer,
                        }
                    } else {
                        current_workflow = workflows.get(&outcome.to_string()).unwrap();
                        break;
                    }
                }
            }
        }
    }

    println!("Accepted parts:");
    for part in &accepted_parts {
        println!("{}", part.to_string());
    }

    let total_rating_sum = accepted_parts.iter().map(|p| p.map.iter().map(|(k, v)| v).sum::<u32>()).sum::<u32>();
    println!("Total sum of ratings of accepted parts: {}", total_rating_sum);
}
pub fn part2(input_file: &str) {

    let (workflows, parts) = read_workflows_and_parts(input_file);

    for (name, workflow) in &workflows {
        println!("{}", workflow.to_string());
    }

    println!("\n");

    for part in &parts {
        println!("{}", part.to_string());
    }

    let mut ranged_part = RangedPart {
        map: HashMap::new(),
    };
    ranged_part.map.insert(PartProperty::ExtremelyCoolLooking, (1, 4000));
    ranged_part.map.insert(PartProperty::Musical, (1, 4000));
    ranged_part.map.insert(PartProperty::Aerodynamic, (1, 4000));
    ranged_part.map.insert(PartProperty::Shiny, (1, 4000));

    let mut accepted_count = 0;

    let initial_workflow = workflows.get("in").unwrap();
    let mut ranged_parts = vec![(ranged_part, initial_workflow)];

    while !ranged_parts.is_empty() {
        let (mut current_part, current_worflow) = ranged_parts.pop().unwrap();
        // if the current part has any of the ranges empty we want to continue.
        if !current_part.ranges_valid() {
            continue;
        }

        for rule in &current_worflow.rules {
            let (taken, outcome, not_taken) = rule.apply_rule_ranged(&mut current_part);
            if let Some(FinalState::Accepted) = outcome.final_state {
                // Count combinations of states and increment the accepted count
                accepted_count += taken.count_combinations()
            }
            if let Some(name) = outcome.next_rule {
                ranged_parts.push((taken, workflows.get(&name).unwrap()));
            }
            if let Some(part) = not_taken {
                current_part = part;
            } else {
                break;
            }
        }
    }

    println!("Total accepted combinations: {}", accepted_count);

}
