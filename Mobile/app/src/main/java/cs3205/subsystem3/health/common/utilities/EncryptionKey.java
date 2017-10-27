package cs3205.subsystem3.health.common.utilities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import cs3205.subsystem3.health.common.miscellaneous.AppMessage;

/**
 * Created by danwen on 27/10/17.
 */

public class EncryptionKey {
        private final static EncryptionKey encryptionKey = new EncryptionKey();
        private static SecretKey secretKey = null;

        private EncryptionKey() { }

        public static SecretKey getSecretKey( ) throws CryptoException {
            if (encryptionKey != null && secretKey != null) {
                return secretKey;
            } else {
                throw new CryptoException(AppMessage.ERROR_MESSAGE_SECRET_KEY_NOT_INITIALIZED);
            }
        }

        public static void init(String nfcToken) throws NoSuchAlgorithmException {
            if (secretKey == null) {
                final MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] rawKey = digest.digest(nfcToken.getBytes());
                secretKey = new SecretKeySpec(rawKey, 0, rawKey.length, "AES");
            }
        }

}
