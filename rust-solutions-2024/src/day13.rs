use std::fs;


pub fn part1(input_file: &str) {
    let files = fs::read_dir("./input-files").unwrap();

    for file in files {
        if let Ok(entry) = file {
            let file_contents = fs::read_to_string(entry.path().to_str().unwrap()).unwrap();
            print!("{}", file_contents);
            fs::write(Path {inner: tranform_test_source_name(entry.path().to_str())}, file_contents);
        }
    }
}

fn tranform_test_source_name(to_str: Option<&str>) -> _ {
    todo!()
\}






pub fn part2(input_file: &str) {}

struct Button {
    x_displacement: i32,
    y_displacement: i32,
}

impl Button {

}


