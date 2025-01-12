use core::panic;
use std::io::{BufRead, BufReader};
use std::{env, path::PathBuf, process::Command};
use std::{fs, thread};

use clap::Parser;
use log::{debug, info};

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

    /// Specifies the location of the output file.
    /// This is optional, if set, the stdout and stderr will be written to the specified
    /// file.
    /// where day and year are specified using the args above.
    #[arg(short, long, value_name = "file")]
    output_file: Option<String>,

    /// Rebuilds the target solution before running it.
    #[arg(short, long, value_name = "boolean flag", default_value = "false")]
    build: bool,
}

fn main() {
    env_logger::init();

    let cli = Cli::parse();

    log_cli_opitons(&cli);
    let maybe_command = assemble_command_and_args(&cli);

    let Some(command_and_args) = maybe_command else {
        panic!("Unable to assemble the run command for args: {:?}", cli);
    };

    info!("Assembled run command: {}", command_and_args.join(" "));

    let mut shell = Command::new("sh");
    let command = shell
        .stdout(std::process::Stdio::piped())
        .stderr(std::process::Stdio::piped())
        .arg(command_and_args[0].clone())
        .arg(command_and_args[1].clone())
        .arg(command_and_args[2].clone())
        .arg(command_and_args[3].clone());

    if cli.build {
        let mut shell = Command::new("sh");
        let build_command = shell
            .stdout(std::process::Stdio::piped())
            .stderr(std::process::Stdio::piped())
            .arg(enrich_script_name_with_path(&cli, "build.sh"))
            .arg(cli.language.clone());
        execute_command_stream_and_capture_output(build_command, None);
    }

    execute_command_stream_and_capture_output(command, cli.output_file);
}

fn execute_command_stream_and_capture_output(command: &mut Command, output_file: Option<String>) {
    let mut child = command.spawn().unwrap();

    // First take the stdout and stderr streams of the child.
    let child_stdout = child
        .stdout
        .take()
        .expect("Internal error, could not take stdout");
    let child_stderr = child
        .stderr
        .take()
        .expect("Internal error, could not take stderr");

    // Initialize message passing channels to pipe stdout to the receiver thread.
    let (stdout_tx, stdout_rx) = std::sync::mpsc::channel();
    let (stderr_tx, stderr_rx) = std::sync::mpsc::channel();

    // Create threads to capture output, stream it to the console and send to the receiver.
    let stdout_thread = thread::spawn(move || {
        let stdout_lines = BufReader::new(child_stdout).lines();
        for line in stdout_lines {
            let line = line.unwrap();
            println!("{}", line);
            stdout_tx.send(line).unwrap();
        }
    });

    let stderr_thread = thread::spawn(move || {
        let stderr_lines = BufReader::new(child_stderr).lines();
        for line in stderr_lines {
            let line = line.unwrap();
            eprintln!("{}", line);
            stderr_tx.send(line).unwrap();
        }
    });

    let status = child
        .wait()
        .expect("Internal error, failed to wait on child");

    debug!("Child process exited with status: {}", status);

    stdout_thread.join().unwrap();
    stderr_thread.join().unwrap();

    let stdout = stdout_rx.into_iter().collect::<Vec<String>>().join("\n");
    let stderr = stderr_rx.into_iter().collect::<Vec<String>>().join("\n");

    if let Some(file) = output_file {
        fs::write(file, format!("{}\n\n{}", stdout, stderr)).unwrap();
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

fn assemble_command_and_args(cli: &Cli) -> Option<Vec<String>> {
    let script_path = enrich_script_name_with_path(cli, "run.sh");
    let command = script_path.to_str()?;

    let input_file = if let Some(input_file) = cli.input_file.as_deref() {
        input_file
    } else {
        &format!(
            "{}/{}/input-files/day-{}-puzzle-input",
            env::current_dir().unwrap().to_str().unwrap(),
            cli.year,
            cli.day
        )
    };
    Some(vec![
        command.to_string(),
        cli.day.to_string(),
        cli.part.to_string(),
        input_file.to_string(),
    ])
}

/// Appends the correct directory to the script name based on the aoc year and
/// selected language.
fn enrich_script_name_with_path(cli: &Cli, script: &str) -> PathBuf {
    let mut script_path = env::current_dir().unwrap();
    script_path = script_path.join(PathBuf::from(&cli.year.to_string()));
    script_path = script_path.join(PathBuf::from(&cli.language));
    script_path = script_path.join(script);
    script_path
}
