package com.fablix.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.naming.InitialContext;

public final class RedisUtil {
    private static volatile JedisPool jedisPool;

    private RedisUtil() {}

    public static void init() {
        if (jedisPool != null) {
            return;
        }

        synchronized (RedisUtil.class) {
            if (jedisPool != null) {
                return;
            }

            try {
                InitialContext context = new InitialContext();
                String address = (String) context.lookup("java:comp/env/redis/Address");
                String[] parts = address.split(":");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Invalid redis/Address value: " + address);
                }

                JedisPoolConfig poolConfig = new JedisPoolConfig();
                poolConfig.setMaxTotal(50);
                poolConfig.setMaxIdle(10);
                poolConfig.setMinIdle(2);
                poolConfig.setTestOnBorrow(true);

                jedisPool = new JedisPool(poolConfig, parts[0], Integer.parseInt(parts[1]));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to initialize Redis pool", e);
            }
        }
    }

    public static String get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    public static void set(String key, String value, int ttlSeconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
            jedis.expire(key, ttlSeconds);
        }
    }

    public static long increment(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incr(key);
        }
    }

    public static void delete(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        }
    }
}
