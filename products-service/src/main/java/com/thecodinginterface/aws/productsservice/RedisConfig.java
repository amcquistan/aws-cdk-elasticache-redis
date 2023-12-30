package com.thecodinginterface.aws.productsservice;

import io.lettuce.core.ReadFrom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.application.name}")
    String appName;

    RedisClientProperties redisClientProperties;
    SpringDataRedisProperties redisProperties;
    public RedisConfig(RedisClientProperties redisClientProperties, SpringDataRedisProperties redisProperties) {
        this.redisClientProperties = redisClientProperties;
        this.redisProperties = redisProperties;
    }

    @Bean
    public RedisCacheConfiguration defaultCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(redisProperties.entryTtl)
                .prefixCacheNameWith(appName + ":")
                .disableCachingNullValues();
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {

        var clientCfg = LettuceClientConfiguration.builder()
                .readFrom(ReadFrom.REPLICA_PREFERRED)
                .commandTimeout(redisClientProperties.commandTimeout)
                .shutdownTimeout(redisClientProperties.shutdownTimeout)
                .useSsl()
                .build();

        var clusterCfg = new RedisStandaloneConfiguration(redisProperties.host, redisProperties.port);
        clusterCfg.setUsername(redisProperties.username);
        clusterCfg.setPassword(redisProperties.password);

        return new LettuceConnectionFactory(clusterCfg, clientCfg);
    }

    @Bean
    public CacheManager redisCacheManager() {
        return RedisCacheManager.builder(redisConnectionFactory())
                .cacheDefaults(defaultCacheConfig())
                .enableStatistics()
                .build();
    }

    /**
     * See https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties.data.spring.data.redis.jedis.pool.enabled
     */
    @Configuration
    @ConfigurationProperties(prefix = "spring.data.redis.lettuce")
    static class RedisClientProperties {
//        Boolean poolEnabled = true;
//        Integer poolMaxActive = 8;
//        Integer poolMaxIdle = 8;
//        Duration poolMaxWait = Duration.ofMillis(-1);
//        Integer poolMinIdle = 0;
        Duration shutdownTimeout = Duration.ofMillis(200);
        Duration commandTimeout = Duration.ofMillis(20_000);

        public Duration getShutdownTimeout() {
            return shutdownTimeout;
        }

        public void setShutdownTimeout(Duration shutdownTimeout) {
            this.shutdownTimeout = shutdownTimeout;
        }

        public Duration getCommandTimeout() {
            return commandTimeout;
        }

        public void setCommandTimeout(Duration commandTimeout) {
            this.commandTimeout = commandTimeout;
        }
    }

    @Configuration
    @ConfigurationProperties(prefix = "spring.data.redis")
    static class SpringDataRedisProperties {
        String host = "localhost";
        Integer port = 6379;
        String username = "default";
        String password = "";
        Duration entryTtl = Duration.ofMinutes(5);

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Duration getEntryTtl() {
            return entryTtl;
        }

        public void setEntryTtl(Duration entryTtl) {
            this.entryTtl = entryTtl;
        }
    }
}
