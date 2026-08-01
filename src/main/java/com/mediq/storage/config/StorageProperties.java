package com.mediq.storage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * Enterprise storage configuration bound from {@code application.properties} or {@code application.yml}.
 *
 * <p>Select backend with {@code storage.provider}: {@code local} (default), {@code oci}, {@code s3}, {@code azure}.
 *
 * <p>OCI credentials may be set either nested ({@code storage.oci.*}) or flat ({@code storage.fingerprint}, …)
 * for backward compatibility with existing HMS configs.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "storage")
public class StorageProperties implements InitializingBean {

    /**
     * Active provider: local | oci | s3 | azure.
     * Defaults to local so CI / developer machines work without cloud credentials.
     */
    private String provider = "local";

    /** Shared logical bucket / container name (used by oci, s3, azure when provider-specific name is empty). */
    private String bucketName;

    // ── Flat OCI fields (backward compatible with HMS application.properties) ──
    private String namespace;
    private String tenancyId;
    private String userId;
    private String fingerprint;
    private String privateKeyPath;
    /** Inline PEM content (alternative to privateKeyPath). Prefer env/secret injection. */
    private String privateKey;
    private String region;

    private final Local local = new Local();
    private final Oci oci = new Oci();
    private final S3 s3 = new S3();
    private final Azure azure = new Azure();

    @Getter
    @Setter
    public static class Local {
        /** Root directory for file storage. Defaults under {@code java.io.tmpdir}/mediq-storage. */
        private String basePath;
    }

    @Getter
    @Setter
    public static class Oci {
        private String namespace;
        private String tenancyId;
        private String userId;
        private String fingerprint;
        /** Absolute or relative path to the OCI API private key PEM file. */
        private String privateKeyPath;
        /** Inline PEM private key (use instead of private-key-path when preferred). */
        private String privateKey;
        private String region;
        private String bucketName;
        /** OCI config profile when falling back to ~/.oci/config (default: DEFAULT). */
        private String configProfile = "DEFAULT";
    }

    @Getter
    @Setter
    public static class S3 {
        private String bucketName;
        private String region;
        private String accessKey;
        private String secretKey;
        /** Custom endpoint for MinIO / LocalStack / compatible stores. */
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

    // ── Resolvers (nested wins over flat) ───────────────────────────────────

    public String resolveProvider() {
        return provider == null ? "local" : provider.trim().toLowerCase();
    }

    public String resolveOciNamespace() {
        return firstNonBlank(oci.getNamespace(), namespace);
    }

    public String resolveOciTenancyId() {
        return firstNonBlank(oci.getTenancyId(), tenancyId);
    }

    public String resolveOciUserId() {
        return firstNonBlank(oci.getUserId(), userId);
    }

    public String resolveOciFingerprint() {
        return firstNonBlank(oci.getFingerprint(), fingerprint);
    }

    public String resolveOciPrivateKeyPath() {
        return firstNonBlank(oci.getPrivateKeyPath(), privateKeyPath);
    }

    public String resolveOciPrivateKey() {
        return firstNonBlank(oci.getPrivateKey(), privateKey);
    }

    public String resolveOciRegion() {
        return firstNonBlank(oci.getRegion(), region);
    }

    public String resolveOciBucket() {
        return firstNonBlank(oci.getBucketName(), bucketName);
    }

    public String resolveOciConfigProfile() {
        return StringUtils.hasText(oci.getConfigProfile()) ? oci.getConfigProfile() : "DEFAULT";
    }

    public String resolveS3Bucket() {
        return firstNonBlank(s3.getBucketName(), bucketName);
    }

    public String resolveAzureContainer() {
        return firstNonBlank(azure.getContainerName(), bucketName);
    }

    public String resolveLocalBasePath() {
        if (StringUtils.hasText(local.getBasePath())) {
            return local.getBasePath();
        }
        return System.getProperty("java.io.tmpdir") + "/mediq-storage";
    }

    @Override
    public void afterPropertiesSet() {
        validate();
    }

    public void validate() {
        switch (resolveProvider()) {
            case "oci":
                validateOci();
                break;
            case "s3":
                validateS3();
                break;
            case "azure":
                validateAzure();
                break;
            case "local":
                break;
            default:
                throw new IllegalStateException(
                        "Unsupported storage.provider='"
                                + provider
                                + "'. Supported values: local, oci, s3, azure.");
        }
    }

    private void validateOci() {
        require(resolveOciBucket(), "storage.bucket-name (or storage.oci.bucket-name) is required for OCI.");
        require(resolveOciNamespace(), "storage.namespace (or storage.oci.namespace) is required for OCI.");
        require(resolveOciRegion(), "storage.region (or storage.oci.region) is required for OCI.");

        boolean hasInlineKey = StringUtils.hasText(resolveOciPrivateKey());
        boolean hasKeyPath = StringUtils.hasText(resolveOciPrivateKeyPath());

        if (hasInlineKey || hasKeyPath) {
            require(resolveOciTenancyId(), "storage.tenancy-id (or storage.oci.tenancy-id) is required for OCI.");
            require(resolveOciUserId(), "storage.user-id (or storage.oci.user-id) is required for OCI.");
            require(resolveOciFingerprint(), "storage.fingerprint (or storage.oci.fingerprint) is required for OCI.");
        }
        // else: auth will fall back to ~/.oci/config at bean creation time
    }

    private void validateS3() {
        require(resolveS3Bucket(), "storage.bucket-name or storage.s3.bucket-name is required for S3.");
        require(s3.getRegion(), "storage.s3.region is required for S3.");
    }

    private void validateAzure() {
        require(
                resolveAzureContainer(),
                "storage.bucket-name or storage.azure.container-name is required for Azure.");
        if (!StringUtils.hasText(azure.getConnectionString())
                && !(StringUtils.hasText(azure.getAccountName())
                        && StringUtils.hasText(azure.getAccountKey()))) {
            throw new IllegalStateException(
                    "For Azure set storage.azure.connection-string "
                            + "or storage.azure.account-name + storage.azure.account-key.");
        }
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (StringUtils.hasText(primary)) {
            return primary.trim();
        }
        if (StringUtils.hasText(fallback)) {
            return fallback.trim();
        }
        return null;
    }

    private static void require(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(message);
        }
    }
}
