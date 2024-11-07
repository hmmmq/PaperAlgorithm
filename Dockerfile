# 使用 Ubuntu 官方镜像作为基础镜像
FROM ubuntu:22.04

# 设置环境变量
ENV DEBIAN_FRONTEND=noninteractive

# 更新并安装必要的依赖
RUN apt update && apt upgrade -y \
    && apt install -y \
    build-essential \
    g++ \
    git \
    curl \
    wget \
    openssl \
    libssl-dev \
    libgmp-dev \
    libboost-all-dev \
    libfftw3-dev \
    libzmq3-dev \
    libsecp256k1-dev \
    zlib1g-dev \
    && rm -rf /var/lib/apt/lists/*

# 下载并安装 CMake 3.25
RUN wget https://cmake.org/files/v3.25/cmake-3.25.0-Linux-x86_64.tar.gz \
    && tar -zxvf cmake-3.25.0-Linux-x86_64.tar.gz \
    && mv cmake-3.25.0-Linux-x86_64 /opt/cmake \
    && ln -s /opt/cmake/bin/cmake /usr/local/bin/cmake \
    && rm cmake-3.25.0-Linux-x86_64.tar.gz

# 确保 CMake 正确安装
RUN cmake --version

# 设置工作目录
WORKDIR /workspace

# 将当前文件夹中的文件复制到容器的工作目录
COPY . /workspace

# 设置默认命令为交互式 shell
CMD ["/bin/bash"]
