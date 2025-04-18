use std::fs;

pub fn first_part(input_file: &str) {
    let input = fs::read_to_string(&input_file).unwrap();
    println!("{}", input);

    let buttons: Vec<Button> = input.split("\n\n").map(Button::from).collect();

    for button in buttons.iter() {
        println!("{:?}", button);
    }
}




pub fn second_part(input_file: &str) {}

#[derive(Debug)]
struct Button(i32, i32);

impl Button {
    fn new(x_displacement: i32, y_displacement: i32) -> Self {
        Self(x_displacement, y_displacement)
    }
}


impl From<&str> for Button {
    fn from(value: &str) -> Self {
        let parts: Vec<&str> = value.split(" ").collect();
        let x_displacement_str = parts.get(2).unwrap();
        let y_displacement_str = parts.get(3).unwrap();

        let x_displacement = x_displacement_str[2..].parse::<i32>().unwrap();
        let y_displacement = y_displacement_str[2..].parse::<i32>().unwrap();

        Self::new(x_displacement, y_displacement)
    }
}

