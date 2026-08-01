package com.mediq.storage.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StoragePropertiesTest {

    @Test
    void defaultsToOciLike103() {
        assertEquals("oci", new StorageProperties().getProvider());
    }

    @Test
    void ociRequiresBucketAndNamespace() {
        StorageProperties props = new StorageProperties();
        assertThrows(IllegalStateException.class, props::afterPropertiesSet);
        props.setBucketName("b");
        props.setNamespace("ns");
        assertDoesNotThrow(props::afterPropertiesSet);
    }

    @Test
    void localAndS3AndAzureValidate() {
        StorageProperties local = new StorageProperties();
        local.setProvider("local");
        assertDoesNotThrow(local::afterPropertiesSet);

        StorageProperties s3 = new StorageProperties();
        s3.setProvider("s3");
        s3.setBucketName("b");
        s3.getS3().setRegion("ap-south-1");
        assertDoesNotThrow(s3::afterPropertiesSet);

        StorageProperties azure = new StorageProperties();
        azure.setProvider("azure");
        azure.setBucketName("c");
        azure.getAzure().setConnectionString("DefaultEndpointsProtocol=https;AccountName=x;AccountKey=y");
        assertDoesNotThrow(azure::afterPropertiesSet);
    }
}
