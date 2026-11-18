
package com.assessment.document_storage.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.assessment.document_storage.dto.FileResponse;
import com.assessment.document_storage.dto.SearchResponse;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class FileService {

    private final S3Client s3Client;
    private final String bucketName;

    public FileService(
            S3Client s3Client,
            @Value("${aws.s3.bucket-name}") String bucketName) {

        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    // Search files from AWS S3
    public SearchResponse searchFiles(
            String userName,
            String searchTerm) {

        String prefix = userName + "/";

        List<FileResponse> matchingFiles = new ArrayList<>();

        ListObjectsV2Request request =
            ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        for (ListObjectsV2Response page :
                s3Client.listObjectsV2Paginator(request)) {

            for (S3Object object : page.contents()) {

                String key = object.key();

                String fileName =
                    key.substring(key.lastIndexOf("/") + 1);

                if (!fileName.isEmpty() &&
                    fileName.toLowerCase()
                        .contains(searchTerm.toLowerCase())) {

                    matchingFiles.add(
                        new FileResponse(fileName, key)
                    );
                }
            }
        }

        return new SearchResponse(
            userName,
            searchTerm,
            matchingFiles.size(),
            matchingFiles
        );
    }

    // Download file from AWS S3
    public ResponseInputStream<GetObjectResponse> downloadFile(
            String userName,
            String key) {

        String prefix = userName + "/";

        if (key == null ||
            !key.startsWith(prefix) ||
            key.length() <= prefix.length() ||
            key.contains("..") ||
            key.contains("\\")) {

            throw new IllegalArgumentException(
                "Invalid file key for this user"
            );
        }

        GetObjectRequest request =
            GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return s3Client.getObject(request);
    }
}
