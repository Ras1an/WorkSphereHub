package com.raslan.taskmanager.util;

import java.security.SecureRandom;

public class CodeGenerator {
    private static final String CHARACTERS="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();


    public static String generateCode(int length) {
        return generate(length);
    }


    public static String generateCode() {
        return generate(8);
    }


    private static String generate(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }

        return sb.toString();
    }
}
