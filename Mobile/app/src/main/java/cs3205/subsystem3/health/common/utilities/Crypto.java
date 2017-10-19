package cs3205.subsystem3.health.common.utilities;

import android.util.Base64;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by danwen on 6/10/17.
 */

public class Crypto {

    private static byte[] generateHash(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(input);
    }


    public static byte[] generateChallengeResponse(String saltedPassword, byte[] challenge) throws NoSuchAlgorithmException {

        //hash the password
        byte[] passwordHash = generateHash(saltedPassword.getBytes());
        //hash the password hash
        byte[] result = generateHash(passwordHash);
        //XOR result with challenge
        result = computeXOR(result, challenge);
        //hash the result
        result = generateHash(result);
        //XOR result of password hash
        result = computeXOR(result, passwordHash);

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
    public static boolean test() throws NoSuchAlgorithmException {
        String saltedPassword = "password";
        byte[] passwordHash = generateHash(saltedPassword.getBytes());
        byte[] expectedResult = generateHash(passwordHash);

        byte[] challenge = new byte[32];
        new Random().nextBytes(challenge);
        byte[] response = generateChallengeResponse(saltedPassword, challenge);

        //XOR hash of password hash with challenge
        byte[] actualResult = computeXOR(expectedResult, challenge);
        //hash the result
        actualResult = generateHash(actualResult);
        //XOR result with challenge response
        actualResult = computeXOR(actualResult, response);
        //hash the result
        actualResult = generateHash(actualResult);

        return Arrays.equals(actualResult, expectedResult);

    }

    public static String generateTOTP(String nfcToken) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        long timeCounter = System.currentTimeMillis() / 30000;
        hmacSHA256.init(new SecretKeySpec(nfcToken.getBytes(), "SHA-256"));
        return Base64.encodeToString(hmacSHA256.doFinal(String.valueOf(timeCounter).getBytes()), Base64.DEFAULT);
    }
}

