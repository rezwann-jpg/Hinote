package com.hinote.shared.utils;

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;

public class IdGenerator {
    private static final SecureRandom random = new SecureRandom();
    private static final AtomicLong counter = new AtomicLong(0);
    
    public static String generateUniqueId() {
        long timestamp = System.currentTimeMillis();
        long count = counter.getAndIncrement();
        int randomSuffix = random.nextInt(1000);
        return String.format("%d-%d-%d", timestamp, count, randomSuffix);
    }
    
    public static String generateRoomId() {
        return "room-" + generateUniqueId();
    }
    
    public static String generateUserId() {
        return "user-" + generateUniqueId();
    }
}
