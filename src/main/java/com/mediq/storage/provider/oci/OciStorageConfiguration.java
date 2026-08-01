package com.mediq.storage.provider.oci;

import com.mediq.storage.config.StorageProperties;
import com.mediq.storage.provider.StorageProvider;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
@ConditionalOnClass(ObjectStorageClient.class)
@ConditionalOnProperty(
        prefix = "storage",
        name = "provider",
        havingValue = "oci",
        matchIfMissing = false)
public class OciStorageConfiguration {

    @Bean
    @ConditionalOnMissingBean(AuthenticationDetailsProvider.class)
    public AuthenticationDetailsProvider authenticationDetailsProvider(
            StorageProperties properties) throws IOException {

        String inlinePem = properties.resolveOciPrivateKey();
        String keyPath = properties.resolveOciPrivateKeyPath();

        if (StringUtils.hasText(inlinePem) || StringUtils.hasText(keyPath)) {
            String pem = resolvePemContent(inlinePem, keyPath);

            return SimpleAuthenticationDetailsProvider.builder()
                    .tenantId(properties.resolveOciTenancyId())
                    .userId(properties.resolveOciUserId())
                    .fingerprint(properties.resolveOciFingerprint())
                    .privateKeySupplier(
                            () -> new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8)))
                    .region(Region.fromRegionCodeOrId(properties.resolveOciRegion()))
                    .build();
        }

        Path config = Path.of(System.getProperty("user.home"), ".oci", "config");
        if (!Files.isRegularFile(config)) {
            throw new IllegalStateException(
                    "OCI credentials not configured. Set either:\n"
                            + "  - storage.private-key-path (or storage.oci.private-key-path) "
                            + "+ tenancy-id, user-id, fingerprint, region\n"
                            + "  - storage.private-key (or storage.oci.private-key) inline PEM\n"
                            + "  - or create an OCI config file at "
                            + config.toAbsolutePath());
        }

        return new ConfigFileAuthenticationDetailsProvider(
                config.toString(), properties.resolveOciConfigProfile());
    }

    @Bean
    @ConditionalOnMissingBean(ObjectStorageClient.class)
    public ObjectStorageClient objectStorageClient(AuthenticationDetailsProvider provider) {
        return ObjectStorageClient.builder().build(provider);
    }

    @Bean
    @ConditionalOnMissingBean(StorageProvider.class)
    public StorageProvider storageProvider(
            ObjectStorageClient client, StorageProperties properties) {
        return new OciStorageProvider(client, properties);
    }

    private static String resolvePemContent(String inlinePem, String keyPath) throws IOException {
        if (StringUtils.hasText(inlinePem)) {
            return inlinePem.trim();
        }

        Path path = Path.of(keyPath.trim());
        if (!Files.isRegularFile(path)) {
            throw new IllegalStateException(
                    "OCI private key file not found. "
                            + "Check storage.private-key-path / storage.oci.private-key-path: "
                            + path.toAbsolutePath()
                            + ". On Windows use a path like C:/Users/<you>/.oci/oci_api_key.pem. "
                            + "Or set storage.private-key / storage.oci.private-key with the PEM content.");
        }
        return Files.readString(path);
    }
}
