package com.mediq.storage.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@Getter
@Setter
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /**
     * Supported:
     * oci
     * s3
     * local
     */
    private String provider = "oci";

    private String bucketName;

    private String namespace;

    private String tenancyId;

    private String userId;

    private String fingerprint;

    private String privateKeyPath;

    private String region;

    @PostConstruct
    public void validate() {

        if (!"oci".equalsIgnoreCase(provider)) {
            return;
        }

        if (!StringUtils.hasText(bucketName)) {
            throw new IllegalStateException(
                    "storage.bucket-name is required.");
        }

        if (!StringUtils.hasText(namespace)) {
            throw new IllegalStateException(
                    "storage.namespace is required.");
        }
    }
}