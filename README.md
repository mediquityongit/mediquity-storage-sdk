# Mediq Storage SDK

Multi-provider object storage for Spring Boot **2.6+** (and 3.x).

**Providers:** `oci` (default), `local`, `s3`, `azure`

## Maven

```xml
<dependency>
    <groupId>com.github.mediquityongit</groupId>
    <artifactId>mediquity-storage-sdk</artifactId>
    <version>1.0.8</version>
</dependency>
```

For `s3` / `azure`, also add the AWS or Azure SDK dependency yourself (they are optional).

## Config (same flat OCI style as 1.0.3)

```properties
storage.provider=oci
storage.bucket-name=hims_config_E1
storage.namespace=...
storage.region=ap-mumbai-1
storage.tenancy-id=...
storage.user-id=...
storage.fingerprint=68:d7:...
storage.private-key-path=C:/Users/you/.oci/oci_api_key.pem
```

```properties
storage.provider=local
storage.local-base-path=${java.io.tmpdir}/mediq-storage
```

```properties
storage.provider=s3
storage.bucket-name=my-bucket
storage.s3.region=ap-south-1
storage.s3.access-key=...
storage.s3.secret-key=...
```

```properties
storage.provider=azure
storage.bucket-name=my-container
storage.azure.connection-string=DefaultEndpointsProtocol=https;...
```

## Usage

```java
@Autowired
StorageService storageService;
```

Scan `com.mediq.storage` or rely on `spring.factories` auto-config.
