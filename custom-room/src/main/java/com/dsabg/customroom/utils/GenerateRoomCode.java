package com.dsabg.customroom.utils;

import java.security.SecureRandom;

public class GenerateRoomCode {

    private static final SecureRandom RANDOM = new SecureRandom();

    private GenerateRoomCode() {
    }

    public static Long generate() {
        return 10_000_000L + RANDOM.nextInt(90_000_000);
    }
}