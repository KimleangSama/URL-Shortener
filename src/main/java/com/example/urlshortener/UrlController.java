package com.example.urlshortener;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("/api")
public class UrlController {
    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/shorten")
    public Mono<ShortUrlResponse> shortenUrl(@RequestBody ShortUrlRequest request) {
        return urlService.shortenUrl(request.getLongUrl())
                .map(ShortUrlResponse::new);
    }

    @GetMapping("/{shortCode}")
    public Mono<Void> redirect(@PathVariable String shortCode, ServerWebExchange exchange) {
        return urlService.getLongUrl(shortCode)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(longUrl -> {
                    try {
                        URI uri = URI.create(longUrl); // May throw IllegalArgumentException
                        exchange.getResponse().setStatusCode(HttpStatus.OK); // or MOVED_PERMANENTLY
                        exchange.getResponse().getHeaders().setLocation(uri);
                        return exchange.getResponse().setComplete();
                    } catch (IllegalArgumentException e) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid redirect URL"));
                    }
                });
    }
}
