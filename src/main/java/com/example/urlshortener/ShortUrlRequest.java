package com.example.urlshortener;

import lombok.*;

@Getter
@Setter
@ToString
public class ShortUrlRequest {
    private String longUrl;
}
