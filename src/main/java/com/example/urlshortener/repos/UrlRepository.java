package com.example.urlshortener.repos;

import com.example.urlshortener.entities.UrlEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UrlRepository extends R2dbcRepository<UrlEntity, Long> {
    Mono<UrlEntity> findByShortCode(String shortCode);
}