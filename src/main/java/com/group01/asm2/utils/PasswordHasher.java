package com.group01.asm2.utils;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65_536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private PasswordHasher() {
    }

    public static String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        try {
            byte[] salt = generateSalt();
            byte[] hash = pbkdf2(rawPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

            return "pbkdf2_sha256$"
                + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);

        } catch (Exception exception) {
            throw new RuntimeException("Could not hash password.", exception);
        }
    }

    public static boolean verify(String rawPassword, String storedHash) {
        if (rawPassword == null || rawPassword.isBlank()) {
            return false;
        }

        if (storedHash == null || storedHash.isBlank()) {
            return false;
        }

        try {
            String[] parts = storedHash.split("\\$");

            if (parts.length != 4) {
                return false;
            }

            String version = parts[0];

            if (!"pbkdf2_sha256".equals(version)) {
                return false;
            }

            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            byte[] actualHash = pbkdf2(
                rawPassword.toCharArray(),
                salt,
                iterations,
                expectedHash.length * 8
            );

            return MessageDigest.isEqual(expectedHash, actualHash);

        } catch (Exception exception) {
            return false;
        }
    }

    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);
        return salt;
    }

    private static byte[] pbkdf2(
        char[] password,
        byte[] salt,
        int iterations,
        int keyLength
    ) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        return factory.generateSecret(spec).getEncoded();
    }
}