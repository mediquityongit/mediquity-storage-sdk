package com.mediq.storage.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StoragePropertiesTest {

    @Test
    void defaultsToLocalWithoutCredentials() {
        StorageProperties props = new StorageProperties();
        assertEquals("local", props.resolveProvider());
        assertDoesNotThrow(props::validate);
    }

    @Test
    void ociAcceptsFlatFingerprintAndKeyPath() {
        StorageProperties props = new StorageProperties();
        props.setProvider("oci");
        props.setBucketName("bucket");
        props.setNamespace("ns");
        props.setRegion("ap-mumbai-1");
        props.setTenancyId("ocid1.tenancy...");
        props.setUserId("ocid1.user...");
        props.setFingerprint("68:d7:86:7e:a0:56:c7:91:14:65:6c:73:50:e2:99:38");
        props.setPrivateKeyPath("C:/Users/Administrator/.oci/oci_api_key.pem");

        assertDoesNotThrow(props::validate);
        assertEquals("68:d7:86:7e:a0:56:c7:91:14:65:6c:73:50:e2:99:38", props.resolveOciFingerprint());
        assertEquals("C:/Users/Administrator/.oci/oci_api_key.pem", props.resolveOciPrivateKeyPath());
    }

    @Test
    void ociNestedOverridesFlat() {
        StorageProperties props = new StorageProperties();
        props.setProvider("oci");
        props.setFingerprint("flat");
        props.getOci().setFingerprint("nested");
        props.getOci().setNamespace("ns");
        props.getOci().setRegion("ap-mumbai-1");
        props.getOci().setBucketName("b");
        props.getOci().setTenancyId("t");
        props.getOci().setUserId("u");
        props.getOci().setPrivateKey("-----BEGIN PRIVATE KEY-----\nX\n-----END PRIVATE KEY-----");

        assertDoesNotThrow(props::validate);
        assertEquals("nested", props.resolveOciFingerprint());
    }

    @Test
    void ociFailsWhenBucketMissing() {
        StorageProperties props = new StorageProperties();
        props.setProvider("oci");
        assertThrows(IllegalStateException.class, props::validate);
    }
}
