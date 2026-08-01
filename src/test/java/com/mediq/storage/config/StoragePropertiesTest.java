package com.mediq.storage.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StoragePropertiesTest {

    @Test
    void defaultsToOci() {
        StorageProperties props = new StorageProperties();
        assertEquals("oci", props.getProvider());
    }

    @Test
    void ociRequiresBucketAndNamespace() {
        StorageProperties props = new StorageProperties();
        props.setProvider("oci");
        assertThrows(IllegalStateException.class, props::afterPropertiesSet);

        props.setBucketName("b");
        props.setNamespace("ns");
        assertDoesNotThrow(props::afterPropertiesSet);
    }

    @Test
    void localSkipsOciValidation() {
        StorageProperties props = new StorageProperties();
        props.setProvider("local");
        assertDoesNotThrow(props::afterPropertiesSet);
    }
}
