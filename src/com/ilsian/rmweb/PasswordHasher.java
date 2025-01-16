package com.ilsian.rmweb;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;

public class PasswordHasher {
    public static String hashPassword(String password) {
        // Generate a random salt value
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        // Hash the password with the salt value
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));

            // Store the salt value and hashed password
            return bytesToHex(salt) + ":" + bytesToHex(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verifyPassword(String storedPassword, String inputPassword) {
        // Split the stored password into salt and hashed password
        String[] parts = storedPassword.split(":");
        byte[] salt = hexToBytes(parts[0]);

        // Hash the input password with the salt value
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedBytes = md.digest(inputPassword.getBytes(StandardCharsets.UTF_8));

            // Compare the hashed input password with the stored hashed password
            return bytesToHex(hashedBytes).equals(parts[1]);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

//    private static byte[] hexToBytes(String hex) {
//        byte[] bytes = new byte[hex.length() / 2];
//        for (int i = 0; i < bytes.length; i++) {
//            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
//        }
//        return bytes;
//    }
    
    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] bytes = new byte[(len + 1) / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, Math.min(i + 2, len)), 16);
        }
        return bytes;
    }
    
    public static void main(String [] args) {
    	String hashed = PasswordHasher.hashPassword("fordebug");
    	System.err.println("HASH: " + hashed);
    	boolean check = PasswordHasher.verifyPassword(hashed, "fordebug");
    	System.err.println("CHECK: " + check);
    }
}