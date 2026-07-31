package com.mediq.storage.autoconfigure;

import com.mediq.storage.config.OciConfiguration;
import com.mediq.storage.config.StorageProperties;
import com.mediq.storage.provider.StorageProvider;
import com.mediq.storage.provider.oci.OciStorageProvider;
import com.mediq.storage.service.StorageService;
import com.mediq.storage.service.impl.DefaultStorageService;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

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

        return new OciStorageProvider(
                client,
                properties);
    }

    @Bean
    public StorageService storageService(
            StorageProvider provider) {

        return new DefaultStorageService(provider);
    }

}