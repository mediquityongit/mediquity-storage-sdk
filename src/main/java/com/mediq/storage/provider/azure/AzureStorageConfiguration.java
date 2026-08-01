package com.mediq.storage.provider.azure;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.mediq.storage.config.StorageProperties;
import com.mediq.storage.provider.StorageProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnClass(BlobServiceClient.class)
@ConditionalOnProperty(
        prefix = "storage",
        name = "provider",
        havingValue = "azure",
        matchIfMissing = false)
public class AzureStorageConfiguration {

    @Bean
    @ConditionalOnMissingBean(BlobServiceClient.class)
    public BlobServiceClient blobServiceClient(StorageProperties properties) {
        StorageProperties.Azure azure = properties.getAzure();

        BlobServiceClientBuilder builder = new BlobServiceClientBuilder();

        if (StringUtils.hasText(azure.getConnectionString())) {
            builder.connectionString(azure.getConnectionString());
        } else {
            String endpoint = StringUtils.hasText(azure.getEndpoint())
                    ? azure.getEndpoint()
                    : "https://" + azure.getAccountName() + ".blob.core.windows.net";

            builder.endpoint(endpoint)
                    .credential(new StorageSharedKeyCredential(
                            azure.getAccountName(), azure.getAccountKey()));
        }

        return builder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean(BlobContainerClient.class)
    public BlobContainerClient blobContainerClient(
            BlobServiceClient blobServiceClient, StorageProperties properties) {
        return blobServiceClient.getBlobContainerClient(properties.resolveAzureContainer());
    }

    @Bean
    @ConditionalOnMissingBean(StorageProvider.class)
    public StorageProvider storageProvider(BlobContainerClient containerClient) {
        return new AzureBlobStorageProvider(containerClient);
    }
}
