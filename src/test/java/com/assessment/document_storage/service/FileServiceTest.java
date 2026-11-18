
package com.assessment.document_storage.service;

import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.assessment.document_storage.dto.SearchResponse;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private S3Client s3Client;

    private FileService fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileService(
            s3Client,
            "rakshith-user-document-storage"
        );
    }

    // Test 1: Search files successfully
    @Test
    void shouldSearchFilesForUser() {

        S3Object file1 = S3Object.builder()
            .key("sandy/logistics_invoice.pdf")
            .build();

        S3Object file2 = S3Object.builder()
            .key("sandy/logistics_report.pdf")
            .build();

        S3Object file3 = S3Object.builder()
            .key("sandy/employee_details.txt")
            .build();

        ListObjectsV2Response response =
            ListObjectsV2Response.builder()
                .contents(file1, file2, file3)
                .build();

        ListObjectsV2Iterable iterable =
    mock(ListObjectsV2Iterable.class);

when(iterable.iterator())
    .thenReturn(List.of(response).iterator());

when(s3Client.listObjectsV2Paginator(
    any(ListObjectsV2Request.class)))
    .thenReturn(iterable);

        SearchResponse result =
            fileService.searchFiles("sandy", "logistics");

        assertEquals(2, result.totalFiles());
        assertEquals("sandy", result.userName());
        assertEquals("logistics", result.searchTerm());
    }

    // Test 2: No matching files
    @Test
    void shouldReturnZeroWhenNoFilesMatch() {

        ListObjectsV2Response response =
            ListObjectsV2Response.builder()
                .contents(
                    S3Object.builder()
                        .key("sandy/resume.pdf")
                        .build()
                )
                .build();

      ListObjectsV2Iterable iterable =
    mock(ListObjectsV2Iterable.class);

   when(iterable.iterator())
    .thenReturn(List.of(response).iterator());

   when(s3Client.listObjectsV2Paginator(
    any(ListObjectsV2Request.class)))
    .thenReturn(iterable);  

        SearchResponse result =
            fileService.searchFiles("sandy", "logistics");

        assertEquals(0, result.totalFiles());
        assertTrue(result.files().isEmpty());
    }

    // Test 3: Prevent access to another user's file
    @Test
    void shouldRejectAnotherUsersFile() {

        assertThrows(
            IllegalArgumentException.class,
            () -> fileService.downloadFile(
                "sandy",
                "rahul/logistics_invoice.pdf"
            )
        );

        verifyNoInteractions(s3Client);
    }
}

