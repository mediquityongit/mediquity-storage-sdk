package com.mediq.storage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@Getter
@Setter
@ConfigurationProperties(prefix = "storage")
public class StorageProperties implements InitializingBean {

    /** oci (default) | local | s3 | azure */
    private String provider = "oci";

    private String bucketName;
    private String namespace;
    private String tenancyId;
    private String userId;
    private String fingerprint;
    private String privateKeyPath;
    private String region;

    /** Used when storage.provider=local */
    private String localBasePath;

    @Override
    public void afterPropertiesSet() {
        if (!"oci".equalsIgnoreCase(provider)) {
            return;
        }
        if (!StringUtils.hasText(bucketName)) {
            throw new IllegalStateException("storage.bucket-name is required.");
        }
        if (!StringUtils.hasText(namespace)) {
            throw new IllegalStateException("storage.namespace is required.");
        }
    }

    // Kept for OciStorageProvider helpers used by newer code paths
    public String resolveOciNamespace() {
        return namespace;
    }

    public String resolveOciBucket() {
        return bucketName;
    }
}
