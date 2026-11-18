package com.assessment.document_storage.dto;

import java.util.List;

public record SearchResponse( 
    String userName,
    String searchTerm,
    int totalFiles,
    List<FileResponse> files
) {
    
}
