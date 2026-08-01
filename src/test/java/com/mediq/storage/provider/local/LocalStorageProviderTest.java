package com.mediq.storage.provider.local;

import com.mediq.storage.model.StorageDownload;
import com.mediq.storage.model.StoredObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalStorageProviderTest {

    @TempDir
    Path tempDir;

    private LocalStorageProvider provider;

    @BeforeEach
    void setUp() {
        provider = new LocalStorageProvider(tempDir.toString());
    }

    @Test
    void uploadDownloadDeleteRoundTrip() {
        byte[] payload = "enterprise-sdk-ok".getBytes(StandardCharsets.UTF_8);
        String key = "demo00/manual-prescription-template/doctor/doc1/file.png";

        StoredObject uploaded = provider.upload(
                new ByteArrayInputStream(payload),
                key,
                "image/png",
                payload.length);

        assertEquals(key, uploaded.getStorageKey());
        assertTrue(provider.exists(key));
        assertEquals(payload.length, provider.size(key));

        StorageDownload download = provider.download(key);
        assertArrayEquals(payload, download.getBytes());

        provider.delete(key);
        assertFalse(provider.exists(key));
    }

    @Test
    void rejectsPathTraversal() {
        assertThrows(
                Exception.class,
                () -> provider.upload(
                        new ByteArrayInputStream(new byte[] {1}),
                        "../outside.txt",
                        "text/plain",
                        1));
    }
}
