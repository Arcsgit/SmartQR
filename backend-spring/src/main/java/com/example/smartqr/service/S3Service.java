package com.example.smartqr.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class S3Service {
    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            this.s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();
            log.info("S3 Service initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize S3 Service", e);
            throw new RuntimeException("Failed to initialize S3 Service", e);
        }
    }

    public String uploadFile(String fileName, ByteArrayOutputStream outputStream) {
        try {
            byte[] bytes = outputStream.toByteArray();
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType("image/png")
                    .contentLength((long) bytes.length)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
            String imageUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                    bucketName, region, fileName);
            log.info("File uploaded successfully: {}", imageUrl);
            return imageUrl;
        } catch (Exception e) {
            log.error("Failed to upload file to S3: {}", fileName, e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    public void deleteFile(String fileName) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully: {}", fileName);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", fileName, e);
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }

    public byte[] downloadFile(String fileName) throws IOException {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObject(getObjectRequest, ResponseTransformer.toBytes());
            return objectBytes.asByteArray();
        } catch (S3Exception e) {
            log.error("Failed to download file from S3: {}", fileName, e);
            throw new IOException("Failed to download file: " + e.getMessage(), e);
        }
    }
}
