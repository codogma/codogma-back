package com.github.codogma.codogmaback.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisLikeService {

    private final RedisTemplate<String, Integer> redisTemplate;

    public RedisLikeService(RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void incrementLikesCount(Long articleId) {
        String key = "article:likes:" + articleId;
        redisTemplate.opsForValue().increment(key, 1);
    }

    public void decrementLikesCount(Long articleId) {
        String key = "article:likes:" + articleId;
        Integer currentCount = redisTemplate.opsForValue().get(key);
        if (currentCount != null && currentCount > 0) {
            redisTemplate.opsForValue().increment(key, -1);
        }
    }

    public Integer getLikesCount(Long articleId) {
        String key = "article:likes:" + articleId;
        Integer count = redisTemplate.opsForValue().get(key);
        return count != null ? count : 0;
    }
}
