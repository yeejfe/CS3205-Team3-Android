package cs3205.subsystem3.health.common.crypto;

import android.util.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.utilities.Crypto;
import cs3205.subsystem3.health.common.utilities.CryptoException;

/**
 * Created by Yee on 10/31/17.
 */

public class Encryption {
    private String TAG = this.getClass().getName();

    private static final int IV_SIZE = 16;
    private static final int SALT_SIZE = 32;
    private static final int SRC_POS = 0;
    private static final int DEST_POS = 0;

    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isKeyNull(){
        return (key == null);
    }

    private static Encryption encryption = new Encryption();

    public static Encryption getInstance() {
        if(encryption == null){
            encryption = new Encryption();
        }
        return encryption;
    }

    public byte[] e(byte[] textBytes, String key){
        return e(new String(textBytes), key);
    }

    public byte[] e(String plainText, String key) {
        byte[] clean = plainText.getBytes(StandardCharsets.UTF_8);

        //Generate Salt
        byte[] saltBytes = generateSalt(SALT_SIZE);
        byte[] salt = new byte[SALT_SIZE];
        try {
            salt = Crypto.generateHash(saltBytes);
        } catch (CryptoException e) {
            e.printStackTrace();
        }

        //Generate IV
        byte[] iv = generateIV(IV_SIZE);
        IvParameterSpec randomIvSpec = new IvParameterSpec(iv);

        // Deriving key
        SecretKey secretKey = PBKDF2.derive(key, salt);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), Algorithm.AES);

        // Encrypt.
        byte[] encryptedBytes = null;
        try {
            Cipher cipher = Cipher.getInstance(Algorithm.TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, randomIvSpec);
            encryptedBytes = cipher.doFinal(clean);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        // Combine salt and IV and encrypted part.
        byte[] encryptedSaltAndIVAndText = new byte[salt.length + iv.length + encryptedBytes.length];
        System.arraycopy(salt, SRC_POS, encryptedSaltAndIVAndText, DEST_POS, salt.length);
        System.arraycopy(iv, SRC_POS, encryptedSaltAndIVAndText, salt.length, iv.length);
        System.arraycopy(encryptedBytes, SRC_POS, encryptedSaltAndIVAndText, salt.length + iv.length, encryptedBytes.length);

        return Base64.encode(encryptedSaltAndIVAndText, Base64.NO_WRAP);
    }

    public byte[] d(InputStream inputStream, String key) throws IOException {
        byte[] encText = new byte[inputStream.available()];
        inputStream.read(encText);
        inputStream.close();

        Log.d(TAG,new String(encText));

        byte[] decrypt = d(encText, key);

        return decrypt;
    }

    public byte[] d(byte[] encryptedSaltIvTextBytes, String key) {
        // Strip off Salt and IV
        ByteBuffer bufferEncrypted = ByteBuffer.wrap(Base64.decode(encryptedSaltIvTextBytes, Base64.NO_WRAP));
        byte[] saltBytes = new byte[SALT_SIZE];
        bufferEncrypted.get(saltBytes, SRC_POS, saltBytes.length);

        byte[] ivBytes = new byte[IV_SIZE];
        bufferEncrypted.get(ivBytes, SRC_POS, ivBytes.length);

        IvParameterSpec randomIvSpec = new IvParameterSpec(ivBytes);

        // Extract encrypted
        byte[] encryptedTextBytes = new byte[bufferEncrypted.capacity() - saltBytes.length - ivBytes.length];
        bufferEncrypted.get(encryptedTextBytes);

        // Deriving key
        SecretKey secretKey = PBKDF2.derive(key, saltBytes);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), Algorithm.AES);

        // Decrypt
        byte[] decrypted = null;
        try {
            Cipher cipher = Cipher.getInstance(Algorithm.TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, randomIvSpec);
            decrypted = cipher.doFinal(encryptedTextBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return decrypted;
    }

    private static byte[] generateIV(int ivSize) {
        byte[] iv = new byte[ivSize];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }

    private static byte[] generateSalt(int saltSize) {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[saltSize];
        random.nextBytes(bytes);
        byte[] randomSaltBytes = bytes;

        return randomSaltBytes;
    }
}
