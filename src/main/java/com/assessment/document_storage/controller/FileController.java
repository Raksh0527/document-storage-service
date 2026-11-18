
package com.assessment.document_storage.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.assessment.document_storage.service.FileService;
import com.assessment.document_storage.dto.SearchResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(name = "File Management", description = "APIs for searching and downloading user documents")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/health")
    @Operation(summary = "Check application health",
            description = "Checks whether the Document Storage Service is running.")
    public String checkHealth() {
        return "Document Storage Service is running!";
    }

    @GetMapping("/{userName}/files/search")
    @Operation(summary = "Search user documents",
            description = "Searches for files by filename within the specified user's S3 folder.")
    public SearchResponse searchFiles(
            @Parameter(description = "Username whose documents need to be searched")
            @PathVariable String userName,

            @Parameter(description = "Full or partial filename to search")
            @RequestParam String name) {

        if (userName.isBlank() || name.isBlank()) {
            throw new IllegalArgumentException(
                "Username and search term cannot be empty"
            );
        }

        return fileService.searchFiles(userName, name);
    }

    @GetMapping("/{userName}/files/download")
    @Operation(summary = "Download user document",
            description = "Downloads a document from S3 after validating that it belongs to the specified user.")
    public ResponseEntity<InputStreamResource> downloadFile(
            @Parameter(description = "Username who owns the document")
            @PathVariable String userName,

            @Parameter(description = "Full S3 object key, for example sandy/logistics_invoice.pdf")
            @RequestParam String key) {

        var fileStream = fileService.downloadFile(userName, key);
        String fileName = key.substring(key.lastIndexOf("/") + 1);

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + fileName + "\""
            )
            .body(new InputStreamResource(fileStream));
    }
}
