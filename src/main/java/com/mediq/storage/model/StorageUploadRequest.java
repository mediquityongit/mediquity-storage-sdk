package com.mediq.storage.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class StorageUploadRequest {

    /**
     * Object path inside bucket.
     */
    private final String storageKey;

    /**
     * Content type.
     */
    private final String contentType;

    /**
     * Optional metadata.
     */
    private final Map<String, String> metadata;

}