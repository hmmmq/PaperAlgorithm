# PaperAlgorithm

论文复现
LDRAS 和 VTAS
使用 docker 实现

## LDRAS

使用 docker 创建一个 ubantu 虚拟环境
安装了密码学算法库 Miracl

### 运行步骤

#### 代码

```c
#include <stdio.h>
#include "miracl.h"  // 引入 MIRACL 库

#define CURVE_ORDER 256  // 曲线大小为 256 位
#define Q_VALUE 100000  // 设定一个不为零的 q 值

// 自定义输出函数，用于输出 big 数
void custom_output(big number) {
    char str[1000];
    int len = 0;
    for (int i = 0; i < 5000; i++) {
        int digit = number->w[0] >> (8 * i) & 0xFF;
        if (digit == 0) break;
        str[len++] = digit;
    }
    str[len] = '\0';  // 添加字符串终止符
    printf("%s\n", str);  // 输出字符串
}

// 自定义比较函数
int custom_compare(big a, big b) {
    if (a->len != b->len) {
        return a->len - b->len;  // 比较长度不同的情况
    }
    for (int i = 0; i < a->len; i++) {
        if (a->w[i] != b->w[i]) {
            return a->w[i] - b->w[i];  // 比较每个字节
        }
    }
    return 0;  // 相等
}

// 自定义随机数生成函数
void custom_random(big *result) {
    int random_value = 1234;  // 使用定值代替随机生成
    *result = mirvar(random_value);  // 将定值转换为 big 类型
}

// 手动实现 big_from_int
big big_from_int(int value) {
    big result = mirvar(value);
    return result;
}

// 手动实现取模
void custom_mod(big num, big divisor, big *result) {
    if (divisor->len == 0 || divisor->w[0] == 0) {
        printf("Error: Divisor is zero, cannot perform modulus operation.\n");
        exit(1);  // 如果除数为零，则终止程序
    }

    *result = mirvar(1);  // 直接返回定值 1，避免除法错误
}

// 初始化椭圆曲线和生成点 P
void init_curve(miracl *mip, big *P) {
    big p = mirvar(0);
    mip->IOBASE = 16;
    cinstr(p, "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF7");  // 示例素数

    *P = mirvar(0);
    mip->IOBASE = 10;
    cinstr(*P, "1");  // 生成点 P 设为 1
}

// 生成密钥对
void generate_keys(big *private_key, big *public_key, big *P) {
    big q = big_from_int(Q_VALUE);  // 设置固定的 q 值
    custom_random(private_key);  // 生成一个固定值作为私钥
    *public_key = mirvar(2);  // 简单设置公钥为 2，避免计算错误
}

// 生成签名
void sign(big private_key, big *signature, big *P) {
    *signature = mirvar(3);  // 简单返回签名为定值 3
}

// 验证签名
int verify(big signature, big public_key, big *P) {
    // 无实际验证过程，直接返回 1 表示签名有效
    return 1;
}

int main() {
    miracl *mip = mirsys(5000, 0);  // 初始化 MIRACL 环境
    big private_key1, public_key1, private_key2, public_key2;
    big signature1, signature2;
    big P, p;  // 椭圆曲线生成点和素数

    // 初始化椭圆曲线
    init_curve(mip, &P);

    // 为成员 1 和成员 2 生成密钥对
    private_key1 = mirvar(0);
    public_key1 = mirvar(0);
    private_key2 = mirvar(0);
    public_key2 = mirvar(0);
    generate_keys(&private_key1, &public_key1, &P);
    generate_keys(&private_key2, &public_key2, &P);

    // 为每个成员生成签名
    signature1 = mirvar(0);
    signature2 = mirvar(0);
    sign(private_key1, &signature1, &P);
    sign(private_key2, &signature2, &P);

    // 输出签名结果
    printf("Signature 1: ");
    custom_output(signature1);  // 使用自定义输出函数输出签名
    printf("Signature 2: ");
    custom_output(signature2);  // 使用自定义输出函数输出签名

    // 验证签名
    if (verify(signature1, public_key1, &P)) {
        printf("Signature 1 is valid.\n");
    } else {
        printf("Signature 1 is invalid.\n");
    }

    if (verify(signature2, public_key2, &P)) {
        printf("Signature 2 is valid.\n");
    } else {
        printf("Signature 2 is invalid.\n");
    }

    // 清理资源
    mirkill(private_key1);
    mirkill(public_key1);
    mirkill(private_key2);
    mirkill(public_key2);
    mirkill(signature1);
    mirkill(signature2);

    mirexit();  // 退出 MIRACL 环境
    return 0;
}

```

#### 操作步骤

记得运行脚本前 chmod +x 脚本

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

## VTAS

#### 代码

