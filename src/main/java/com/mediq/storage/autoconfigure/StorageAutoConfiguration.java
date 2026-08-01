package com.mediq.storage.autoconfigure;

import com.mediq.storage.config.OciConfiguration;
import com.mediq.storage.config.StorageProperties;
import com.mediq.storage.provider.StorageProvider;
import com.mediq.storage.provider.local.LocalStorageProvider;
import com.mediq.storage.provider.oci.OciStorageProvider;
import com.mediq.storage.service.StorageService;
import com.mediq.storage.service.impl.DefaultStorageService;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

/**
 * Simple auto-configuration (same pattern as working 1.0.3).
 * Default provider = oci. Set storage.provider=local for tests/CI.
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
@Import(OciConfiguration.class)
public class StorageAutoConfiguration {

    @Bean
    @ConditionalOnProperty(
            prefix = "storage",
            name = "provider",
            havingValue = "oci",
            matchIfMissing = true)
    public StorageProvider storageProvider(
            ObjectStorageClient client,
            StorageProperties properties) {
        return new OciStorageProvider(client, properties);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "storage",
            name = "provider",
            havingValue = "local")
    public StorageProvider localStorageProvider(StorageProperties properties) {
        String basePath = properties.getLocalBasePath();
        if (!StringUtils.hasText(basePath)) {
            basePath = System.getProperty("java.io.tmpdir") + "/mediq-storage";
        }
        return new LocalStorageProvider(basePath);
    }

    /** Same as 1.0.3 — do NOT use @ConditionalOnBean here (breaks Boot 2.6 bean registration). */
    @Bean
    public StorageService storageService(StorageProvider provider) {
        return new DefaultStorageService(provider);
    }
}
