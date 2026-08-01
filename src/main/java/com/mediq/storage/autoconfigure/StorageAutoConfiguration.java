package com.mediq.storage.autoconfigure;

import com.mediq.storage.config.OciConfiguration;
import com.mediq.storage.config.StorageProperties;
import com.mediq.storage.provider.StorageProvider;
import com.mediq.storage.provider.azure.AzureStorageConfiguration;
import com.mediq.storage.provider.local.LocalStorageProvider;
import com.mediq.storage.provider.oci.OciStorageProvider;
import com.mediq.storage.provider.s3.S3StorageConfiguration;
import com.mediq.storage.service.StorageService;
import com.mediq.storage.service.impl.DefaultStorageService;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Multi-provider storage (oci default, local, s3, azure).
 * Same bean pattern as working 1.0.3 — StorageService has NO @ConditionalOnBean.
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
@Import({
        OciConfiguration.class,
        S3StorageConfiguration.class,
        AzureStorageConfiguration.class
})
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
    @ConditionalOnProperty(prefix = "storage", name = "provider", havingValue = "local")
    public StorageProvider localStorageProvider(StorageProperties properties) {
        return new LocalStorageProvider(properties.resolveLocalBasePath());
    }

    /**
     * Critical for Spring Boot 2.6: do not use @ConditionalOnBean(StorageProvider).
     * That condition fails at registration time and leaves HMS without StorageService.
     */
    @Bean
    public StorageService storageService(StorageProvider provider) {
        return new DefaultStorageService(provider);
    }
}
