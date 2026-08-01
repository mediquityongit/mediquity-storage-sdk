package com.mediq.storage.provider.local;

import com.mediq.storage.exception.StorageException;
import com.mediq.storage.model.StorageDownload;
import com.mediq.storage.model.StoredObject;
import com.mediq.storage.provider.StorageProvider;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * Filesystem-backed storage. Maps each storage key to a file under {@code basePath}.
 */
public class LocalStorageProvider implements StorageProvider {

    private final Path basePath;

    public LocalStorageProvider(String basePath) {
        try {
            this.basePath = Path.of(basePath).toAbsolutePath().normalize();
            Files.createDirectories(this.basePath);
        } catch (IOException ex) {
            throw new StorageException("Unable to create local storage directory: " + basePath, ex);
        }
    }

    @Override
    public StoredObject upload(
            InputStream inputStream,
            String storageKey,
            String contentType,
            long contentLength) {

        Path target = resolve(storageKey);

        try {
            Files.createDirectories(target.getParent());
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);

            long size = Files.size(target);

            return StoredObject.builder()
                    .storageKey(storageKey)
                    .contentType(contentType)
                    .size(size)
                    .etag(Long.toHexString(size) + "-" + Files.getLastModifiedTime(target).toMillis())
                    .build();

        } catch (IOException ex) {
            throw new StorageException("Failed to upload object to local storage: " + storageKey, ex);
        }
    }

    @Override
    public StorageDownload download(String storageKey) {
        Path target = resolve(storageKey);

        try {
            if (!Files.isRegularFile(target)) {
                throw new StorageException("Stored object not found: " + storageKey);
            }

            byte[] bytes = Files.readAllBytes(target);

            return StorageDownload.builder()
                    .bytes(bytes)
                    .contentType(probeContentType(target))
                    .contentLength((long) bytes.length)
                    .build();

        } catch (StorageException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new StorageException("Failed to download object from local storage: " + storageKey, ex);
        }
    }

    @Override
    public StorageDownload downloadStream(String storageKey) {
        Path target = resolve(storageKey);

        try {
            if (!Files.isRegularFile(target)) {
                throw new StorageException("Stored object not found: " + storageKey);
            }

            InputStream stream = Files.newInputStream(target, StandardOpenOption.READ);

            return StorageDownload.builder()
                    .inputStream(stream)
                    .contentType(probeContentType(target))
                    .contentLength(Files.size(target))
                    .build();

        } catch (StorageException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new StorageException("Failed to stream object from local storage: " + storageKey, ex);
        }
    }

    @Override
    public void delete(String storageKey) {
        Path target = resolve(storageKey);

        try {
            Files.deleteIfExists(target);
        } catch (IOException ex) {
            throw new StorageException("Failed to delete local object: " + storageKey, ex);
        }
    }

    @Override
    public boolean exists(String storageKey) {
        return Files.isRegularFile(resolve(storageKey));
    }

    @Override
    public long size(String storageKey) {
        Path target = resolve(storageKey);

        try {
            if (!Files.isRegularFile(target)) {
                throw new StorageException("Stored object not found: " + storageKey);
            }
            return Files.size(target);
        } catch (StorageException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new StorageException("Failed to get local object size: " + storageKey, ex);
        }
    }

    Path resolve(String storageKey) {
        if (!StringUtils.hasText(storageKey)) {
            throw new StorageException("Storage key is required.");
        }

        String normalizedKey = storageKey.replace('\\', '/');
        while (normalizedKey.startsWith("/")) {
            normalizedKey = normalizedKey.substring(1);
        }

        Path resolved = basePath.resolve(normalizedKey).normalize();

        if (!resolved.startsWith(basePath)) {
            throw new StorageException("Invalid storage key (path traversal denied): " + storageKey);
        }

        return resolved;
    }

    private static String probeContentType(Path target) throws IOException {
        String type = Files.probeContentType(target);
        return type != null ? type : "application/octet-stream";
    }

    /** Exposed for tests / diagnostics. */
    public Path getBasePath() {
        return basePath;
    }

    /** Writes bytes helper used by some adapters. */
    public void writeBytes(String storageKey, byte[] data) {
        Path target = resolve(storageKey);
        try {
            Files.createDirectories(target.getParent());
            try (OutputStream out = Files.newOutputStream(
                    target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                out.write(data);
            }
        } catch (IOException ex) {
            throw new StorageException("Failed to write local object: " + storageKey, ex);
        }
    }
}
