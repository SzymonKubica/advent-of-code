use core::panic;
use std::{
    env,
    os::unix::process::CommandExt,
    path::{Path, PathBuf},
    process::Command,
};

use clap::Parser;

#[derive(Parser, Debug)]
#[command(version, about, long_about = None)]
struct Cli {
    /// Selects the year of the AoC edition
    #[arg(short, long, value_name = "number")]
    year: u32,

    /// Selects which day to run
    #[arg(short, long, value_name = "number")]
    day: u32,

    /// Selects which part of the puzzle to execute
    #[arg(short, long, value_name = "number")]
    part: u32,

    /// Language which needs to be used to run the solution
    #[arg(short, long, value_name = "language name")]
    language: String,

    /// Specifies the location of the input file.
    /// This is optional, defaults to <year>/input-files/day-<day>-puzzle-input
    /// where day and year are specified using the args above.
    #[arg(short, long, value_name = "file")]
    input_file: Option<String>,
    // TODO: think about a subcommand for benchmarking
}

fn main() {
    let cli = Cli::parse();

    log_cli_opitons(&cli);
    let command_str = assemble_command(&cli);

    let Some((command, args)) = command_str else {
        panic!("Unable to assemble the run command for args: {:?}", cli);
    };

    println!("Assembled run command: {}", command);
    let output = Command::new("sh")
        .arg(command)
        .arg(args[0].clone())
        .arg(args[1].clone())
        .arg(args[2].clone())
        .output();
    if let Ok(output) = output {
        println!("{}", String::from_utf8(output.stdout).unwrap());
    } else {
        panic!(
            "Script didn't execute successfully:\n {}",
            output.err().unwrap()
        );
    }
}

fn log_cli_opitons(cli: &Cli) {
    let part_name: &str = if cli.part == 1 { "first" } else { "second" };
    println!(
        "Executing AoC {} solution for day {}, {} part",
        cli.year, cli.day, part_name
    );
    println!("Using language: {}", cli.language);

    if let Some(input_file) = cli.input_file.as_deref() {
        println!("Reading input from file {}", input_file);
    }
}

fn assemble_command(cli: &Cli) -> Option<(String, Vec<String>)> {
    let mut script_path = env::current_dir().unwrap();
    script_path = script_path.join(PathBuf::from(&cli.year.to_string()));
    script_path = script_path.join(PathBuf::from(&cli.language));
    script_path = script_path.join("run.sh");
    let Some(command) = script_path.to_str() else {
        return None;
    };

    let input_file = if let Some(input_file) = cli.input_file.as_deref() {
        input_file
    } else {
        &format!("{}/input-files/day-{}-puzzle-input", cli.year, cli.day)
    };
    Some((
        command.to_string(),
        vec![
            cli.day.to_string(),
            cli.part.to_string(),
            input_file.to_string(),
        ],
    ))
}
