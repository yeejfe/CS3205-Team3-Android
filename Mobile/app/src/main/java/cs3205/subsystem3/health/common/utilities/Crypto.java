package cs3205.subsystem3.health.common.utilities;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cs3205.subsystem3.health.common.miscellaneous.AppMessage;

import static cs3205.subsystem3.health.common.crypto.Algorithm.SHA_256;

/**
 * Created by danwen on 6/10/17.
 */


public class Crypto {
    public static byte[] generateHash(byte[] input) throws CryptoException {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException(AppMessage.ERROR_MESSAGE_CRYPTO_EXCEPTION, e);
        }
    }


    public static byte[] generatePasswordResponse(String saltedPassword, byte[] challenge) throws CryptoException {

        //hash the password
        byte[] passwordHash = generateHash(saltedPassword.getBytes());
        //hash the password hash
        byte[] result = generateHash(passwordHash);
        //XOR result with challenge
        result = computeXOR(result, challenge);
        //hash the result
        result = generateHash(result);
        //XOR result with password hash
        result = computeXOR(result, passwordHash);

        return result;
    }

    public static byte[] generateNfcResponse(String nfcSecret, byte[] challenge) throws CryptoException {
        byte[] nfcTokenBytes = Base64.decode(nfcSecret, Base64.NO_WRAP);
        byte[] result = generateHash(nfcTokenBytes);
        result = computeXOR(result, challenge);
        result = generateHash(result);
        result = computeXOR(result, nfcTokenBytes);
        return result;
    }

    private static byte[] computeXOR(byte[] b1, byte[] b2) {
        byte[] result = new byte[32];
        for (int i = 0; i < 32; i++) {
            result[i] = (byte)(b1[i] ^ b2[i]);
        }
        return result;
    }

}

