package com.mediq.storage.provider.s3;

import com.mediq.storage.config.StorageProperties;
import com.mediq.storage.exception.StorageException;
import com.mediq.storage.model.StorageDownload;
import com.mediq.storage.model.StoredObject;
import com.mediq.storage.provider.StorageProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.io.InputStream;

public class S3StorageProvider implements StorageProvider {

    private final S3Client s3Client;
    private final String bucketName;

    public S3StorageProvider(S3Client s3Client, StorageProperties properties) {
        this.s3Client = s3Client;
        this.bucketName = properties.resolveS3Bucket();
    }

    @Override
    public StoredObject upload(
            InputStream inputStream, String storageKey, String contentType, long contentLength) {
        try {
            PutObjectResponse response = s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(storageKey)
                            .contentType(contentType)
                            .contentLength(contentLength)
                            .build(),
                    RequestBody.fromInputStream(inputStream, contentLength));

            return StoredObject.builder()
                    .storageKey(storageKey)
                    .contentType(contentType)
                    .size(contentLength)
                    .etag(response.eTag())
                    .build();
        } catch (Exception ex) {
            throw new StorageException("Failed to upload object to S3.", ex);
        }
    }

    @Override
    public StorageDownload download(String storageKey) {
        try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(
                GetObjectRequest.builder().bucket(bucketName).key(storageKey).build())) {
            byte[] bytes = response.readAllBytes();
            return StorageDownload.builder()
                    .bytes(bytes)
                    .contentType(response.response().contentType())
                    .contentLength((long) bytes.length)
                    .build();
        } catch (NoSuchKeyException ex) {
            throw new StorageException("Stored object not found: " + storageKey, ex);
        } catch (IOException ex) {
            throw new StorageException("Failed to download object from S3.", ex);
        }
    }

    @Override
    public StorageDownload downloadStream(String storageKey) {
        try {
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(
                    GetObjectRequest.builder().bucket(bucketName).key(storageKey).build());
            return StorageDownload.builder()
                    .inputStream(response)
                    .contentType(response.response().contentType())
                    .contentLength(response.response().contentLength())
                    .build();
        } catch (NoSuchKeyException ex) {
            throw new StorageException("Stored object not found: " + storageKey, ex);
        } catch (Exception ex) {
            throw new StorageException("Failed to stream object from S3.", ex);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(storageKey).build());
        } catch (Exception ex) {
            throw new StorageException("Failed to delete S3 object.", ex);
        }
    }

    @Override
    public boolean exists(String storageKey) {
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(storageKey).build());
            return true;
        } catch (NoSuchKeyException ex) {
            return false;
        } catch (Exception ex) {
            throw new StorageException("Failed to check S3 object.", ex);
        }
    }

    @Override
    public long size(String storageKey) {
        try {
            HeadObjectResponse response = s3Client.headObject(
                    HeadObjectRequest.builder().bucket(bucketName).key(storageKey).build());
            return response.contentLength();
        } catch (NoSuchKeyException ex) {
            throw new StorageException("Stored object not found: " + storageKey, ex);
        } catch (Exception ex) {
            throw new StorageException("Failed to get S3 object size.", ex);
        }
    }
}
