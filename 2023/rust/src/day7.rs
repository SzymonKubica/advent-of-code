use std::{collections::HashMap, fs};

pub fn part1(input_file: &str) {
    let mut hands = read_hands(input_file);

    println!("Initial hands:");
    for hand in &hands {
        println!("{:?}", hand);
    }

    hands.sort();

    println!("Hands sorted ascending strength:");
    for hand in &hands {
        println!("{:?} {:?}", hand, hand.get_hand_type());
    }

    let mut total_winnings: usize = 0;
    for (i, hand) in hands.iter().enumerate() {
        total_winnings += (i + 1) * hand.bid;
    }

    println!("Total winnings: {}", total_winnings);
}

fn read_hands(input_file: &str) -> Vec<Hand> {
    let file_contents =
        fs::read_to_string(&input_file).expect("Should have been able to read the file");

    let lines = file_contents
        .split("\n")
        .filter(|s| !s.is_empty())
        .collect::<Vec<&str>>();

    lines.iter().map(|l| parse_hand(l)).collect::<Vec<Hand>>()
}

fn parse_hand(line: &str) -> Hand {
    let split = line.split(" ").collect::<Vec<&str>>();
    let (hand_str, bid) = (split[0], split[1].parse::<usize>().unwrap());

    let cards = hand_str
        .chars()
        .map(|c| match c {
            '2' => Card::Two,
            '3' => Card::Three,
            '4' => Card::Four,
            '5' => Card::Five,
            '6' => Card::Six,
            '7' => Card::Seven,
            '8' => Card::Eight,
            '9' => Card::Nine,
            'T' => Card::Ten,
            'J' => Card::Jack,
            'Q' => Card::Queen,
            'K' => Card::King,
            'A' => Card::Ace,
            _ => panic!(),
        })
        .collect::<Vec<Card>>();

    Hand { cards, bid }
}

#[derive(Hash, PartialEq, Eq, Debug, PartialOrd, Ord)]
enum Card {
    Two,
    Three,
    Four,
    Five,
    Six,
    Seven,
    Eight,
    Nine,
    Ten,
    Jack,
    Queen,
    King,
    Ace,
}

#[derive(PartialEq, Eq, Debug, PartialOrd, Ord)]
enum HandType {
    HighCard,
    OnePair,
    TwoPairs,
    ThreeOfAKind,
    FullHouse,
    FourOfAKind,
    FiveOfAKind,
}

#[derive(PartialEq, Eq, Debug)]
struct Hand {
    cards: Vec<Card>,
    bid: usize,
}

impl Ord for Hand {
    fn cmp(&self, other: &Self) -> std::cmp::Ordering {
        // All hands can be compared between each other
        self.partial_cmp(other).unwrap()
    }
}

impl PartialOrd for Hand {
    fn partial_cmp(&self, other: &Self) -> Option<std::cmp::Ordering> {
        // If card sets are the same, hands are equal
        match self.cards.partial_cmp(&other.cards) {
            Some(core::cmp::Ordering::Equal) => return Some(core::cmp::Ordering::Equal),
            _ => {}
        }
        let self_type = self.get_hand_type();
        let other_type = other.get_hand_type();

        // First compare the hand types
        match self_type.cmp(&other_type) {
            std::cmp::Ordering::Equal => {}
            ord => return Some(ord),
        }

        // If hand types are equal iterate over individual cards and compare them
        for (i, card) in self.cards.iter().enumerate() {
            match card.cmp(&other.cards[i]) {
                std::cmp::Ordering::Equal => {}
                ord => return Some(ord),
            }
        }

        self.bid.partial_cmp(&other.bid)
    }
}

impl Hand {
    pub fn get_hand_type(&self) -> HandType {
        let card_counts: HashMap<&Card, i32> =
            self.cards.iter().fold(HashMap::new(), |mut acc, card| {
                *acc.entry(card).or_insert(0) += 1;
                acc
            });

        let occurrences = card_counts
            .values()
            .map(|x| x.clone())
            .collect::<Vec<i32>>();

        if occurrences.contains(&5) {
            return HandType::FiveOfAKind;
        }
        if occurrences.contains(&4) {
            return HandType::FourOfAKind;
        }
        if occurrences.contains(&3) && occurrences.contains(&2) {
            return HandType::FullHouse;
        }
        if occurrences.contains(&3) {
            return HandType::ThreeOfAKind;
        }
        if occurrences.contains(&2) {
            if occurrences.iter().filter(|x| x == &&2).count() == 2 {
                return HandType::TwoPairs;
            }
            return HandType::OnePair;
        }

        HandType::HighCard
    }
}

// Part 2 requires different logic for ordering hands and so it is done here
// to avoid confusion with the duplication.
// TODO: learn generics to avoid duplication.
pub fn part2(input_file: &str) {
    let mut hands = read_hands_with_joker(input_file);

    println!("Initial hands:");
    for hand in &hands {
        println!("{:?}", hand);
    }

    hands.sort();

    println!("Hands sorted ascending strength:");
    for hand in &hands {
        println!("{:?} {:?}", hand, hand.get_hand_type());
    }

    let mut total_winnings: usize = 0;
    for (i, hand) in hands.iter().enumerate() {
        total_winnings += (i + 1) * hand.bid;
    }

    println!("Total winnings: {}", total_winnings);
}

