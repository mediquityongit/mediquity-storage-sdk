package com.mediq.storage.provider.oci;

import com.mediq.storage.config.StorageProperties;
import com.mediq.storage.model.StorageDownload;
import com.mediq.storage.model.StoredObject;
import com.mediq.storage.provider.StorageProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.HeadObjectRequest;
import lombok.RequiredArgsConstructor;
import com.mediq.storage.exception.StorageException;
import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.PutObjectResponse;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import org.springframework.util.StreamUtils;

import java.io.InputStream;

@RequiredArgsConstructor
public class OciStorageProvider  implements StorageProvider {

    private final ObjectStorageClient objectStorageClient;

    private final StorageProperties storageProperties;

    @Override
    public StoredObject upload(
            InputStream inputStream,
            String storageKey,
            String contentType,
            long contentLength) {

        try {

            PutObjectRequest request =
                    PutObjectRequest.builder()
                            .namespaceName(storageProperties.getNamespace())
                            .bucketName(storageProperties.getBucketName())
                            .objectName(storageKey)
                            .putObjectBody(inputStream)
                            .contentType(contentType)
                            .contentLength(contentLength)
                            .build();

            PutObjectResponse response =
                    objectStorageClient.putObject(request);

            return StoredObject.builder()
                    .storageKey(storageKey)
                    .contentType(contentType)
                    .size(contentLength)
                    .etag(response.getETag())
                    .build();

        } catch (BmcException ex) {

            throw new StorageException(
                    "Failed to upload object to OCI.",
                    ex);

        }
    }

    @Override
    public StorageDownload download(String storageKey) {

        try {

            GetObjectRequest request =
                    GetObjectRequest.builder()
                            .namespaceName(storageProperties.getNamespace())
                            .bucketName(storageProperties.getBucketName())
                            .objectName(storageKey)
                            .build();

            GetObjectResponse response =
                    objectStorageClient.getObject(request);

            byte[] bytes;

            try (InputStream inputStream = response.getInputStream()) {
                bytes = StreamUtils.copyToByteArray(inputStream);
            }

            return StorageDownload.builder()
                    .bytes(bytes)
                    .contentType(response.getContentType())
                    .contentLength(response.getContentLength())
                    .build();

        } catch (Exception ex) {

            throw new StorageException(
                    "Failed to download object from OCI.",
                    ex);
        }
    }

    @Override
    public StorageDownload downloadStream(String storageKey) {

        try {

            GetObjectRequest request =
                    GetObjectRequest.builder()
                            .namespaceName(storageProperties.getNamespace())
                            .bucketName(storageProperties.getBucketName())
                            .objectName(storageKey)
                            .build();

            GetObjectResponse response =
                    objectStorageClient.getObject(request);

            return StorageDownload.builder()
                    .inputStream(response.getInputStream())
                    .contentType(response.getContentType())
                    .contentLength(response.getContentLength())
                    .build();

        } catch (Exception ex) {

            throw new StorageException(
                    "Failed to stream object from OCI.",
                    ex);
        }
    }

    @Override
    public void delete(String storageKey) {

        try {

            DeleteObjectRequest request =
                    DeleteObjectRequest.builder()
                            .namespaceName(storageProperties.getNamespace())
                            .bucketName(storageProperties.getBucketName())
                            .objectName(storageKey)
                            .build();

            objectStorageClient.deleteObject(request);

        } catch (Exception ex) {

            throw new StorageException(
                    "Failed to delete object.",
                    ex);
        }
    }

    @Override
    public boolean exists(String storageKey) {

        try {

            objectStorageClient.headObject(
                    HeadObjectRequest.builder()
                            .namespaceName(storageProperties.getNamespace())
                            .bucketName(storageProperties.getBucketName())
                            .objectName(storageKey)
                            .build());

            return true;

        } catch (BmcException ex) {

            if (ex.getStatusCode() == 404) {
                return false;
            }

            throw new StorageException(
                    "Failed to check object.",
                    ex);
        }
    }

    @Override
    public long size(String storageKey) {

        try {

            return objectStorageClient.headObject(
                            HeadObjectRequest.builder()
                                    .namespaceName(storageProperties.getNamespace())
                                    .bucketName(storageProperties.getBucketName())
                                    .objectName(storageKey)
                                    .build())
                    .getContentLength();

        } catch (Exception ex) {

            throw new StorageException(
                    "Failed to get object size.",
                    ex);
        }
    }
}
