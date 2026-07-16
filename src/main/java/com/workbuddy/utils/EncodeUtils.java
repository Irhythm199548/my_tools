package com.workbuddy.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.net.URLCodec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * 编码工具类
 * 说明：Base64 / 摘要 明确使用 Apache Commons Codec（不依赖 JDK 9+ 的 java.util.Base64）。
 *       AES 使用 AES/CBC/PKCS5Padding，随机 IV 前缀拼接，整体再做 Base64。
 */
public class EncodeUtils {

    /** 加密算法 */
    private static final String AES_ALGO = "AES/CBC/PKCS5Padding";
    /** IV 长度（AES 块大小 16 字节） */
    private static final int IV_LEN = 16;

    // ---------------- Base64 ----------------

    /** Base64 编码（Commons Codec） */
    public static String base64Encode(String text) {
        return Base64.encodeBase64String(text.getBytes(StandardCharsets.UTF_8));
    }

    /** Base64 解码（Commons Codec） */
    public static String base64Decode(String text) {
        return new String(Base64.decodeBase64(text), StandardCharsets.UTF_8);
    }

    // ---------------- URL ----------------

    /** URL 编码（强制 UTF-8） */
    public static String urlEncode(String text) {
        try {
            return new URLCodec().encode(text, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("URL 编码失败", e);
        }
    }

    /** URL 解码（强制 UTF-8） */
    public static String urlDecode(String text) {
        try {
            return new URLCodec().decode(text, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("URL 解码失败", e);
        }
    }

    // ---------------- 摘要 ----------------

    /** MD5（Commons Codec DigestUtils） */
    public static String md5(String text) {
        return DigestUtils.md5Hex(text.getBytes(StandardCharsets.UTF_8));
    }

    /** SHA 系列：sha1 / sha256 / sha512（Commons Codec DigestUtils） */
    public static String sha(String text, String algorithm) {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        switch (algorithm.toLowerCase()) {
            case "sha1":
                return DigestUtils.sha1Hex(data);
            case "sha256":
                return DigestUtils.sha256Hex(data);
            case "sha512":
                return DigestUtils.sha512Hex(data);
            default:
                throw new IllegalArgumentException("不支持的算法: " + algorithm);
        }
    }

    // ---------------- AES ----------------

    /** 由密钥派生 16 字节（AES-128）密钥：SHA-256 取前 16 字节 */
    private static byte[] deriveKey(String secret) {
        byte[] hash = DigestUtils.sha256(secret.getBytes(StandardCharsets.UTF_8));
        return Arrays.copyOfRange(hash, 0, IV_LEN);
    }

    /** AES 加密：随机 IV 拼接到密文前，整体 Base64 编码 */
    public static String aesEncrypt(String plainText, String secret) {
        try {
            byte[] key = deriveKey(secret);
            byte[] iv = new byte[IV_LEN];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(AES_ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            byte[] enc = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[IV_LEN + enc.length];
            System.arraycopy(iv, 0, out, 0, IV_LEN);
            System.arraycopy(enc, 0, out, IV_LEN, enc.length);
            return Base64.encodeBase64String(out);
        } catch (Exception e) {
            throw new RuntimeException("AES 加密失败: " + e.getMessage(), e);
        }
    }

    /** AES 解密 */
    public static String aesDecrypt(String cipherText, String secret) {
        try {
            byte[] all = Base64.decodeBase64(cipherText);
            if (all.length < IV_LEN) {
                throw new IllegalArgumentException("密文格式不正确");
            }
            byte[] iv = Arrays.copyOfRange(all, 0, IV_LEN);
            byte[] enc = Arrays.copyOfRange(all, IV_LEN, all.length);
            byte[] key = deriveKey(secret);
            Cipher cipher = Cipher.getInstance(AES_ALGO);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            byte[] dec = cipher.doFinal(enc);
            return new String(dec, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES 解密失败: " + e.getMessage(), e);
        }
    }
}
