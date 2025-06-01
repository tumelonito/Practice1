package ua.ukma.edu.elvvelon;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

public class CryptoUtils {

    private static final String ALGORITHM = "AES";
    private static final byte[] KEY = "VVELON_SecretKey".getBytes();

    public static byte[] encrypt(byte[] data) throws Exception {
        Key key = new SecretKeySpec(KEY, ALGORITHM);
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.ENCRYPT_MODE, key);
        return c.doFinal(data);
    }

    public static byte[] decrypt(byte[] encryptedData) throws Exception {
        Key key = new SecretKeySpec(KEY, ALGORITHM);
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.DECRYPT_MODE, key);
        return c.doFinal(encryptedData);
    }
}