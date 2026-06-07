package org.autostock.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import java.net.URI;

@Configuration
public class AwsConfig {

    @Value("${aws.endpoint-override:}")
    private String endpointOverride;

    @Value("${aws.region:us-east-1}")
    private String region;

    @Value("${aws.access-key-id:test}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:test}")
    private String secretAccessKey;

    private StaticCredentialsProvider credentials() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey));
    }

    private URI endpoint() {
        return (endpointOverride != null && !endpointOverride.isBlank())
                ? URI.create(endpointOverride) : null;
    }

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentials())
                .forcePathStyle(true); // obligatoire pour LocalStack
        URI ep = endpoint();
        if (ep != null) builder.endpointOverride(ep);
        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        var builder = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentials());
        URI ep = endpoint();
        if (ep != null) builder.endpointOverride(ep);
        return builder.build();
    }

    @Bean
    public SesClient sesClient() {
        var builder = SesClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentials());
        URI ep = endpoint();
        if (ep != null) builder.endpointOverride(ep);
        return builder.build();
    }

    @Bean
    public SqsClient sqsClient() {
        var builder = SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentials());
        URI ep = endpoint();
        if (ep != null) builder.endpointOverride(ep);
        return builder.build();
    }

    @Bean
    public SecretsManagerClient secretsManagerClient() {
        var builder = SecretsManagerClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentials());
        URI ep = endpoint();
        if (ep != null) builder.endpointOverride(ep);
        return builder.build();
    }
}
