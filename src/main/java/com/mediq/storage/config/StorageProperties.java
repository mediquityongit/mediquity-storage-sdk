package com.mediq.storage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * storage.provider = oci (default) | local | s3 | azure
 *
 * Flat OCI fields work like 1.0.3 (storage.fingerprint, storage.private-key-path, …).
 * S3/Azure use storage.s3.* / storage.azure.* (or shared storage.bucket-name).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "storage")
public class StorageProperties implements InitializingBean {

    private String provider = "oci";

    private String bucketName;

    // OCI (flat — same as 1.0.3 / HMS application.properties)
    private String namespace;
    private String tenancyId;
    private String userId;
    private String fingerprint;
    private String privateKeyPath;
    private String region;

    // local
    private String localBasePath;

    private final S3 s3 = new S3();
    private final Azure azure = new Azure();

    @Getter
    @Setter
    public static class S3 {
        private String bucketName;
        private String region;
        private String accessKey;
        private String secretKey;
        private String endpoint;
    }

    @Getter
    @Setter
    public static class Azure {
        private String connectionString;
        private String containerName;
        private String accountName;
        private String accountKey;
        private String endpoint;
    }

    public String resolveLocalBasePath() {
        if (StringUtils.hasText(localBasePath)) {
            return localBasePath;
        }
        return System.getProperty("java.io.tmpdir") + "/mediq-storage";
    }

    public String resolveOciNamespace() {
        return namespace;
    }

    public String resolveOciBucket() {
        return bucketName;
    }

    public String resolveS3Bucket() {
        return StringUtils.hasText(s3.bucketName) ? s3.bucketName : bucketName;
    }

    public String resolveAzureContainer() {
        return StringUtils.hasText(azure.containerName) ? azure.containerName : bucketName;
    }

    @Override
    public void afterPropertiesSet() {
        String p = provider == null ? "oci" : provider.trim().toLowerCase();
        switch (p) {
            case "oci":
                require(bucketName, "storage.bucket-name is required for OCI.");
                require(namespace, "storage.namespace is required for OCI.");
                break;
            case "s3":
                require(resolveS3Bucket(), "storage.bucket-name or storage.s3.bucket-name is required for S3.");
                require(s3.region, "storage.s3.region is required for S3.");
                break;
            case "azure":
                require(resolveAzureContainer(), "storage.bucket-name or storage.azure.container-name is required for Azure.");
                if (!StringUtils.hasText(azure.connectionString)
                        && !(StringUtils.hasText(azure.accountName) && StringUtils.hasText(azure.accountKey))) {
                    throw new IllegalStateException(
                            "storage.azure.connection-string or account-name/account-key is required.");
                }
                break;
            case "local":
                break;
            default:
                throw new IllegalStateException(
                        "Unsupported storage.provider='" + provider + "'. Use oci, local, s3, or azure.");
        }
    }

    private static void require(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(message);
        }
    }
}
