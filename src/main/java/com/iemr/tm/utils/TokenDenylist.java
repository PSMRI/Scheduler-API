package com.iemr.tm.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
public class TokenDenylist {
    private static final String PREFIX = "denied_";
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public void addTokenToDenylist(String jti, Long expirationTime) {
        if (jti != null && expirationTime != null && expirationTime > 0) {
            try {
                String key = PREFIX + jti;
                redisTemplate.opsForValue().set(key, true);
                redisTemplate.expire(key, expirationTime, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                throw new RuntimeException("Failed to add token to denylist", e);
            }
        }
    }
    
    public boolean isTokenDenylisted(String jti) {
        if (jti == null) {
            return false;
        }
        try {
            Object value = redisTemplate.opsForValue().get(PREFIX + jti);
            return value != null;
        } catch (Exception e) {
            // If Redis is down, assume token is valid to prevent service disruption
            return false;
        }
    }

}
