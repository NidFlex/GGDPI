cmake_minimum_required(VERSION 3.22.1)
project("ggdpi")

set(CMAKE_CXX_STANDARD 17)

add_library(
    ggdpi
    SHARED
    sni-extractor.cpp
)

target_link_libraries(
    ggdpi
    android
    log
)