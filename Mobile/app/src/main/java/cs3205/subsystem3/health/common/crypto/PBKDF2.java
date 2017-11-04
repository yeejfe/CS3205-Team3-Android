package cs3205.subsystem3.health.common.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Created by Yee on 10/30/17.
 */

public class PBKDF2 {
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 32;
    private static final String FORMAT = "%x";

    public static SecretKey derive(String password, byte[] salt) {
        char[] passwordChars = password.toCharArray();
        return derive(passwordChars, salt);
    }

    private static SecretKey derive(char[] passwordChars, byte[] saltBytes) {
        PBEKeySpec spec = new PBEKeySpec(passwordChars, saltBytes, ITERATIONS, KEY_LENGTH);

        //byte[] hashedPassword = null;
        SecretKeyFactory keyFactory;
        SecretKey secretKey = null;
        try {
            keyFactory = SecretKeyFactory.getInstance(Algorithm.PBKDF2);
            secretKey = keyFactory.generateSecret(spec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        //hashedPassword = secretKey.getEncoded();

        return secretKey;
        //return String.format(FORMAT, new BigInteger(hashedPassword));
    }
}
