# PaperAlgorithm

论文复现
LDRAS 和 VTAS

## LDRAS

使用 docker 创建一个 ubantu 虚拟环境
安装了密码学算法库 Miracl

### 运行步骤

```
(base) hmqhmq@192 PaperAlgorithm % ./dockercmd1.sh
[+] Building 1.4s (11/11) FINISHED                         docker:desktop-linux
 => [internal] load build definition from Dockerfile                       0.0s
 => => transferring dockerfile: 1.51kB                                     0.0s
 => [internal] load metadata for docker.io/library/ubuntu:22.04            0.0s
 => [internal] load .dockerignore                                          0.1s
 => => transferring context: 2B                                            0.0s
 => [1/6] FROM docker.io/library/ubuntu:22.04                              0.0s
 => [internal] load build context                                          0.6s
 => => transferring context: 13.06MB                                       0.6s
 => CACHED [2/6] RUN apt update && apt upgrade -y     && apt install -y    0.0s
 => CACHED [3/6] RUN if [ ! -f /cmake-3.25.0.tar.gz ]; then         echo   0.0s
 => CACHED [4/6] RUN tar -zxvf /cmake-3.25.0.tar.gz     && cd cmake-3.25.  0.0s
 => CACHED [5/6] WORKDIR /workspace                                        0.0s
 => [6/6] COPY ./test /workspace/test                                      0.6s
 => exporting to image                                                     0.1s
 => => exporting layers                                                    0.1s
 => => writing image sha256:8f5a81ba7ac53088bb1575535303a97d35d7924e7f926  0.0s
 => => naming to docker.io/library/my-ldras-image                          0.0s

View build details: docker-desktop://dashboard/build/desktop-linux/desktop-linux/orvrxd77q9lmtmx7n1h6qx4ua

What's next:
    View a summary of image vulnerabilities and recommendations → docker scout quickview
root@4f98cde22977:/workspace# ls
test
root@4f98cde22977:/workspace# cd test
root@4f98cde22977:/workspace/test# ls
miracl  ldras-test
root@4f98cde22977:/workspace/test# cd ldras-test
root@4f98cde22977:/workspace/test/ldras-test# ./main
Signature 1:
Signature 2:
Signature 1 is valid.
Signature 2 is valid.
```
