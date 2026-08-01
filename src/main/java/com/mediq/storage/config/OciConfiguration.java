package com.mediq.storage.config;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * OCI client beans — active when storage.provider=oci (default, same as 1.0.3).
 */
@Configuration
@ConditionalOnProperty(
        prefix = "storage",
        name = "provider",
        havingValue = "oci",
        matchIfMissing = true)
public class OciConfiguration {

    @Bean
    public AuthenticationDetailsProvider authenticationDetailsProvider(
            StorageProperties properties) throws IOException {

        if (StringUtils.hasText(properties.getPrivateKeyPath())) {
            Path keyPath = Path.of(properties.getPrivateKeyPath());
            if (!Files.isRegularFile(keyPath)) {
                throw new IllegalStateException(
                        "storage.private-key-path does not point to an existing file: "
                                + keyPath.toAbsolutePath());
            }
            String pem = Files.readString(keyPath);

            return SimpleAuthenticationDetailsProvider.builder()
                    .tenantId(properties.getTenancyId())
                    .userId(properties.getUserId())
                    .fingerprint(properties.getFingerprint())
                    .privateKeySupplier(() -> new ByteArrayInputStream(pem.getBytes()))
                    .region(Region.fromRegionCodeOrId(properties.getRegion()))
                    .build();
        }

        Path config = Path.of(System.getProperty("user.home"), ".oci", "config");
        return new ConfigFileAuthenticationDetailsProvider(config.toString(), "DEFAULT");
    }

    @Bean
    public ObjectStorageClient objectStorageClient(AuthenticationDetailsProvider provider) {
        return ObjectStorageClient.builder().build(provider);
    }
}
