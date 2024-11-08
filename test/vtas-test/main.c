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

