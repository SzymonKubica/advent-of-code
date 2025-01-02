use num::abs;

#[derive(Copy, Clone, Eq, PartialEq, Debug, Hash)]
enum Direction {
    Up,
    Down,
    Left,
    Right,
}

impl From<char> for Direction {
    fn from(value: char) -> Self {
        match value {
            'U' => Direction::Up,
            'D' => Direction::Down,
            'L' => Direction::Left,
            'R' => Direction::Right,
            _ => panic!("invalid direction"),
        }
    }
}

impl ToString for Direction {
    fn to_string(&self) -> String {
        match self {
            Direction::Up => 'U'.to_string(),
            Direction::Down => 'D'.to_string(),
            Direction::Left => 'L'.to_string(),
            Direction::Right => 'R'.to_string(),
        }
    }
}

impl Direction {
    pub fn translate(&self, point: (i64, i64)) -> (i64, i64) {
        match self {
            Direction::Up => (point.0, point.1 - 1),
            Direction::Down => (point.0, point.1 + 1),
            Direction::Left => (point.0 - 1, point.1),
            Direction::Right => (point.0 + 1, point.1),
        }
    }

    fn translate_by(&self, point: (i64, i64), length: i64) -> (i64, i64) {
        match self {
            Direction::Up => (point.0, point.1 - length),
            Direction::Down => (point.0, point.1 + length),
            Direction::Left => (point.0 - length, point.1),
            Direction::Right => (point.0 + length, point.1),
        }
    }
}

struct Instruction {
    direction: Direction,
    length: usize,
    color: String,
}

impl Instruction {
    fn recover_from_hex(&self) -> Self {
        let length = self.color.len();
        let direction = match self.color.chars().last().unwrap() {
            '0' => Direction::Right,
            '1' => Direction::Down,
            '2' => Direction::Left,
            '3' => Direction::Up,
            _ => panic!("invalid direction char"),
        };
        let length = usize::from_str_radix(&self.color[0..(length - 1)], 16).unwrap();
        Instruction {
            direction,
            length,
            color: "".to_string(),
        }
    }
}

impl From<&str> for Instruction {
    fn from(value: &str) -> Self {
        let parts = value.trim().split(" ").collect::<Vec<&str>>();
        let direction = Direction::from(parts[0].chars().nth(0).unwrap());
        let length = parts[1].parse::<usize>().unwrap();
        let color = parts[2][2..(parts[2].len() - 1)].to_string();

        Instruction {
            direction,
            length,
            color,
        }
    }
}

impl ToString for Instruction {
    fn to_string(&self) -> String {
        format!(
            "{} {} ({})",
            self.direction.to_string(),
            self.length,
            self.color
        )
    }
}

pub fn part1(input_file: &str) {
    let instructions = read_instructions(input_file);

    for instruction in &instructions {
        println!("{}", instruction.to_string());
    }

    let width = calculate_trench_width(&instructions);
    let height = calculate_trench_height(&instructions);

    println!("Trench dimensions: {} x {}", width, height);

    // We trace out on a sparse grid so that it is possible to detect cells
    // that aren't on the inside even though they aren't reachable from the outside
    // i.e. the ones that are enclosed by the outer side of the loop.
    let mut trench = vec![vec!['.'; 4 * width]; 4 * height];
    let mut position = (2 * width as i64, 2 * height as i64);

    for instruction in &instructions {
        for _ in 0..(2 * instruction.length) {
            position = instruction.direction.translate(position);
            dig_hole_at(position, &mut trench);
        }
    }

    show_grid(&trench);
    // Now perform the usual flooding to find the cells that are on the inside.

    // First mark the cells at the edges as outside:
    for y in 0..(4 * height) {
        if trench[y][0] != '#' {
            trench[y][0] = 'O';
        }
        if trench[y][4 * width - 1] != '#' {
            trench[y][4 * width - 1] = 'O';
        }
    }

    for x in 0..(4 * width) {
        if trench[0][x] != '#' {
            trench[0][x] = 'O';
        }
        if trench[4 * height - 1][x] != '#' {
            trench[4 * height - 1][x] = 'O';
        }
    }

    let mut converged = false;
    while !converged {
        converged = true;
        for y in 1..(4 * height - 1) {
            for x in 1..(4 * width - 1) {
                if trench[y][x] == '.' && has_outside_neighbours((x, y), &trench) {
                    trench[y][x] = 'O';
                    converged = false;
                }
            }
        }
    }

    // now count the inisde ones.
    let mut dug_holes_count = 0;
    for y in (0..4 * height).step_by(2) {
        for x in (0..(4 * width)).step_by(2) {
            if trench[y][x] == '.' || trench[y][x] == '#' {
                dug_holes_count += 1;
            }
        }
    }
    show_grid(&trench);

    println!("Area of the trench: {}", dug_holes_count);
}

