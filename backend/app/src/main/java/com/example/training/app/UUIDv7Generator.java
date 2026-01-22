package com.example.training.app;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

/**
 * UUIDv7 生成器（简化实现）。
 * <p>
 * 生产环境建议使用成熟库生成 UUIDv7，例如 com.github.f4b6a3:uuid-creator。
 */
public final class UUIDv7Generator {
    private static final SecureRandom RANDOM = new SecureRandom();

    private UUIDv7Generator() {
    }

    public static UUID generate() {
        long unixTsMillis = Instant.now().toEpochMilli();
        long msb = (unixTsMillis & 0xFFFFFFFFFFFFL) << 16;
        msb |= 0x7000L; // version 7
        long rand = RANDOM.nextLong();
        long lsb = (rand & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L; // variant 2
        return new UUID(msb, lsb);
    }
}
