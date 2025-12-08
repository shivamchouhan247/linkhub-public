package com.scm.service.impl;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.scm.service.S3Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
public class S3ServiceImpl implements S3Service {
    @Value("${app.s3.bucket}")
    private String bucket;

    private S3Client s3Client;
    private S3Presigner s3Presigner;

    public S3ServiceImpl(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(S3ServiceImpl.class);

    @Override
    public String uploadFile(MultipartFile file) throws Exception {
        try {

            if (file.isEmpty()) {
                throw new IOException("file is empty");
            }

            // Generate Unique Filename with Original Extension
            String originalFileName = file.getOriginalFilename();
            String extension = FilenameUtils.getExtension(originalFileName);
            String fileName = UUID.randomUUID().toString() + (extension.isEmpty() ? "" : ("." + extension));

            // Define S3 bucketName , File Name(key),File content type
            PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucket).key(fileName)
                    .contentType(file.getContentType()).build();

            PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest,
                    RequestBody.fromBytes(file.getBytes()));

            LOGGER.info("putresponse :{}", putObjectResponse.toString());

            return fileName;
            // return generatePresignedUrl(fileName);

        } catch (Exception e) {
            throw new Exception("Failed to upload file", e);
        }
    }

    @Override
    public String generatePresignedUrl(String fileName) throws Exception {

        try {
            if (fileName == null || fileName.isEmpty()) {
                return "";
            }
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(r -> {
                r.getObjectRequest(getObjectRequest)
                        .signatureDuration(Duration.ofHours(1));
            });

            return presignedRequest.url().toString();
        } catch (Exception e) {
            throw new Exception("Failed to get the file url", e);
        }

    }

}