pub fn part2(input_file: &str) {
    let instructions = read_instructions(input_file);
    for instruction in &instructions {
        println!("{}", instruction.to_string());
    }

    let instructions = instructions
        .iter()
        .map(|i| i.recover_from_hex())
        .collect::<Vec<Instruction>>();

    let mut coordinates: Vec<(i64, i64)> = vec![];

    let mut position = (0, 0);
    coordinates.push(position);
    let mut boundary: i64 = 0;

    for instruction in instructions {
        position = instruction
            .direction
            .translate_by(position, instruction.length as i64);
        coordinates.push(position);
        boundary += instruction.length as i64;
    }

    let mut area = 0;
    for i in 0..(coordinates.len() - 1) {
        area += get_determinant(coordinates[i], coordinates[i + 1]);
    }
    area += get_determinant(*coordinates.last().unwrap(), coordinates[0]);
    area = area / 2;

    println!("Total enclosed area: {}", area);
    // Now we need to restore the total area using Pick's theorem:
    // Area = interior_points + boundary_points / 2 - 1
    // We are looking for interior_points + boundary_points
    println!("Total integer area: {}", area + 1 + boundary / 2);
}

fn get_determinant(point1: (i64, i64), point2: (i64, i64)) -> i64 {
    point1.0 * point2.1 - point1.1 * point2.0
}
pub fn part2_overcomplicated(input_file: &str) {
    let instructions = read_instructions(input_file);

    for instruction in &instructions {
        println!("{}", instruction.to_string());
    }

    let width = calculate_trench_width(&instructions);
    let height = calculate_trench_height(&instructions);

    println!("Trench dimensions: {} x {}", width, height);

    let mut trench = vec![vec!['.'; 4 * width]; 4 * height];
    let mut position = (2 * width as i64, 2 * height as i64);

    let mut prev_direction = instructions[0].direction;
    for instruction in &instructions {
        for _ in 0..(2 * instruction.length) {
            dig_hole_at_direction(position, &mut trench, instruction.direction, prev_direction);
            position = instruction.direction.translate(position);
            prev_direction = instruction.direction;
        }
    }

    // Now a cell is on the inside if the number of times the rays from that
    // cell to the borders cross the loop is odd.
    //
    for y in 1..(4 * height - 1) {
        'middle: for x in 1..(4 * width - 1) {
            for direction in [
                Direction::Up,
                Direction::Down,
                Direction::Right,
                Direction::Left,
            ] {
                let mut intersections = 0;
                let mut current_position = (x as i64, y as i64);
                current_position = direction.translate(current_position);
                let mut crossed_borders: Vec<char> = vec![];
                while current_position.0 >= 0
                    && current_position.1 >= 0
                    && current_position.0 < 4 * width as i64
                    && current_position.1 < 4 * height as i64
                {
                    let curr_char =
                        trench[current_position.1 as usize][current_position.0 as usize];
                    match direction {
                        Direction::Up | Direction::Down => {
                            if ['-', 'J', 'L', 'F', '7'].contains(&curr_char) {
                                crossed_borders.push(curr_char);
                            }
                        }
                        Direction::Right | Direction::Left => {
                            if ['|', 'J', 'L', 'F', '7'].contains(&curr_char) {
                                crossed_borders.push(curr_char);
                            }
                        }
                    }

                    current_position = direction.translate(current_position);
                }

                // Now we count the actual intersections:
                let mut i = 0;
                while i < crossed_borders.len() {
                    match crossed_borders[i] {
                        '-' | '|' => {
                            i += 1;
                            intersections += 1;
                        }
                        'J' => {
                            if i < crossed_borders.len() - 1 && crossed_borders[i + 1] == 'F' {
                                intersections += 1;
                                i += 2;
                            } else if i < crossed_borders.len() - 1 && crossed_borders[i + 1] == 'L'
                            {
                                intersections += 2;
                                i += 2;
                            } else if i < crossed_borders.len() - 1 && crossed_borders[i + 1] == '7'
                            {
                                intersections += 2;
                                i += 2;
                            } else {
                                i += 1;
                            }
                        }
                        'F' => {
                            if i < crossed_borders.len() - 1 && crossed_borders[i + 1] == 'J' {
                                intersections += 1;
                                i += 2;
                            } else if i < crossed_borders.len() - 1 && crossed_borders[i + 1] == '7'
                            {
                                intersections += 2;
                                i += 2;
                            } else if i < crossed_borders.len() - 1 && crossed_borders[i + 1] == 'L'
                            {
                                intersections += 2;
                                i += 2;
                            } else {
                                i += 1;
                            }
                        }
                        '7' => {
                            if i < crossed_borders.len() - 1 && crossed_borders[i + 1] == 'L' {
                                intersections += 1;
                                i += 2;
                            } else if i < crossed_borders.len() - 1 && crossed_borders[i + 1] == 'J'
                            {
                                intersections += 2;
                                i += 2;
                            } else if i < crossed_borders.len() - 1 && crossed_borders[i + 1] == 'F'
                            {
                                intersections += 2;
                                i += 2;
                            } else {
                                i += 1;
                            }
                        }
                        'L' => {
                            if i < crossed_borders.len() - 1 && crossed_borders[i + 1] == '7' {
                                intersections += 1;
                                i += 2;
                            } else if i < crossed_borders.len() - 1 && crossed_borders[i + 1] == 'J'
                            {
                                intersections += 2;
                                i += 2;
                            } else if i < crossed_borders.len() - 1 && crossed_borders[i + 1] == 'F'
                            {
                                intersections += 2;
                                i += 2;
                            } else {
                                i += 1;
                            }
                        }
                        _ => panic!("invalid char"),
                    }
                }
                if intersections % 2 == 0 {
                    // Even number of intersectios means that not on the inside
                    continue 'middle;
                }
            }
            trench[y][x] = 'I';
        }
    }

    show_grid(&trench);

    // now count the inisde ones.
    let mut dug_holes_count = 0;
    for y in (0..4 * height).step_by(2) {
        for x in (0..(4 * width)).step_by(2) {
            let curr_char = trench[y][x];
            if ['-', '|', 'J', 'L', 'F', '7'].contains(&curr_char) || curr_char == 'I' {
                dug_holes_count += 1;
            }
        }
    }
    println!("Area of the trench: {}", dug_holes_count);
}

