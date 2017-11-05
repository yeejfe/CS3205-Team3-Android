package cs3205.subsystem3.health.common.crypto;

/**
 * Created by Yee on 11/03/17.
 */

public class Algorithm {
    public static final String AES = "AES";
    public static final String SHA_256 = "SHA-256";
    //NOTE: Only Android api 26+ have better algorithms like PBKDF2withHmacSHA256
    //Android ORE0 is only release to public recently
    public static final String PBKDF2 = "PBKDF2withHmacSHA1";
    public static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
}
