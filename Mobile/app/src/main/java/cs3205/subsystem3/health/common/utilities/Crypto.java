package cs3205.subsystem3.health.common.utilities;

import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import cs3205.subsystem3.health.common.logger.Log;
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
        Log.d("Crypto", passwordHash.toString());
        //hash the password hash
        byte[] result = generateHash(passwordHash);
        Log.d("Crypto", result.toString());

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

    //for testing only
    public static void test() throws CryptoException {
        String saltedPassword = "password";
        byte[] passwordHash = generateHash(saltedPassword.getBytes());
        byte[] expectedResult = generateHash(passwordHash);

        byte[] challenge = new byte[32];
        new SecureRandom().nextBytes(challenge);
        byte[] response = generatePasswordResponse(saltedPassword, challenge);

        //XOR hash of password hash with challenge
        byte[] actualResult = computeXOR(expectedResult, challenge);
        //hash the result
        actualResult = generateHash(actualResult);
        //XOR result with challenge response
        actualResult = computeXOR(actualResult, response);
        //hash the result
        actualResult = generateHash(actualResult);

        Log.d("Crypto", "challenge response test: " + Arrays.equals(actualResult, expectedResult));

        byte[] nfcToken = new byte[32];
        new SecureRandom().nextBytes(nfcToken);
        byte[] nfcHash = generateHash(nfcToken);
        byte[] nfcResponse = generateNfcResponse(Base64.encodeToString(nfcToken, Base64.NO_WRAP), challenge);
        byte[] nfcResult = generateHash(computeXOR(nfcHash, challenge));
        nfcResult = computeXOR(nfcResult, nfcResponse);
        nfcResult = generateHash(nfcResult);
        Log.d("Crypto", "nfc response test: " + Arrays.equals(nfcHash, nfcResult));

    }
}

