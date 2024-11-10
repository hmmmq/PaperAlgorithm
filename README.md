# PaperAlgorithm

论文复现
LDRAS 和 VTAS
使用 docker 实现

## LDRAS

使用 docker 创建一个 ubantu 虚拟环境
安装了密码学算法库 Miracl

### 运行步骤
这是 Generic Construction of Linkable DualRing Adaptor Signature (LDRA) 签名算法的伪代码步骤，通过一系列过程实现可链接和适配器的双重功能。这里是每个步骤的详细解释：

### 1. `Setup(λ)` - 系统参数生成
   - **输入**：安全参数 \( λ \)
   - **输出**：返回参数 `param`
   - **描述**：调用基础算法（如 `IA.Setup(A)`）生成系统参数，通常包括生成椭圆曲线和基础点 \( G \) 等加密相关的参数。

### 2. `KeyGen(param)` - 密钥对生成
   - **输入**：系统参数 `param`
   - **输出**：公私钥对 `(pk, sk)`
   - **描述**：生成一个公私钥对，公钥用于签名验证，私钥用于生成签名。

### 3. `PreSign(param, M, pk, skj, Y)` - 预签名
   - **输入**：系统参数 `param`、消息 \( M \)、公钥 \( pk \)、签名者私钥 \( skj \) 和适配器 \( Y \)
   - **输出**：预签名 \( \sigma = (z, c, I) \)
   - **描述**：生成环签名的初步签名信息，用于后续签名阶段。计算中包含混淆信息 \( r \) 和环成员的公钥。

### 4. `PreVerify(param, M, pk, \sigma, Y)` - 预验证
   - **输入**：系统参数 `param`、消息 \( M \)、公钥 \( pk \)、预签名 \( \sigma \) 和适配器 \( Y \)
   - **输出**：返回是否验证通过
   - **描述**：计算预签名中的环签名值 \( R' \) 和 \( L' \)，验证是否符合签名消息 \( M \) 的哈希值。

### 5. `Adapt((Y, y), pk, \sigma, M)` - 适配
   - **输入**：适配器信息 \( (Y, y) \)、公钥 \( pk \)、消息 \( M \)
   - **输出**：适配签名 \( o = (z, c, I, J) \)
   - **描述**：基于适配器信息生成完整的环签名，将信息 `Y` 适配到签名中，通过 \( J = V(Y, y) \) 确保可以在需要时提取隐藏信息。

### 6. `Verify(param, M, pk, o)` - 签名验证
   - **输入**：系统参数 `param`、消息 \( M \)、公钥 \( pk \)、签名 \( o \)
   - **输出**：返回是否验证通过
   - **描述**：验证环签名的合法性，检查链式链接性。计算 \( R'' \) 和 \( L'' \) 是否与消息 \( M \) 的哈希值匹配。

### 7. `Ext(Y, \sigma, o)` - 信息提取
   - **输入**：适配器 \( Y \)、预签名 \( \sigma \)、签名 \( o \)
   - **输出**：返回提取出的隐藏信息或 `⊥` 表示解锁失败
   - **描述**：尝试提取适配器中的隐藏信息。通过计算 \( y = z \oplus \sigma \) 验证适配器中的信息是否有效。

