use core::fmt;
use std::fs;

pub fn first_part(input_file: &str) {
    let word_search: WordSearch = read_word_search(input_file);
    println!("{}", word_search);
    let mut total_occurrences: usize = 0;

    println!("Checking rows");
    total_occurrences += search_horizontal(&word_search);
    println!("Checking columns");
    total_occurrences += search_vertical(&word_search);
    println!("Checking left-sloping diagonals");
    total_occurrences += search_diagonal(&word_search);
    println!("Checking right-sloping diagonals");
    total_occurrences += search_diagonal(&rotate(&word_search));
    println!("Total occurrences of 'XMAS': {}", total_occurrences);
}

fn rotate(word_search: &WordSearch) -> WordSearch {
    let height = word_search.0.len();
    let width = word_search.0.get(0).unwrap().len();

    let mut output_vec = vec![];
    for x in (0..width).rev() {
        let mut column = vec![];
        for y in 0..height {
            column.push(*word_search.0.get(y).unwrap().get(x).unwrap());
        }
        output_vec.push(column);
    }

    WordSearch(output_vec)
}

fn search_horizontal(word_search: &WordSearch) -> usize {
    word_search.0.iter().map(|r| search_row(r)).sum()
}

fn search_vertical(word_search: &WordSearch) -> usize {
    let mut cols: Vec<Vec<char>> = vec![];
    for i in 0..word_search.0.len() {
        cols.push(word_search.0.iter().map(|r| *r.get(i).unwrap()).collect())
    }

    cols.iter().map(|c| search_row(c)).sum()
}

fn search_diagonal(word_search: &WordSearch) -> usize {
    let height = word_search.0.len();
    let width = word_search.0.get(0).unwrap().len();

    let mut diagonal_total = 0;
    // first create left-sloping diagonals
    // first diagonal traces starting at the top edge including large diagonal
    for i in 0..width {
        let mut chars = vec![];
        for j in 0..=i {
            if i - j < height {
                chars.push(*word_search.0.get(i - j).unwrap().get(j).unwrap())
            }
        }
        diagonal_total += search_row(&chars);
    }
    // then diagonal traces starting at the right edge excluding the large
    // diagonal
    for i in 1..height {
        let mut chars = vec![];
        for j in 0..height - i {
            if i + j < height && width - j - 1 > 0 {
                chars.push(
                    *word_search
                        .0
                        .get(i + j)
                        .unwrap()
                        .get(width - j - 1)
                        .unwrap(),
                )
            }
        }
        diagonal_total += search_row(&chars);
    }
    diagonal_total
}

fn search_row(row: &[char]) -> usize {
    println!("Checking row: {:?}", row);
    if row.len() < 4 {
        return 0;
    }

    let mut occurrences = 0;
    for i in 0..(row.len() - 3) {
        if let ['X', 'M', 'A', 'S'] = row[i..(i + 4)] {
            occurrences += 1;
        }
        if let ['S', 'A', 'M', 'X'] = row[i..(i + 4)] {
            occurrences += 1;
        }
    }
    println!("Occurrences found: {}", occurrences);
    occurrences
}

pub fn second_part(input_file: &str) {
    let word_search: WordSearch = read_word_search(input_file);
    println!("{}", word_search);
    let mut total_occurrences: usize = 0;

    let grid = word_search.0;

    for i in 0..grid.len() - 2 {
        for j in 0..grid[0].len() - 2 {
            let window = grid[i..i + 3]
                .iter()
                .map(|r| r[j..j + 3].to_vec())
                .collect();
            if is_x_mas(&window) {
                total_occurrences += 1;
            }
        }
    }
    println!("Total occurrences of 'X-MAS': {}", total_occurrences);
}

fn is_x_mas(window: &Vec<Vec<char>>) -> bool {
    let mask = vec![
        vec!['M', '.', 'S'],
        vec!['.', 'A', '.'],
        vec!['M', '.', 'S'],
    ];

    matches(window, &mask)
        || matches(window, &rotate_vec(&mask))
        || matches(window, &rotate_vec(&rotate_vec(&mask)))
        || matches(window, &rotate_vec(&rotate_vec(&rotate_vec(&mask))))
}

fn print_window(window: &Vec<Vec<char>>) {
    for row in window {
        println!("{}", row.iter().collect::<String>());
    }
}

fn matches(window: &Vec<Vec<char>>, mask: &Vec<Vec<char>>) -> bool {
    println!("Checking window: ");
    print_window(&window);
    println!("Against mask: ");
    print_window(&mask);

    window[0][0] == mask[0][0]
        && window[1][1] == mask[1][1]
        && window[2][2] == mask[2][2]
        && window[2][0] == mask[2][0]
        && window[0][2] == mask[0][2]
}

fn rotate_vec(v: &Vec<Vec<char>>) -> Vec<Vec<char>> {
    let height = v.len();
    let width = v[0].len();

    let mut output_vec = vec![];
    for x in (0..width).rev() {
        let mut column = vec![];
        for y in 0..height {
            column.push(v[y][x]);
        }
        output_vec.push(column);
    }

    output_vec
}

fn read_word_search(input_file: &str) -> WordSearch {
    let file_content = fs::read_to_string(input_file).unwrap();
    let lines = file_content.lines();
    return WordSearch(lines.into_iter().map(|l| l.chars().collect()).collect());
}

struct WordSearch(Vec<Vec<char>>);

impl fmt::Display for WordSearch {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.write_str(
            &self
                .0
                .iter()
                .map(|v| v.iter().collect::<String>())
                .collect::<Vec<String>>()
                .join("\n"),
        )
    }
}
