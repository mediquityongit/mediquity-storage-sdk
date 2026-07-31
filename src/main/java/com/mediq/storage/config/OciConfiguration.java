package com.mediq.storage.config;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.Region;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class OciConfiguration {

    @Bean
    public AuthenticationDetailsProvider authenticationDetailsProvider(
            StorageProperties properties) throws IOException {

        if (StringUtils.hasText(properties.getPrivateKeyPath())) {

            String pem =
                    Files.readString(Path.of(properties.getPrivateKeyPath()));

            return SimpleAuthenticationDetailsProvider.builder()
                    .tenantId(properties.getTenancyId())
                    .userId(properties.getUserId())
                    .fingerprint(properties.getFingerprint())
                    .privateKeySupplier(
                            () -> new ByteArrayInputStream(pem.getBytes()))
                    .region(Region.fromRegionCodeOrId(properties.getRegion()))
                    .build();
        }

        Path config =
                Path.of(System.getProperty("user.home"),
                        ".oci",
                        "config");
        return new ConfigFileAuthenticationDetailsProvider(
                config.toString(),
                "DEFAULT");
    }

    @Bean
    public ObjectStorageClient objectStorageClient(
            AuthenticationDetailsProvider provider) {

        return ObjectStorageClient.builder().build(provider);
    }

}