package com.login.springlogin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public JwtParser jwtParser() {
        return Jwts.parser().setSigningKey(jwtSecret);
    }

}
