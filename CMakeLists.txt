cmake_minimum_required(VERSION 3.25)
project(test)
 
set(CMAKE_CXX_STANDARD 17)
set(OBJECTS_MIRACL ${PROJECT_SOURCE_DIR}/miracl/big.o) 
#需要添加.o文件，否则会编译失败
 
INCLUDE_DIRECTORIES(${PROJECT_SOURCE_DIR}/miracl)
 
add_executable(test main.cpp ${OBJECTS_MIRACL})
 
target_link_libraries(${PROJECT_NAME} ${PROJECT_SOURCE_DIR}/miracl/miracl.a) 
#添加静态库
