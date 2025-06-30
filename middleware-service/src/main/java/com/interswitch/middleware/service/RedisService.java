package com.interswitch.middleware.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public void setValue(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void setValue(String key, String value, long durationInSec) {
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(durationInSec));
    }

    public String getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}

