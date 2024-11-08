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
    unzip \
    openssl \
    libssl-dev \
    libgmp-dev \
    libboost-all-dev \
    libfftw3-dev \
    libzmq3-dev \
    libsecp256k1-dev \
    zlib1g-dev \
    vim \
    && rm -rf /var/lib/apt/lists/*

# 判断是否已经下载过 CMake 3.25.0 的安装包，若没有则下载并安装
RUN if [ ! -f /cmake-3.25.0.tar.gz ]; then \
        echo "CMake tarball not found, downloading..."; \
        wget https://github.com/Kitware/CMake/releases/download/v3.25.0/cmake-3.25.0.tar.gz -P /; \
    else \
        echo "CMake tarball already downloaded"; \
    fi

# 安装 CMake 3.25
RUN tar -zxvf /cmake-3.25.0.tar.gz \
    && cd cmake-3.25.0 \
    && ./bootstrap \
    && make \
    && make install \
    && cd .. \
    && rm -rf cmake-3.25.0 cmake-3.25.0.tar.gz

# 设置工作目录
WORKDIR /workspace

# 将当前文件夹中的文件复制到容器的工作目录
COPY ./test /workspace/test

# 设置默认命令为交互式 shell
CMD ["/bin/bash"]