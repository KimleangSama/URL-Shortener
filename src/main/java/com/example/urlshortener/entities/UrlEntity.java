package com.example.urlshortener.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table(name = "urls")
public class UrlEntity {
    @Id
    private Long id;
    private String shortCode;
    private String longUrl;
    private Instant createdAt;
    private Long clickCount;
}
