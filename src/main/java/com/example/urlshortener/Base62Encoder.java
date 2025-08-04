package com.example.urlshortener;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class Base62Encoder {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = 62;
    private static final int CODE_LENGTH = 7;
    private static final SecureRandom random = new SecureRandom();

    public String generateCode() {
        long MAX_VALUE = (long) Math.pow(62, 7);
        long value = nextRandomLong(MAX_VALUE);
        return encode(value);
    }

    private long nextRandomLong(long bound) {
        long r;
        do {
            r = Math.abs(random.nextLong()) % bound;
        } while (r >= bound);
        return r;
    }

    private String encode(long value) {
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(ALPHABET.charAt((int) (value % BASE)));
            value /= BASE;
        }
        while (sb.length() < CODE_LENGTH) {
            sb.append(ALPHABET.charAt(0));
        }
        return sb.reverse().toString();
    }
}

