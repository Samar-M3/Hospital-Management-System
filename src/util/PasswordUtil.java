package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for hashing and verifying passwords using SHA-256 with a random salt.
 * Stored format: Base64(salt) + ":" + Base64(hash).
 */
public class PasswordUtil {

    private static final String ALGORITHM  = "SHA-256";
    private static final int    SALT_BYTES = 16;

    /**
     * Hashes a plain-text password with a fresh salt.
     */
    public static String hash(String plainText) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_BYTES];
            random.nextBytes(salt);

            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashBytes = md.digest(plainText.getBytes());

            String saltB64 = Base64.getEncoder().encodeToString(salt);
            String hashB64 = Base64.getEncoder().encodeToString(hashBytes);
            return saltB64 + ":" + hashB64;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("[PasswordUtil] Hashing algorithm not found.", e);
        }
    }

    /**
     * Compares a plain-text password against a stored salt:hash value.
     */
    public static boolean verify(String plainText, String stored) {
        try {
            if (stored == null || !stored.contains(":")) return false;

            String[] parts   = stored.split(":", 2);
            byte[]   salt    = Base64.getDecoder().decode(parts[0]);
            byte[]   expHash = Base64.getDecoder().decode(parts[1]);

            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] actualHash = md.digest(plainText.getBytes());

            if (expHash.length != actualHash.length) return false;
            int diff = 0;
            for (int i = 0; i < expHash.length; i++) {
                diff |= expHash[i] ^ actualHash[i];
            }
            return diff == 0;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("[PasswordUtil] Hashing algorithm not found.", e);
        }
    }
}
