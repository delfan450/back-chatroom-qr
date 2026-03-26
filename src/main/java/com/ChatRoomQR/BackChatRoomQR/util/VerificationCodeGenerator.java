package com.ChatRoomQR.BackChatRoomQR.util;

import java.util.Random;

public class VerificationCodeGenerator {
    private static final Random random = new Random();

    public static String generate() {
        int codigo = 100000 + random.nextInt(900000);
        return String.valueOf(codigo);
    }
}
