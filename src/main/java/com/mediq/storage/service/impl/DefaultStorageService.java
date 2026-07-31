package com.mediq.storage.service.impl;

import com.mediq.storage.exception.StorageException;
import com.mediq.storage.model.StorageDownload;
import com.mediq.storage.model.StorageUploadRequest;
import com.mediq.storage.model.StoredObject;
import com.mediq.storage.provider.StorageProvider;
import com.mediq.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
public class DefaultStorageService implements StorageService {

    private final StorageProvider storageProvider;

    @Override
    public StoredObject upload(
            MultipartFile file,
            StorageUploadRequest request) {

        try {

            return storageProvider.upload(
                    file.getInputStream(),
                    request.getStorageKey(),
                    request.getContentType(),
                    file.getSize());

        } catch (IOException ex) {

            throw new StorageException(
                    "Unable to read uploaded file.",
                    ex);
        }
    }

    @Override
    public StoredObject upload(
            InputStream inputStream,
            StorageUploadRequest request,
            long contentLength) {

        return storageProvider.upload(
                inputStream,
                request.getStorageKey(),
                request.getContentType(),
                contentLength);
    }

    @Override
    public byte[] download(String storageKey) {

        return storageProvider.download(storageKey)
                .getBytes();
    }

    @Override
    public StorageDownload downloadStream(String storageKey) {

        return storageProvider.downloadStream(storageKey);
    }

    @Override
    public void delete(String storageKey) {

        storageProvider.delete(storageKey);
    }

    @Override
    public boolean exists(String storageKey) {
        return storageProvider.exists(storageKey);
    }

    @Override
    public long size(String storageKey) {
        return storageProvider.size(storageKey);
    }
}