fn read_hands_with_joker(input_file: &str) -> Vec<HandWithJoker> {
    let file_contents =
        fs::read_to_string(&input_file).expect("Should have been able to read the file");

    let lines = file_contents
        .split("\n")
        .filter(|s| !s.is_empty())
        .collect::<Vec<&str>>();

    lines
        .iter()
        .map(|l| parse_hand_with_joker(l))
        .collect::<Vec<HandWithJoker>>()
}

fn parse_hand_with_joker(line: &str) -> HandWithJoker {
    let split = line.split(" ").collect::<Vec<&str>>();
    let (hand_str, bid) = (split[0], split[1].parse::<usize>().unwrap());

    let cards = hand_str
        .chars()
        .map(|c| match c {
            '2' => CardWithJoker::Two,
            '3' => CardWithJoker::Three,
            '4' => CardWithJoker::Four,
            '5' => CardWithJoker::Five,
            '6' => CardWithJoker::Six,
            '7' => CardWithJoker::Seven,
            '8' => CardWithJoker::Eight,
            '9' => CardWithJoker::Nine,
            'T' => CardWithJoker::Ten,
            'J' => CardWithJoker::Joker,
            'Q' => CardWithJoker::Queen,
            'K' => CardWithJoker::King,
            'A' => CardWithJoker::Ace,
            _ => panic!(),
        })
        .collect::<Vec<CardWithJoker>>();

    HandWithJoker { cards, bid }
}

#[derive(Hash, PartialEq, Eq, Debug, PartialOrd, Ord)]
enum CardWithJoker {
    Joker,
    Two,
    Three,
    Four,
    Five,
    Six,
    Seven,
    Eight,
    Nine,
    Ten,
    Queen,
    King,
    Ace,
}

#[derive(PartialEq, Eq, Debug)]
struct HandWithJoker {
    cards: Vec<CardWithJoker>,
    bid: usize,
}

impl Ord for HandWithJoker {
    fn cmp(&self, other: &Self) -> std::cmp::Ordering {
        // All hands can be compared between each other
        self.partial_cmp(other).unwrap()
    }
}

impl PartialOrd for HandWithJoker {
    fn partial_cmp(&self, other: &Self) -> Option<std::cmp::Ordering> {
        // If card sets are the same, hands are equal
        match self.cards.partial_cmp(&other.cards) {
            Some(core::cmp::Ordering::Equal) => return Some(core::cmp::Ordering::Equal),
            _ => {}
        }
        let self_type = self.get_hand_type();
        let other_type = other.get_hand_type();

        // First compare the hand types
        match self_type.cmp(&other_type) {
            std::cmp::Ordering::Equal => {}
            ord => return Some(ord),
        }

        // If hand types are equal iterate over individual cards and compare them
        for (i, card) in self.cards.iter().enumerate() {
            match card.cmp(&other.cards[i]) {
                std::cmp::Ordering::Equal => {}
                ord => return Some(ord),
            }
        }

        self.bid.partial_cmp(&other.bid)
    }
}

impl HandWithJoker {
    pub fn get_hand_type(&self) -> HandType {
        let card_counts: HashMap<&CardWithJoker, i32> =
            self.cards.iter().fold(HashMap::new(), |mut acc, card| {
                *acc.entry(card).or_insert(0) += 1;
                acc
            });

        let jokers = card_counts.get(&CardWithJoker::Joker);
        if let None = jokers {
            let occurrences = card_counts
                .values()
                .map(|x| x.clone())
                .collect::<Vec<i32>>();

            if occurrences.contains(&5) {
                return HandType::FiveOfAKind;
            }
            if occurrences.contains(&4) {
                return HandType::FourOfAKind;
            }
            if occurrences.contains(&3) && occurrences.contains(&2) {
                return HandType::FullHouse;
            }
            if occurrences.contains(&3) {
                return HandType::ThreeOfAKind;
            }
            if occurrences.contains(&2) {
                if occurrences.iter().filter(|x| x == &&2).count() == 2 {
                    return HandType::TwoPairs;
                }
                return HandType::OnePair;
            }

            return HandType::HighCard;
        } else {
            let joker_count = jokers.unwrap();
            if joker_count == &5 {
                return HandType::FiveOfAKind;
            }
            let occurrences = card_counts
                .iter()
                .filter(|e| e.0 != &&CardWithJoker::Joker)
                .map(|e| e.1.clone())
                .collect::<Vec<i32>>();

            let maximum_duplicates = joker_count + occurrences.iter().max().unwrap();

            match maximum_duplicates {
                5 => HandType::FiveOfAKind,
                4 => HandType::FourOfAKind,
                3 => {
                    if occurrences.iter().filter(|x| x == &&2).count() == 2 {
                        return HandType::FullHouse;
                    }
                    return HandType::ThreeOfAKind;
                }
                2 => HandType::OnePair,
                _ => panic!(),
            }
        }
    }
}
