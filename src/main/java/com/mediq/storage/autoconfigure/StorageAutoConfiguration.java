package com.mediq.storage.autoconfigure;

import com.mediq.storage.config.StorageProperties;
import com.mediq.storage.provider.StorageProvider;
import com.mediq.storage.provider.azure.AzureStorageConfiguration;
import com.mediq.storage.provider.local.LocalStorageConfiguration;
import com.mediq.storage.provider.oci.OciStorageConfiguration;
import com.mediq.storage.provider.s3.S3StorageConfiguration;
import com.mediq.storage.service.StorageService;
import com.mediq.storage.service.impl.DefaultStorageService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Enterprise multi-provider storage auto-configuration.
 * Active backend is selected by {@code storage.provider} (default: {@code local}).
 */
@AutoConfiguration
@EnableConfigurationProperties(StorageProperties.class)
@Import({
        LocalStorageConfiguration.class,
        OciStorageConfiguration.class,
        S3StorageConfiguration.class,
        AzureStorageConfiguration.class
})
public class StorageAutoConfiguration {

    @Bean
    @ConditionalOnBean(StorageProvider.class)
    @ConditionalOnMissingBean(StorageService.class)
    public StorageService storageService(StorageProvider provider) {
        return new DefaultStorageService(provider);
    }
}
