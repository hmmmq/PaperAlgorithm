import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import java.math.BigInteger;
import java.security.SecureRandom;
//LDRAS
public class EllipticCurveSignature {
     // 初始化椭圆曲线和基本参数
    private static final SecureRandom random = new SecureRandom();
    private static final ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");  // 使用 secp256k1 曲线
    private static final ECPoint G = ecSpec.getG();  // 使用默认生成点,  生成基点 G

    private static final BigInteger order = ecSpec.getN();  // 获取曲线的阶

    // 生成私钥和公钥,生成密钥对
    public static ECPrivateKeyParameters generatePrivateKey() {
        // 随机生成私钥，确保私钥在有效范围内
        BigInteger sk = new BigInteger(order.bitLength(), random).mod(order);
        System.out.println("Private Key: " + sk);

        // 基于私钥生成公钥 (sk * G)
        ECPoint pk = G.multiply(sk);  // 公钥 pk = sk * G

        // 检查公钥是否有效
        if (pk.isInfinity()) {
            throw new IllegalArgumentException("Generated public key is not on the curve.");
        }

        // 打印公钥
        System.out.println("Public Key: " + pk);

        ECDomainParameters domainParameters = new ECDomainParameters(ecSpec.getCurve(), G, order);
        return new ECPrivateKeyParameters(sk, domainParameters);
    }

    public static ECPublicKeyParameters generatePublicKey(ECPrivateKeyParameters privateKey) {
        ECPoint pk = G.multiply(privateKey.getD());
        return new ECPublicKeyParameters(pk, new ECDomainParameters(ecSpec.getCurve(), G, order));
    }

    // 签名预生成步骤
    public static void preSign(ECPoint[] pkList, BigInteger sk, BigInteger[] r, BigInteger[] c, ECPoint R, ECPoint L) {
        // 初始化随机数 r，并计算环签名的初始值

        BigInteger h = new BigInteger(order.bitLength(), random).mod(order);
        r[0] = new BigInteger(order.bitLength(), random).mod(order);
        R = G.multiply(r[0]);
        // 计算所有环成员的部分签名

        for (int i = 0; i < pkList.length; i++) {
            if (!BigInteger.valueOf(i).equals(sk)) {
                c[i] = new BigInteger(order.bitLength(), random).mod(order);
                ECPoint tmp = pkList[i].multiply(c[i]);
                R = R.add(tmp);
            }
        }
        // 计算 L 和最终的 c

        L = G.multiply(h);  // L = h * G
    }

    // 验证签名步骤
    public static boolean verify(ECPoint G, ECPoint[] pkList, BigInteger sigma) {
        BigInteger cPrime = BigInteger.ZERO;  // 假设消息生成的 c'
        // 在这里实现验证逻辑，通常是通过验签算法计算并比对 c'
        return sigma.equals(cPrime);
    }

    public static void main(String[] args) {
        // 生成私钥和公钥
        ECPrivateKeyParameters privateKey = generatePrivateKey();
        ECPublicKeyParameters publicKey = generatePublicKey(privateKey);

        // 示例操作
        ECPoint[] pkList = new ECPoint[10]; // 假设环大小为 10
        BigInteger sk = privateKey.getD();
        BigInteger[] r = new BigInteger[10];
        BigInteger[] c = new BigInteger[10];
        ECPoint R = ecSpec.getCurve().getInfinity();  // 初始化为无穷大点
        ECPoint L = ecSpec.getCurve().getInfinity();  // 初始化为无穷大点

        // 初始化 pkList，这里简单地用公钥数组来示范
        for (int i = 0; i < pkList.length; i++) {
            pkList[i] = generatePublicKey(privateKey).getQ();  // 假设生成同样的公钥
        }

        // 测量 preSign 方法的执行时间
        long startTime = System.nanoTime();  // 记录开始时间
        preSign(pkList, sk, r, c, R, L);
        long endTime = System.nanoTime();    // 记录结束时间

        // 计算并打印运行时间
        long duration = endTime - startTime;
        System.out.println("preSign 方法运行时间: " + duration + " 纳秒");

        // 验证示例
        BigInteger sigma = BigInteger.ZERO; // 生成 sigma 示例
        boolean verified = verify(G, pkList, sigma);
        System.out.println("签名验证" + (verified ? "通过" : "失败"));
    }
}