### 8. `Link(pk, o', o'')` - 链接性检查
   - **输入**：公钥 \( pk \)、两个签名 \( o' \) 和 \( o'' \)
   - **输出**：返回 `Linked` 或 `Unlinked`
   - **描述**：通过比较两个签名的链式标识 \( I' \) 和 \( I'' \) 判断是否来自同一签名者，确保签名者的链接性。

### 步骤总结

该算法的主要流程包括生成密钥对、签名、验证以及在链上提供的适配解锁能力。适配器允许生成具有隐藏信息的签名，并在满足某些条件时提取这些信息；而链接性则确保签名者在重复签名时被识别。
#### 代码

```c
#include <stdio.h>
#include <stdlib.h>
#include "miracl.h"

// 初始化椭圆曲线和基本参数
void setup(big order, epoint *G, miracl *mip) {
    mip = mirsys(5000, 10);  // 设置大数系统
    irand((unsigned long)time(NULL));
    // 设置椭圆曲线，假设使用 y^2 = x^3 + Ax + B (可根据需求设置)
    big A = mirvar(0);
    big B = mirvar(7);
    ecurve_init(A, B, order, MR_AFFINE);  // 初始化椭圆曲线

    // 生成基点 G
    G = epoint_init();
    big x = mirvar(0);
    big y = mirvar(0);
    convert(5, x);  // 使用假设值，可按实际曲线选择
    convert(1, y);
    epoint_set(x, y, 0, G);
}

// 生成密钥对
void keygen(epoint *G, big order, big sk, epoint *pk) {
    sk = mirvar(0);
    bigrand(order, sk);  // 随机生成私钥
    pk = epoint_init();
    ecurve_mult(sk, G, pk);  // 生成公钥 pk = sk * G
}

// 预签名步骤
void pre_sign(epoint *G, epoint **pk_list, int n, big sk, big *r, big *c, epoint *Y, big order, epoint *R, epoint *L) {
    int i;
    big h = mirvar(0);
    epoint *tmp = epoint_init();

    // 初始化随机数 r，并计算环签名的初始值
    *r = mirvar(0);
    bigrand(order, *r);
    ecurve_mult(*r, G, R);  // R = r * G

    // 计算所有环成员的部分签名
    for (i = 0; i < n; i++) {
        if (i != sk) {
            c[i] = mirvar(0);
            bigrand(order, c[i]);  // 生成随机 c[i]
            ecurve_mult(c[i], pk_list[i], tmp);
            epoint_add(R, tmp);
        }
    }
    // 计算 L 和最终的 c
    ecurve_mult(h, G, L);  // L = h * G (需按实际需求设定)
}

// 签名适配步骤
void adapt(big Y, big y, epoint **pk_list, int n, big *c, big order, big *sigma) {
    big z = mirvar(0);
    zadd(y, Y, z);  // z = y + Y (示例)

    // 生成适配器签名元素，假设 `sigma` 保存结果
    copy(z, *sigma);
}

// 签名验证步骤
int verify(epoint *G, epoint **pk_list, int n, big *sigma, big order) {
    big h = mirvar(0);
    big c_prime = mirvar(0);
    epoint *R_prime = epoint_init();
    epoint *L_prime = epoint_init();

    // 验证计算，重建 R' 和 L'
    // 假设 c_prime 由消息生成
    if (mr_compare(c_prime, *sigma) == 0) {
        return 1;  // 验证通过
    } else {
        return 0;  // 验证失败
    }
}

// 提取隐藏信息步骤
void extract(big Y, big *sigma, big *result) {
    big y_extracted = mirvar(0);
    add(*sigma, Y, y_extracted);
    copy(y_extracted, *result);  // 假设 `result` 保存提取结果
}

// 链接性检查步骤
int link(big *sigma1, big *sigma2) {
    if (mr_compare(*sigma1, *sigma2) == 0) {
        return 1;  // Linked
    } else {
        return 0;  // Unlinked
    }
}

int main() {
    miracl *mip = mirsys(100, 0);
    big order = mirvar(0);
    epoint *G = epoint_init();
    setup(order, G, mip);

    big sk;
    epoint *pk = epoint_init();
    keygen(G, order, sk, pk);

    // 示例运行流程
    epoint *R = epoint_init();
    epoint *L = epoint_init();
    big r;
    big c[10];  // 假设环大小为 10
    pre_sign(G, NULL, 10, sk, &r, c, NULL, order, R, L);

    // 适配与验证示例
    big sigma = mirvar(0);
    adapt(r, sk, NULL, 10, c, order, &sigma);
    int verified = verify(G, NULL, 10, &sigma, order);

    if (verified) {
        printf("签名验证通过。\n");
    } else {
        printf("签名验证失败。\n");
    }

    mirkill(mip);
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
完整的Verifier Time-lock Adaptor Signature (VTAS) 算法框架包含以下几部分：
1. **Setup**: 初始化系统参数，包括生成基础群和生成元等。
2. **KeyGen**: 生成公私钥对。
3. **PreSign**: 生成预签名。
4. **PreVerify**: 验证预签名。
5. **Adapt**: 将预签名转换为完整的签名。
6. **Verify**: 验证签名。
7. **Commit**: 生成带时间锁的承诺。
8. **VerifyCommit**: 验证时间锁承诺。
9. **Open**: 解锁时间锁并获取签名。
10. **FOpen**: 解锁时间锁后强制获取签名。
11. **Link**: 检查两个签名是否链接，确保不可伪造。

为了实现VTAS算法，下面给出一个示例代码框架，使用MIRACL库来实现这些步骤。

### VTAS算法框架的C语言代码实现

```c
#include <stdio.h>
#include <stdlib.h>
#include "miracl.h"

// 初始化系统参数
void Setup(big *N, big *g, miracl *mip) {
    *N = mirvar(0);
    *g = mirvar(0);

    // 设置大素数N和生成元g
    convert(17, *N);  // 示例数值，实际应为大素数
    convert(5, *g);   // 示例生成元
}

// 生成公私钥对
void KeyGen(miracl *mip, big *sk, big *pk, big N, big g) {
    *sk = mirvar(0);
    *pk = mirvar(0);

    big temp = mirvar(0);
    bigrand(mip, N, *sk);        // 随机生成私钥 sk
    powmod(g, *sk, N, *pk);      // 计算公钥 pk = g^sk mod N
    mirkill(temp);
}

// 生成预签名
void PreSign(miracl *mip, big pk, big sk, big *sigma) {
    *sigma = mirvar(0);
    // 模拟预签名
    copy(sk, *sigma);
}

// 验证预签名
int PreVerify(miracl *mip, big pk, big sigma) {
    big temp = mirvar(0);
    // 模拟验证，实际需要更多细节
    copy(sigma, temp);
    if (mr_compare(pk, temp) == 0) return 1;
    return 0;
}

// 适配签名
void Adapt(miracl *mip, big pk, big y, big *signature) {
    *signature = mirvar(0);
    // 模拟适配器签名
    multiply(pk, y, *signature);
}

// 验证签名
int Verify(miracl *mip, big pk, big signature) {
    big temp = mirvar(0);
    // 模拟验证
    copy(signature, temp);
    if (mr_compare(pk, temp) == 0) return 1;
    return 0;
}

// 生成带时间锁的承诺
void Commit(miracl *mip, big g, big N, big T, big *C, big *pi) {
    big u = mirvar(0), v = mirvar(0), h = mirvar(0);
    big R1 = mirvar(0), R2 = mirvar(0), R3 = mirvar(0);
    big e = mirvar(0);
    big z1 = mirvar(0), z2 = mirvar(0);

    // 时间锁 Puzzle (h, u, v)
    powmod(g, T, N, h);
    powmod(g, T, N, u);
    powmod(h, T, N, v);

    // 零知识证明
    powmod(g, T, N, R1);
    powmod(h, T, N, R2);
    powmod(g, T, N, R3);

    // 承诺结果
    *C = mirvar(0);
    *pi = mirvar(0);
    copy(u, *C);
    copy(e, *pi);
}

// 验证时间锁承诺
int VerifyCommit(miracl *mip, big g, big N, big C, big pi) {
    big R1 = mirvar(0), R2 = mirvar(0), R3 = mirvar(0);
    big e = mirvar(0);

    // 模拟验证零知识证明
    return 1;
}

// 解锁时间锁 Puzzle
big Open(miracl *mip, big o) {
    return o;
}

// 强制解锁时间锁
big FOpen(miracl *mip, big N, big T) {
    big result = mirvar(0);
    powmod(T, 2, N, result);
    return result;
}

// 链接检测
int Link(miracl *mip, big sig1, big sig2) {
    if (mr_compare(sig1, sig2) == 0) return 1;  // 1表示已链接
    return 0;                                   // 0表示未链接
}

int main() {
    miracl *mip = mirsys(100, 0);

    big N, g;
    Setup(&N, &g, mip);

    big sk, pk;
    KeyGen(mip, &sk, &pk, N, g);

    big sigma;
    PreSign(mip, pk, sk, &sigma);

    if (PreVerify(mip, pk, sigma)) {
        printf("预签名验证通过\n");
    } else {
        printf("预签名验证失败\n");
    }

    big y = mirvar(3); // 示例值
    big signature;
    Adapt(mip, pk, y, &signature);

    if (Verify(mip, pk, signature)) {
        printf("签名验证通过\n");
    } else {
        printf("签名验证失败\n");
    }

    big T = mirvar(2); // 示例值
    big C, pi;
    Commit(mip, g, N, T, &C, &pi);

    if (VerifyCommit(mip, g, N, C, pi)) {
        printf("时间锁承诺验证通过\n");
    } else {
        printf("时间锁承诺验证失败\n");
    }

    big openedSig = Open(mip, signature);
    printf("解锁后的签名: %d\n", openedSig);

    big fopenedSig = FOpen(mip, N, T);
    printf("强制解锁后的签名: %d\n", fopenedSig);

    if (Link(mip, signature, fopenedSig)) {
        printf("签名已链接\n");
    } else {
        printf("签名未链接\n");
    }

    mirkill(mip);
    return 0;
}
```

### 代码说明
- **Setup**: 初始化系统参数，设置大数N和生成元g。
- **KeyGen**: 生成公私钥对。
- **PreSign**: 生成预签名。
- **PreVerify**: 验证预签名。
- **Adapt**: 生成适配签名。
- **Verify**: 验证适配签名。
- **Commit**: 使用时间锁Puzzle生成承诺C和零知识证明π。
- **VerifyCommit**: 验证时间锁承诺C和零知识证明π。
- **Open**: 解锁时间锁以获取签名。
- **FOpen**: 强制解锁时间锁。
- **Link**: 检查两个签名是否链接。

这段代码实现了VTAS算法的框架，但一些部分（如哈希函数和复杂的零知识证明）只是示例性的，实际应用时应进行更详细的实现。
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
