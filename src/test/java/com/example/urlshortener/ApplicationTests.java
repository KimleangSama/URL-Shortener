package com.example.urlshortener;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTests {
    @Autowired
    private UrlService urlService;

    @Test
    void contextLoads() {
        for (int i = 0; i < 1_000_000; i++) {
            String longUrl = "https://keakimleang.com";
            urlService.shortenUrl(longUrl)
//                    .doOnNext(shortCode -> System.out.println("Shortened URL: " + shortCode))
                    .block();
        }
    }

}
