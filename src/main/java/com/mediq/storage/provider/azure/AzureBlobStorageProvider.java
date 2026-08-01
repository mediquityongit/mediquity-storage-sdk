package com.mediq.storage.provider.azure;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.mediq.storage.exception.StorageException;
import com.mediq.storage.model.StorageDownload;
import com.mediq.storage.model.StoredObject;
import com.mediq.storage.provider.StorageProvider;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class AzureBlobStorageProvider implements StorageProvider {

    private final BlobContainerClient containerClient;

    public AzureBlobStorageProvider(BlobContainerClient containerClient) {
        this.containerClient = containerClient;
        if (!containerClient.exists()) {
            containerClient.create();
        }
    }

    @Override
    public StoredObject upload(
            InputStream inputStream, String storageKey, String contentType, long contentLength) {
        try {
            BlobClient blob = containerClient.getBlobClient(storageKey);
            blob.upload(inputStream, contentLength, true);
            if (contentType != null) {
                blob.setHttpHeaders(new BlobHttpHeaders().setContentType(contentType));
            }
            BlobProperties props = blob.getProperties();
            return StoredObject.builder()
                    .storageKey(storageKey)
                    .contentType(contentType)
                    .size(contentLength)
                    .etag(props.getETag())
                    .build();
        } catch (Exception ex) {
            throw new StorageException("Failed to upload object to Azure Blob Storage.", ex);
        }
    }

    @Override
    public StorageDownload download(String storageKey) {
        try {
            BlobClient blob = containerClient.getBlobClient(storageKey);
            if (!blob.exists()) {
                throw new StorageException("Stored object not found: " + storageKey);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            blob.downloadStream(out);
            byte[] bytes = out.toByteArray();
            BlobProperties props = blob.getProperties();
            return StorageDownload.builder()
                    .bytes(bytes)
                    .contentType(props.getContentType())
                    .contentLength((long) bytes.length)
                    .build();
        } catch (StorageException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StorageException("Failed to download object from Azure Blob Storage.", ex);
        }
    }

    @Override
    public StorageDownload downloadStream(String storageKey) {
        try {
            BlobClient blob = containerClient.getBlobClient(storageKey);
            if (!blob.exists()) {
                throw new StorageException("Stored object not found: " + storageKey);
            }
            BlobProperties props = blob.getProperties();
            return StorageDownload.builder()
                    .inputStream(blob.openInputStream())
                    .contentType(props.getContentType())
                    .contentLength(props.getBlobSize())
                    .build();
        } catch (StorageException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StorageException("Failed to stream object from Azure Blob Storage.", ex);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            containerClient.getBlobClient(storageKey).deleteIfExists();
        } catch (BlobStorageException ex) {
            throw new StorageException("Failed to delete Azure blob.", ex);
        }
    }

    @Override
    public boolean exists(String storageKey) {
        try {
            return containerClient.getBlobClient(storageKey).exists();
        } catch (Exception ex) {
            throw new StorageException("Failed to check Azure blob.", ex);
        }
    }

    @Override
    public long size(String storageKey) {
        try {
            BlobClient blob = containerClient.getBlobClient(storageKey);
            if (!blob.exists()) {
                throw new StorageException("Stored object not found: " + storageKey);
            }
            return blob.getProperties().getBlobSize();
        } catch (StorageException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StorageException("Failed to get Azure blob size.", ex);
        }
    }
}
