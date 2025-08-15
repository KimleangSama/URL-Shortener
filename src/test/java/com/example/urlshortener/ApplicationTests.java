package com.example.urlshortener;

import com.example.urlshortener.services.UrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTests {
    @Autowired
    private UrlService urlService;

    @Test
    void contextLoads() {
        for (int i = 0; i < 500_000; i++) {
            String longUrl = "https://keakimleang.com";
            urlService.shortenUrl(longUrl)
//                    .doOnNext(shortCode -> System.out.println("Shortened URL: " + shortCode))
                    .block();
        }
    }

}
