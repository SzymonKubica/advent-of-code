cmake_minimum_required(VERSION 3.10)

project(AdventOfCode
  VERSION 1.0)

# This configures the language version required by the project.
set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED True)

# This recursively grabs all .cpp files in the specified directories.
# It allows for adding new source files without needing to modify this CMakeLists.txt.
file(GLOB_RECURSE SOURCES CONFIGURE_DEPENDS ${CMAKE_SOURCE_DIR}/src/*.cpp)

set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

if(CMAKE_BUILD_TYPE STREQUAL "Debug")
    add_compile_definitions(DEBUG_BUILD)
endif()

find_package(Z3 REQUIRED)

add_executable(advent-of-code
  ${SOURCES}
  main.cpp)

target_include_directories(advent-of-code PUBLIC
  "${PROJECT_BINARY_DIR}"
)

target_include_directories(advent-of-code PRIVATE
  ${Z3_INCLUDE_DIRS}
)

target_link_libraries(advent-of-code PRIVATE
  ${Z3_LIBRARIES}
)
