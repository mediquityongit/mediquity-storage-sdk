package com.mediq.storage.provider.local;

import com.mediq.storage.config.StorageProperties;
import com.mediq.storage.provider.StorageProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        prefix = "storage",
        name = "provider",
        havingValue = "local",
        matchIfMissing = true)
public class LocalStorageConfiguration {

    @Bean
    @ConditionalOnMissingBean(StorageProvider.class)
    public StorageProvider storageProvider(StorageProperties properties) {
        return new LocalStorageProvider(properties.resolveLocalBasePath());
    }
}
