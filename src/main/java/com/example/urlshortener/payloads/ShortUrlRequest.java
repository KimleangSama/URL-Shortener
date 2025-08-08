package com.example.urlshortener.payloads;

import lombok.*;

@Getter
@Setter
@ToString
public class ShortUrlRequest {
    private String longUrl;
}
