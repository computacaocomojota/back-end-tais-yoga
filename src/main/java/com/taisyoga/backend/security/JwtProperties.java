package com.taisyoga.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api.security.jwt")
public record JwtProperties(
        String secret,
        long expiration
) {}
