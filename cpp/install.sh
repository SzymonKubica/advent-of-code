#!/bin/bash
echo "Creating build directories..."
mkdir build build-debug
cd build
echo "Configuring release build..."
cmake ..
cd -
cd build-debug
echo "Configuring debug build..."
cmake -DCMAKE_BUILD_TYPE=Debug ..
cd -
