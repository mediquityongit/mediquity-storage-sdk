package com.mediq.storage.service;

import com.mediq.storage.model.StorageDownload;
import com.mediq.storage.model.StorageUploadRequest;
import com.mediq.storage.model.StoredObject;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageService {

    /**
     * Upload using Spring MultipartFile.
     * Convenience method for REST APIs.
     */
    StoredObject upload(
            MultipartFile file,
            StorageUploadRequest request);

    /**
     * Upload using InputStream.
     * Preferred for batch jobs, Kafka consumers,
     * schedulers, desktop applications, etc.
     */
    StoredObject upload(
            InputStream inputStream,
            StorageUploadRequest request,
            long contentLength);

    /**
     * Download object as bytes.
     */
    byte[] download(String storageKey);

    /**
     * Download object as stream.
     * Preferred for large files.
     */
    StorageDownload downloadStream(String storageKey);

    /**
     * Delete object.
     */
    void delete(String storageKey);

    /**
     * Check whether object exists.
     */
    boolean exists(String storageKey);

    /**
     * Returns object size.
     */
    long size(String storageKey);

}