package com.topographe.topographe.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PasswordGenerator {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARS;
    private static final int DEFAULT_LENGTH = 12;

    private final SecureRandom random = new SecureRandom();

    /**
     * Génère un mot de passe aléatoire sécurisé
     * @return mot de passe généré
     */
    public String generatePassword() {
        return generatePassword(DEFAULT_LENGTH);
    }

    /**
     * Génère un mot de passe aléatoire sécurisé avec une longueur spécifiée
     * @param length longueur du mot de passe
     * @return mot de passe généré
     */
    public String generatePassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("La longueur du mot de passe doit être d'au moins 8 caractères");
        }

        StringBuilder password = new StringBuilder(length);

        // S'assurer qu'il y a au moins un caractère de chaque type
        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));

        // Remplir le reste avec des caractères aléatoires
        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        // Mélanger les caractères pour éviter un pattern prévisible
        return shuffleString(password.toString());
    }

    /**
     * Génère un mot de passe simple (lettres et chiffres uniquement)
     * @return mot de passe simple généré
     */
    public String generateSimplePassword() {
        return generateSimplePassword(DEFAULT_LENGTH);
    }

    /**
     * Génère un mot de passe simple avec une longueur spécifiée
     * @param length longueur du mot de passe
     * @return mot de passe simple généré
     */
    public String generateSimplePassword(int length) {
        if (length < 6) {
            throw new IllegalArgumentException("La longueur du mot de passe doit être d'au moins 6 caractères");
        }

        String simpleChars = UPPERCASE + LOWERCASE + DIGITS;
        StringBuilder password = new StringBuilder(length);

        // S'assurer qu'il y a au moins un caractère de chaque type
        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));

        // Remplir le reste
        for (int i = 3; i < length; i++) {
            password.append(simpleChars.charAt(random.nextInt(simpleChars.length())));
        }

        return shuffleString(password.toString());
    }

    /**
     * Mélange les caractères d'une chaîne
     * @param input chaîne à mélanger
     * @return chaîne mélangée
     */
    private String shuffleString(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
}