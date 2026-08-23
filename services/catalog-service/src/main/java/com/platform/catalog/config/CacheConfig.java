package com.platform.catalog.config;

import com.platform.catalog.dto.category.CategoryResponse;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager redisCacheManager(
            RedisConnectionFactory connectionFactory
    ) {

        JsonMapper jsonMapper =
                JsonMapper.builder()
                        .findAndAddModules()
                        .build();

        // =========================================
        // CategoryResponse serializer
        // =========================================

        JacksonJsonRedisSerializer<CategoryResponse>
                categorySerializer =
                new JacksonJsonRedisSerializer<>(
                        jsonMapper,
                        CategoryResponse.class
                );

        // =========================================
        // List<CategoryResponse> serializer
        // =========================================

        JacksonJsonRedisSerializer<List>
                categoryListSerializer =
                new JacksonJsonRedisSerializer<>(
                        jsonMapper,
                        List.class
                );

        // =========================================
        // Common key configuration
        // =========================================

        StringRedisSerializer keySerializer =
                new StringRedisSerializer();

        // =========================================
        // categoryById
        // =========================================

        RedisCacheConfiguration categoryByIdConfig =
                RedisCacheConfiguration
                        .defaultCacheConfig()
                        .entryTtl(
                                Duration.ofMinutes(10)
                        )
                        .disableCachingNullValues()
                        .serializeKeysWith(
                                RedisSerializationContext
                                        .SerializationPair
                                        .fromSerializer(
                                                keySerializer
                                        )
                        )
                        .serializeValuesWith(
                                RedisSerializationContext
                                        .SerializationPair
                                        .fromSerializer(
                                                categorySerializer
                                        )
                        );

        // =========================================
        // categoryAll
        // =========================================

        RedisCacheConfiguration categoryAllConfig =
                RedisCacheConfiguration
                        .defaultCacheConfig()
                        .entryTtl(
                                Duration.ofMinutes(10)
                        )
                        .disableCachingNullValues()
                        .serializeKeysWith(
                                RedisSerializationContext
                                        .SerializationPair
                                        .fromSerializer(
                                                keySerializer
                                        )
                        )
                        .serializeValuesWith(
                                RedisSerializationContext
                                        .SerializationPair
                                        .fromSerializer(
                                                categoryListSerializer
                                        )
                        );

        // =========================================
        // Redis Cache Manager
        // =========================================

        return RedisCacheManager
                .builder(connectionFactory)
                .withCacheConfiguration(
                        "categoryById",
                        categoryByIdConfig
                )
                .withCacheConfiguration(
                        "categoryAll",
                        categoryAllConfig
                )
                .build();
    }
}