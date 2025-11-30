#include <sstream>
#include <fstream>
#include <iostream>
#include "./utils.hpp"

std::vector<std::string> get_lines_from_file(std::string file_path)
{

        std::ifstream file(file_path);
        if (!file.is_open()) {
                throw std::runtime_error("Could not open file");
        }

        std::ostringstream ss;
        ss << file.rdbuf();
        file.close();

        std::string file_contents = ss.str();

        std::istringstream iss(file_contents);

        std::vector<std::string> lines;
        std::string line;
        while (std::getline(iss, line)) {
                lines.emplace_back(line);
        }

        return lines;
}
