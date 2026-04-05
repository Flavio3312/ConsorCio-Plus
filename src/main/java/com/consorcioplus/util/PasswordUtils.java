package com.consorcioplus.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilidad para el hash seguro de contraseñas con SHA-256.
 *
 * <p>Las contraseñas nunca se almacenan en texto plano.
 * Se almacena únicamente el hash hexadecimal de 64 caracteres.
 */
public final class PasswordUtils {

    /** Algoritmo de hash utilizado. */
    private static final String ALGORITHM = "SHA-256";

    /** Clase de utilidad — no debe instanciarse. */
    private PasswordUtils() {
        throw new UnsupportedOperationException("Clase de utilidad, no instanciable.");
    }

    /**
     * Genera el hash SHA-256 de una contraseña en texto plano.
     *
     * @param plainPassword contraseña en texto plano
     * @return hash hexadecimal de 64 caracteres
     * @throws RuntimeException si el algoritmo SHA-256 no está disponible
     */
    public static String hash(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hashBytes = digest.digest(
                    plainPassword.getBytes(StandardCharsets.UTF_8));
            return toHexString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no disponible en esta JVM.", e);
        }
    }

    /**
     * Verifica si una contraseña en texto plano coincide con su hash almacenado.
     *
     * @param plainPassword contraseña ingresada por el usuario
     * @param storedHash    hash almacenado en la base de datos
     * @return true si la contraseña es correcta
     */
    public static boolean matches(String plainPassword, String storedHash) {
        return hash(plainPassword).equalsIgnoreCase(storedHash);
    }

    /** Convierte un arreglo de bytes a su representación hexadecimal. */
    private static String toHexString(byte[] bytes) {
        BigInteger number = new BigInteger(1, bytes);
        String hex = number.toString(16);
        // Asegura que siempre tenga 64 caracteres con ceros a la izquierda
        while (hex.length() < 64) {
            hex = "0" + hex;
        }
        return hex;
    }
}
