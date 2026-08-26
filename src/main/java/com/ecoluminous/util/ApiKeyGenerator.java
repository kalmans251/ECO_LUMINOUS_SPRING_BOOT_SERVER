package com.ecoluminous.util;

import java.security.SecureRandom;

public class ApiKeyGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateKey() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 16; i++) {
            if (i > 0 && i % 4 == 0) {
                sb.append("-");
            }
            int randomIndex = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }

        return sb.toString(); // 출력 예시: A1B2-C3D4-E5F6-G7H8
    }
}