package space.mmty.util;

import org.apache.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA1加密数据
 * https://blog.csdn.net/LucasXu01/article/details/82954991
 */
public class SecuritySHA1Utils {
    private static final Logger logger = Logger.getLogger(SecuritySHA1Utils.class);

    // 使用sha1加密数据
    public static String shaEncode(String inStr) {
        MessageDigest sha;
        try {
            sha = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            logger.error("java获取SHA加密失败", e);
            sha = null;
        }

        // 判断获取加密是否成功
        if (sha == null) {
            return null;
        }

        byte[] byteArray = inStr.getBytes(StandardCharsets.UTF_8);
        byte[] sha1Array = sha.digest(byteArray);
        StringBuilder hexValue = new StringBuilder();
        for (byte b : sha1Array) {
            int val = ((int) b) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    public static void main(String[] args) {
        logger.info(shaEncode("kjyabfjeokahfyeafbeyuaw"));
    }
}