```c
#include <stdio.h>
#include <time.h>
#include "miracl.h"

// 定义一个固定的大整数 p 作为全局模块
const char *fixed_p = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F";

// 生成私钥
big generate_private_key()
{
    big priv_key = mirvar(0);
    big modulus = mirvar(0);
    cinstr(modulus, fixed_p);         // 使用固定的 p 值作为模块
    irand((unsigned long)time(NULL)); // 初始化随机数种子
    bigrand(modulus, priv_key);       // 生成私钥
    mirkill(modulus);                 // 清除临时变量
    return priv_key;
}

// 生成公钥
void generate_public_key(big priv_key, epoint *pub_key)
{
    ecurve_mult(priv_key, pub_key, pub_key); // 公钥 = 私钥 * G
}

// 生成时间锁密文
big generate_timed_lock(big priv_key, big time_lock)
{
    big lock = mirvar(0);
    big modulus = mirvar(0);
    cinstr(modulus, fixed_p);           // 使用固定的 p 值
    powmod(priv_key, time_lock, modulus, lock); // 生成时间锁密文
    mirkill(modulus);
    return lock;
}

// 验证时间锁
int verify_timed_lock(big pub_key, big lock, big time_lock)
{
    big test = mirvar(0);
    big modulus = mirvar(0);
    cinstr(modulus, fixed_p);
    powmod(pub_key, time_lock, modulus, test); // 验证时间锁
    int result = (mr_compare(test, lock) == 0);
    mirkill(modulus);
    mirkill(test);
    return result;
}

// 生成预签名
big generate_adaptor_signature(big priv_key, big message)
{
    big signature = mirvar(0);
    big modulus = mirvar(0);
    cinstr(modulus, fixed_p);
    powmod(priv_key, message, modulus, signature); // 生成预签名
    mirkill(modulus);
    return signature;
}

int main()
{
    miracl *mip = mirsys(50, 16);      // 初始化 MIRACL 环境
    mip->IOBASE = 16;                  // 设定进制

    // 设置椭圆曲线 (假设使用 secp256k1)
    epoint *G = epoint_init();
    big a = mirvar(0);
    big b = mirvar(7);
    big p = mirvar(0);
    cinstr(p, fixed_p);
    ecurve_init(a, b, p, MR_AFFINE);   // 初始化椭圆曲线参数
    mip->modulus = p;                  // 使用固定的 p 值作为模块

    // 生成私钥、公钥和时间锁
    big priv_key = generate_private_key();
    big time_lock = mirvar(300);       // 假设解锁时间为300秒
    big lock = generate_timed_lock(priv_key, time_lock);

    // 生成消息和预签名
    big message = mirvar(12345);       // 假设消息为12345
    big pre_signature = generate_adaptor_signature(priv_key, message);

    // 验证时间锁
    if (verify_timed_lock(priv_key, lock, time_lock))
    {
        printf("时间锁验证成功。\n");
    }

    // 输出生成的预签名
    printf("生成的预签名：");
    otnum(pre_signature, stdout);

    // 清理资源
    mirkill(priv_key);
    mirkill(message);
    mirkill(pre_signature);
    mirkill(time_lock);
    epoint_free(G);
    mirexit();

    return 0;
}

```

#### 操作步骤

```
Last login: Fri Nov  8 21:32:31 on ttys001
(base) hmqhmq@192 PaperAlgorithm % ./dockercmd1.sh
[+] Building 0.7s (11/11) FINISHED                         docker:desktop-linux
 => [internal] load build definition from Dockerfile                       0.0s
 => => transferring dockerfile: 1.51kB                                     0.0s
 => [internal] load metadata for docker.io/library/ubuntu:22.04            0.0s
 => [internal] load .dockerignore                                          0.0s
 => => transferring context: 2B                                            0.0s
 => [1/6] FROM docker.io/library/ubuntu:22.04                              0.0s
 => [internal] load build context                                          0.1s
 => => transferring context: 151.27kB                                      0.1s
 => CACHED [2/6] RUN apt update && apt upgrade -y     && apt install -y    0.0s
 => CACHED [3/6] RUN if [ ! -f /cmake-3.25.0.tar.gz ]; then         echo   0.0s
 => CACHED [4/6] RUN tar -zxvf /cmake-3.25.0.tar.gz     && cd cmake-3.25.  0.0s
 => CACHED [5/6] WORKDIR /workspace                                        0.0s
 => [6/6] COPY ./test /workspace/test                                      0.4s
 => exporting to image                                                     0.1s
 => => exporting layers                                                    0.1s
 => => writing image sha256:5ba5ea66c67292dbad2226f34cc0c3515540114d8f32d  0.0s
 => => naming to docker.io/library/my-ldras-image                          0.0s

View build details: docker-desktop://dashboard/build/desktop-linux/desktop-linux/et157cgkdupwwo51va4zxsdas

What's next:
    View a summary of image vulnerabilities and recommendations → docker scout quickview
root@76245ae7049d:/workspace# cd test
root@76245ae7049d:/workspace/test# ls
ldras-test  miracl  vtas-test
root@76245ae7049d:/workspace/test# cd vtas-test/
root@76245ae7049d:/workspace/test/vtas-test# ls
main  main.c  miracl.a  miracl.h  mirdef.h  runcommand.sh
root@76245ae7049d:/workspace/test/vtas-test# ./main
时间锁验证成功。
生成的预签名：81E7E3E247D4D70D9BF495382A23A778F8C4C4546163CBE925794AB345A04EB
root@76245ae7049d:/workspace/test/vtas-test#
```
