package com.mediq.storage.provider;

import com.mediq.storage.model.StorageDownload;
import com.mediq.storage.model.StoredObject;

import java.io.InputStream;

public interface StorageProvider {

    StoredObject upload(
            InputStream inputStream,
            String storageKey,
            String contentType,
            long contentLength);

    StorageDownload download(String storageKey);

    StorageDownload downloadStream(String storageKey);

    void delete(String storageKey);

    boolean exists(String storageKey);

    long size(String storageKey);
}