fn has_outside_neighbours(point: (usize, usize), trench: &Vec<Vec<char>>) -> bool {
    let neighbours = vec![
        trench[point.1 - 1][point.0],
        trench[point.1 + 1][point.0],
        trench[point.1][point.0 - 1],
        trench[point.1][point.0 + 1],
    ];
    neighbours.iter().any(|c| *c == 'O')
}

fn dig_hole_at(point: (i64, i64), trench: &mut Vec<Vec<char>>) {
    trench[point.1 as usize][point.0 as usize] = '#';
}

fn dig_hole_at_direction(
    point: (i64, i64),
    trench: &mut Vec<Vec<char>>,
    direction: Direction,
    prev_direction: Direction,
) {
    let char = match (direction, prev_direction) {
        (Direction::Up, Direction::Up) => '|',
        (Direction::Right, Direction::Right) => '-',
        (Direction::Down, Direction::Down) => '|',
        (Direction::Left, Direction::Left) => '-',
        (Direction::Up, Direction::Right) => 'J',
        (Direction::Left, Direction::Down) => 'J',
        (Direction::Up, Direction::Left) => 'L',
        (Direction::Right, Direction::Down) => 'L',
        (Direction::Down, Direction::Left) => 'F',
        (Direction::Right, Direction::Up) => 'F',
        (Direction::Down, Direction::Right) => '7',
        (Direction::Left, Direction::Up) => '7',
        _ => panic!("invalid direction"),
    };
    trench[point.1 as usize][point.0 as usize] = char
}

fn show_grid(trench: &Vec<Vec<char>>) {
    for row in trench {
        println!("{}", row.iter().collect::<String>());
    }
}

fn read_instructions(input_file: &str) -> Vec<Instruction> {
    let contents = std::fs::read_to_string(input_file).expect("Unable to read the file.");
    contents
        .trim()
        .split("\n")
        .map(|l| Instruction::from(l))
        .collect()
}

fn calculate_trench_width(instructions: &Vec<Instruction>) -> usize {
    let mut max_displacement: i64 = 0;
    let mut displacement: i64 = 0;

    for instruction in instructions {
        match instruction.direction {
            Direction::Left => displacement -= instruction.length as i64,
            Direction::Right => displacement += instruction.length as i64,
            _ => {}
        }
        if abs(displacement) > max_displacement {
            max_displacement = abs(displacement);
        }
    }
    (max_displacement + 1).try_into().unwrap()
}

fn calculate_trench_height(instructions: &Vec<Instruction>) -> usize {
    let mut max_displacement: i64 = 0;
    let mut displacement: i64 = 0;

    for instruction in instructions {
        match instruction.direction {
            Direction::Up => displacement -= instruction.length as i64,
            Direction::Down => displacement += instruction.length as i64,
            _ => {}
        }
        if abs(displacement) > max_displacement {
            max_displacement = abs(displacement);
        }
    }
    (max_displacement + 1).try_into().unwrap()
}
