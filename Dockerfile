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
RUN wget https://github.com/Kitware/CMake/releases/download/v3.25.0/cmake-3.25.0.tar.gz \
    && tar -zxvf cmake-3.25.0.tar.gz \
    && cd cmake-3.25.0 \
    && ./bootstrap \
    && make \
    && sudo make install \
    && cmake --version


# 设置工作目录
WORKDIR /workspace

# 将当前文件夹中的文件复制到容器的工作目录
COPY . /workspace

# 设置默认命令为交互式 shell
CMD ["/bin/bash"]
