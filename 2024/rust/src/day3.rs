use core::fmt;
use std::fs;

// xmul(2,4)%&mul[3,7]!@^do_not_mul(5,5)+mul(32,64]then(mul(11,8)mul(8,5))
pub fn part1(input_file: &str) {
    let input = fs::read_to_string(input_file).unwrap();
    let mut line = input.as_str();

    let parser: TokenParser = TokenParser::new(false);

    let mut tokens: Vec<Token> = vec![];
    while line.len() > 0 {
        let (token, tail) = parser.parse_next(&line);
        tokens.push(token);
        line = tail;
    }
    println!("Parsed tokens:");
    for token in &tokens {
        println!("{:?}", token);
    }
    let mut well_formed_expressions: Vec<Multiplication> = vec![];
    // A well-formed expression required 6 tokens
    while tokens.len() >= 6 {
        let maybe_expression = &tokens[..6];
        let Token::Mul = maybe_expression.get(0).unwrap() else {
            tokens.remove(0);
            continue;
        };
        let Token::Lparen = maybe_expression.get(1).unwrap() else {
            tokens.remove(0);
            continue;
        };
        let Token::Number(x) = maybe_expression.get(2).unwrap() else {
            tokens.remove(0);
            continue;
        };
        let Token::Comma = maybe_expression.get(3).unwrap() else {
            tokens.remove(0);
            continue;
        };
        let Token::Number(y) = maybe_expression.get(4).unwrap() else {
            tokens.remove(0);
            continue;
        };
        let Token::Rparen = maybe_expression.get(5).unwrap() else {
            tokens.remove(0);
            continue;
        };
        well_formed_expressions.push(Multiplication(*x, *y));
        tokens = tokens[6..].to_vec();
    }

    println!("Well-formed expressions:");
    for exp in &well_formed_expressions {
        println!("{}", exp);
    }

    let result: u64 = well_formed_expressions
        .into_iter()
        .map(|exp| exp.0 * exp.1)
        .sum();
    println!("Sum of all well-formed expressions: {}", result);
}

pub fn part2(input_file: &str) {
    let input = fs::read_to_string(input_file).unwrap();
    let mut line = input.as_str();

    let parser: TokenParser = TokenParser::new(true);

    let mut tokens: Vec<Token> = vec![];
    while line.len() > 0 {
        let (token, tail) = parser.parse_next(&line);
        tokens.push(token);
        line = tail;
    }
    println!("Parsed tokens:");
    for token in &tokens {
        println!("{:?}", token);
    }
    let mut well_formed_expressions: Vec<Multiplication> = vec![];
    let mut multiplication_enabled: bool = true;
    // A well-formed expression required 6 tokens
    while tokens.len() >= 6 {
        // First try to parse do/don't
        if let &[Token::Do, Token::Lparen, Token::Rparen] = &tokens[..3] {
            multiplication_enabled = true;
            tokens.remove(0);
            tokens.remove(0);
            tokens.remove(0);
            continue;
        }
        if let &[Token::DoNot, Token::Lparen, Token::Rparen] = &tokens[..3] {
            multiplication_enabled = false;
            tokens.remove(0);
            tokens.remove(0);
            tokens.remove(0);
            continue;
        }
        let maybe_expression = &tokens[..6];
        let Token::Mul = maybe_expression.get(0).unwrap() else {
            tokens.remove(0);
            continue;
        };
        let Token::Lparen = maybe_expression.get(1).unwrap() else {
            tokens.remove(0);
            continue;
        };
        let Token::Number(x) = maybe_expression.get(2).unwrap() else {
            tokens.remove(0);
            continue;
        };
        let Token::Comma = maybe_expression.get(3).unwrap() else {
            tokens.remove(0);
            continue;
        };
        let Token::Number(y) = maybe_expression.get(4).unwrap() else {
            tokens.remove(0);
            continue;
        };
        let Token::Rparen = maybe_expression.get(5).unwrap() else {
            tokens.remove(0);
            continue;
        };
        if multiplication_enabled {
            well_formed_expressions.push(Multiplication(*x, *y));
        }
        tokens = tokens[6..].to_vec();
    }

    println!("Well-formed expressions:");
    for exp in &well_formed_expressions {
        println!("{}", exp);
    }

    let result: u64 = well_formed_expressions
        .into_iter()
        .map(|exp| exp.0 * exp.1)
        .sum();
    println!("Sum of all well-formed expressions: {}", result);
}

struct TokenParser {
    allow_conditional_expressions: bool,
}

impl TokenParser {
    pub fn new(allow_conditional_expressions: bool) -> Self {
        TokenParser {
            allow_conditional_expressions,
        }
    }

    pub fn parse_next<'a>(&self, input: &'a str) -> (Token, &'a str) {
        assert!(input.len() > 0);
        let next_char = input.chars().nth(0).unwrap();

        if next_char == 'm' {
            return Self::parse_mul(input);
        } else if next_char == '(' {
            return (Token::Lparen, &input[1..]);
        } else if next_char == ')' {
            return (Token::Rparen, &input[1..]);
        } else if next_char == ',' {
            return (Token::Comma, &input[1..]);
        } else if next_char == 'd' && self.allow_conditional_expressions {
            return Self::parse_conditional(input);
        } else if next_char.is_digit(10) {
            return Self::parse_number(input);
        } else {
            return (Token::Nothing, &input[1..]);
        }
    }

    fn parse_number(input: &str) -> (Token, &str) {
        assert!(input.len() > 0 && input.chars().nth(0).unwrap().is_digit(10));
        let mut digits: Vec<char> = vec![];
        while digits.len() < 3
            && input.len() > digits.len()
            && input.chars().nth(digits.len()).unwrap().is_digit(10)
        {
            digits.push(input.chars().nth(digits.len()).unwrap())
        }
        let number = digits.iter().collect::<String>().parse::<u64>().unwrap();
        return (Token::Number(number), &input[digits.len()..]);
    }

    fn parse_mul<'a>(input: &'a str) -> (Token, &'a str) {
        if input.len() >= 3 && input[..3] == *"mul" {
            return (Token::Mul, &input[3..]);
        }
        return (Token::Nothing, &input[1..]);
    }

    fn parse_conditional(input: &str) -> (Token, &str) {
        if input.len() >= 5 && input[..5] == *"don't" {
            return (Token::DoNot, &input[5..]);
        } else if input.len() >= 2 && input[..2] == *"do" {
            return (Token::Do, &input[2..]);
        }
        return (Token::Nothing, &input[1..]);
    }
}

#[derive(Debug, Clone)]
enum Token {
    Mul,
    Lparen,
    Rparen,
    Number(u64),
    Comma,
    Nothing,
    Do,
    DoNot,
}

struct Multiplication(u64, u64);

impl fmt::Display for Multiplication {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        return f.write_str(format!("mul({}, {})", self.0, self.1).as_str());
    }
}
