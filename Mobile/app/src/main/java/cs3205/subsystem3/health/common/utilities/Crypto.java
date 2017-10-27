package cs3205.subsystem3.health.common.utilities;

import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.miscellaneous.AppMessage;

/**
 * Created by danwen on 6/10/17.
 */

public class Crypto {

    public static byte[] generateHash(byte[] input) throws CryptoException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException(AppMessage.ERROR_MESSAGE_CRYPTO_EXCEPTION, e);
        }
    }


    public static byte[] generateChallengeResponse(String saltedPassword, byte[] challenge) throws CryptoException {

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

        Log.d("Crypto", "challenge response test: " + Arrays.equals(actualResult, expectedResult));


        //generate a random secret
        byte[] secret = new byte[32];
        new Random().nextBytes(secret);
        //client sends h(s) || totp(s)
        String totp = generateNfcAuthToken(secret);

        //server stores h(s) xor s
        byte[] data = computeXOR(generateHash(secret), secret);
        //server decodes client totp
        byte[] decoded = Base64.decode(totp, Base64.NO_WRAP);
        //server retrieves secret hash
        byte[] secretHash = new byte[32];
        System.arraycopy(decoded, 0, secretHash, 0, 32);
        //server recovers secret
        byte[] recoveredSecret = computeXOR(secretHash, data);
        //server generates totp from recovered secret
        byte[] client_totp = new byte[32];
        System.arraycopy(decoded, 32, client_totp, 0, 32);
        byte[] server_totp = generateTOTP(recoveredSecret);

        Log.d("Crypto", "totp test: " + Arrays.equals(client_totp, server_totp));
    }


    public static String generateNfcAuthToken(byte[] nfcTokenBytes) throws CryptoException {

        byte[] rawTOTP = new byte[64];
        System.arraycopy(generateHash(nfcTokenBytes), 0, rawTOTP, 0, 32);
        System.arraycopy(generateTOTP(nfcTokenBytes), 0, rawTOTP, 32, 32);

        return Base64.encodeToString(rawTOTP, Base64.NO_WRAP);
    }

    private static byte[] generateTOTP(byte[] nfcTokenBytes) throws CryptoException {
        try {
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            long timeCounter = System.currentTimeMillis() / 30000;
            hmacSHA256.init(new SecretKeySpec(nfcTokenBytes, "SHA-256"));
            return hmacSHA256.doFinal(String.valueOf(timeCounter).getBytes());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new CryptoException(AppMessage.ERROR_MESSAGE_CRYPTO_EXCEPTION, e);
        }
    }

    private static void doCrypto(int cipherMode, File inputFile, File outputFile) throws CryptoException{

        SecretKey secretKey = EncryptionKey.getSecretKey();
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipherMode, secretKey);
            FileInputStream inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            byte[] outputBytes = cipher.doFinal(inputBytes);

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(outputBytes);

            inputStream.close();
            outputStream.close();

        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException | IOException e) {
            throw new CryptoException(AppMessage.ERROR_MESSAGE_CRYPTO_EXCEPTION, e);
        }
    }


    public static void encryptFile(File inputFile, File outputFile)
            throws CryptoException {
        doCrypto(Cipher.ENCRYPT_MODE, inputFile, outputFile);

    }

    public static void decryptFile(File inputFile, File outputFile)
            throws CryptoException {
        doCrypto(Cipher.DECRYPT_MODE, inputFile, outputFile);
    }



}

