package com.thecodinginterface.aws.productsservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4PresignerParams;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;

public class ElasticacheIamAuthTokenGenerator {
    static final Logger log = LoggerFactory.getLogger(ElasticacheIamAuthTokenGenerator.class);

    private static final SdkHttpMethod REQUEST_METHOD = SdkHttpMethod.GET;
    private static final String REQUEST_PROTOCOL = "http://";
    private static final String ACTION_PARAM_KEY = "Action";
    private static final String PARAM_ACTION = "Action";
    private static final String PARAM_USER = "User";
    private static final String USER_PARAM_KEY = "User";
    private static final String ACTION_PARAM_VALUE = "connect";
    private static final String ACTION_NAME = "connect";
    private static final String SERVICE_NAME = "elasticache";
    public static final long TOKEN_EXPIRY_SECONDS = 900L;
    private static final Duration TOKEN_EXPIRY = Duration.ofSeconds(TOKEN_EXPIRY_SECONDS);

    private final String userId;
    private final String replicationGroupId;
    private final String region;
    private final AwsCredentialsProvider credsProvider;

    public ElasticacheIamAuthTokenGenerator(String userId, String replicationGroupId, AwsCredentialsProvider credsProvider, String region) {
        this.userId = userId;
        this.replicationGroupId = replicationGroupId;
        this.region = region;
        this.credsProvider = credsProvider;
    }

    public String toSignedRequestUri() {
        var presignRequest = SdkHttpFullRequest.builder()
                .method(REQUEST_METHOD)
                .uri(URI.create(String.format("%s%s/", REQUEST_PROTOCOL, replicationGroupId)))
                .appendRawQueryParameter(ACTION_PARAM_KEY, ACTION_PARAM_VALUE)
                .appendRawQueryParameter(USER_PARAM_KEY, userId)
                .build();
        log.info("presignRequest created with host {}", presignRequest.host());

        var presignParams = Aws4PresignerParams.builder()
                .signingRegion(Region.of(region))
                .awsCredentials(credsProvider.resolveCredentials())
                .signingName(SERVICE_NAME)
                .expirationTime(Instant.now().plus(TOKEN_EXPIRY))
                .build();
        log.info("presignedParams created");

        var signedRequest = Aws4Signer.create().presign(presignRequest, presignParams);

        log.info("signed request created");
        var result = signedRequest.getUri().toString().replace(REQUEST_PROTOCOL, "");

        log.info("*** signedRequest={}", result);

        return result;
    }
}
