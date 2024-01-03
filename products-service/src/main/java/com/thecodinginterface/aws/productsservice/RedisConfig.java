package com.thecodinginterface.aws.productsservice;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.protocol.ProtocolVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;

import java.time.Duration;
import java.time.Instant;

import static com.thecodinginterface.aws.productsservice.ElasticacheIamAuthTokenGenerator.TOKEN_EXPIRY_SECONDS;

@Configuration
@EnableCaching
public class RedisConfig {

    static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.application.name}")
    String appName;

    @Value("${AWS_REGION}")
    String awsRegion;

    volatile IamToken iamToken;

    Environment env;
    RedisClientProperties redisClientProperties;
    SpringDataRedisProperties redisProperties;
    public RedisConfig(Environment env, RedisClientProperties redisClientProperties, SpringDataRedisProperties redisProperties) {
        this.env = env;
        this.redisClientProperties = redisClientProperties;
        this.redisProperties = redisProperties;
    }

    @Bean
    public RedisCacheConfiguration defaultCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(redisProperties.getEntryTtl())
                .prefixCacheNameWith(appName + ":")
                .disableCachingNullValues();
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        log.info("*** Creating Connection Factory");
        log.info("*** - awsRegion={}", awsRegion);
        log.info("*** - host={}", redisProperties.getHost());
        log.info("*** - port={}", redisProperties.getPort());
        log.info("*** - database={}", redisProperties.getDatabase());
        log.info("*** - username={}", redisProperties.getUsername());
        log.info("*** - replicaGroupId={}", redisProperties.getReplicaGroupId());

        var password = getPassword();
        log.info("*** - password={}", password);
        var clientCfg = LettuceClientConfiguration.builder()
                .readFrom(ReadFrom.REPLICA_PREFERRED)
                .commandTimeout(redisClientProperties.getCommandTimeout())
                .shutdownTimeout(redisClientProperties.getShutdownTimeout())
                .clientOptions(ClientOptions.builder().protocolVersion(ProtocolVersion.RESP2).build())
                .useSsl()
                .build();

        var clusterCfg = new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());
        clusterCfg.setUsername(redisProperties.getUsername());
        clusterCfg.setPassword(password);
        clusterCfg.setDatabase(redisProperties.getDatabase());

        var factory = new LettuceConnectionFactory(clusterCfg, clientCfg);

        log.info("#### hostname being used {}", factory.getHostName());

        return factory;
    }

    private synchronized String getPassword() {
        if (!env.matchesProfiles("aws")) {
            return redisProperties.getPassword();
        }

        // iam password generation
        log.info("*** using iam auth, checking to see if new iam token generation is needed");
        var now = Instant.now();
        var generate = iamToken == null || now.isAfter(iamToken.created().plusSeconds(TOKEN_EXPIRY_SECONDS));
        if (generate) {
            log.info("*** generating iam token");
            var request = new ElasticacheIamAuthTokenGenerator(redisProperties.getUsername(),
                    redisProperties.getReplicaGroupId(), DefaultCredentialsProvider.create(), awsRegion);
            var tokenPassword = request.toSignedRequestUri();
            log.info("*** generated iam token {}", tokenPassword);
            iamToken = new IamToken(now, tokenPassword);
        }

        return iamToken.value();
    }

    record IamToken(Instant created, String value) {
    }

    @Bean
    public CacheManager redisCacheManager() {
        return RedisCacheManager.builder(redisConnectionFactory())
                .cacheDefaults(defaultCacheConfig())
                .enableStatistics()
                .build();
    }

    /**
     * See https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html
     */
    @Configuration
    @ConfigurationProperties(prefix = "spring.data.redis.lettuce")
    static class RedisClientProperties {
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
        String replicaGroupId;
        int database = 0;

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

        public String getReplicaGroupId() {
            return replicaGroupId;
        }

        public void setReplicaGroupId(String replicaGroupId) {
            this.replicaGroupId = replicaGroupId;
        }

        public int getDatabase() {
            return database;
        }

        public void setDatabase(int database) {
            this.database = database;
        }
    }
}
