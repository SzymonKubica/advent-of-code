use std::{
    cmp::{max, min},
    collections::HashMap,
    fs,
};

#[derive(Debug)]
struct Card {
    pub card_number: usize,
    pub winning_numbers: Vec<i32>,
    pub card_numbers: Vec<i32>,
}

pub fn part1(input_file: &str) {
    let cards = read_cards(input_file);

    let total_cards_value = cards
        .iter()
        .map(|c| count_winning_numbers(&c.card_numbers, &c.winning_numbers))
        .map(|x| compute_score(x))
        .sum::<i32>();

    println!("Total cards value: {}", total_cards_value);
}

pub fn part2(input_file: &str) {
    let mut card_copies_counts: HashMap<usize, usize> = HashMap::new();
    let cards = read_cards(input_file);

    for card in &cards {
        card_copies_counts.insert(card.card_number, 1_usize);
    }

    for card in &cards {
        let winning_numbers = count_winning_numbers(&card.winning_numbers, &card.card_numbers);
        let card_count = card_copies_counts.get(&card.card_number).unwrap().clone();

        // If the card won 4 numbers, we receive one copy of the 4 subsequent cards
        // if we have multiple copies of that card, we multiply the won copies.
        for card_number in
            (card.card_number + 1)..=min(cards.len(), card.card_number + winning_numbers)
        {
            card_copies_counts.insert(
                card_number,
                card_copies_counts.get(&card_number).unwrap() + card_count,
            );
        }
    }

    println!("{:?}", card_copies_counts);
    let total_number_scratchcards: usize = card_copies_counts.iter().map(|e| e.1).sum();
    println!("{}", total_number_scratchcards);
}

fn compute_score(num_matches: usize) -> i32 {
    if num_matches == 0 {
        return 0;
    };
    2_i32.pow((num_matches - 1).try_into().unwrap())
}

fn count_winning_numbers(winning_numbers: &Vec<i32>, card_numbers: &Vec<i32>) -> usize {
    card_numbers
        .iter()
        .filter(|x| winning_numbers.contains(x))
        .count()
}

fn read_cards(input_file: &str) -> Vec<Card> {
    let lines = fs::read_to_string(&input_file).expect("Should have been able to read the file.");

    lines
        .split("\n")
        .filter(|s| !s.is_empty())
        .map(|l| parse_card(l))
        .collect::<Vec<Card>>()
}

// Example card: Card 5: 87 83 26 28 32 | 88 30 70 12 93 22 82 36
fn parse_card(line: &str) -> Card {
    let header_and_numbers = line.split(": ").collect::<Vec<&str>>();
    let (header, numbers) = (header_and_numbers[0], header_and_numbers[1]);

    let card_number: usize = header
        .split(" ")
        .filter_map(|s| {
            if !s.trim().is_empty() {
                Some(s.trim())
            } else {
                None
            }
        })
        .collect::<Vec<&str>>()[1]
        .parse()
        .unwrap();

    let split_numbers = numbers.split(" | ").collect::<Vec<&str>>();
    let (winning_numbers, card_numbers) = (
        split_numbers[0]
            .split(" ")
            .map(|s| s.parse())
            .filter_map(|x| if let Ok(val) = x { Some(val) } else { None })
            .collect::<Vec<i32>>(),
        split_numbers[1]
            .split(" ")
            .map(|s| s.parse())
            .filter_map(|x| if let Ok(val) = x { Some(val) } else { None })
            .collect::<Vec<i32>>(),
    );

    Card {
        card_number,
        winning_numbers,
        card_numbers,
    }
}
