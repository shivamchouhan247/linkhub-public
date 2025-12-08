package com.scm.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
public String uploadFile(MultipartFile file) throws Exception;
public String generatePresignedUrl(String fileName) throws Exception;
}
    