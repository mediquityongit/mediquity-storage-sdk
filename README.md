# Mediq Storage SDK (Enterprise)

Multi-provider object storage for Mediq apps: **local** (default), **oci**, **s3**, **azure**.

Configure everything from `application.properties` or `application.yml` — fingerprint, private key path (or inline PEM), tenancy, bucket, etc.

## Maven

```xml
<dependency>
    <groupId>com.mediq</groupId>
    <artifactId>mediq-storage-sdk</artifactId>
    <version>1.0.5</version>
</dependency>

<!-- Required only when storage.provider=oci -->
<dependency>
    <groupId>com.oracle.oci.sdk</groupId>
    <artifactId>oci-java-sdk-objectstorage</artifactId>
    <version>3.69.0</version>
</dependency>
<dependency>
    <groupId>com.oracle.oci.sdk</groupId>
    <artifactId>oci-java-sdk-common-httpclient-jersey</artifactId>
    <version>3.69.0</version>
</dependency>
```

JitPack (after tagging a release):

```xml
<dependency>
    <groupId>com.github.mediquityongit</groupId>
    <artifactId>mediquity-storage-sdk</artifactId>
    <version>1.0.5</version>
</dependency>
```

## Configuration

### Local (default — CI / tests / no cloud keys)

**properties**
```properties
storage.provider=local
storage.local.base-path=${java.io.tmpdir}/mediq-storage
```

**yaml**
```yaml
storage:
  provider: local
  local:
    base-path: ${java.io.tmpdir}/mediq-storage
```

### OCI (production)

Flat style (existing HMS style — fully supported):

```properties
storage.provider=oci
storage.bucket-name=hims_config_E1
storage.namespace=bmnmmmkx3gxo
storage.region=ap-mumbai-1
storage.tenancy-id=ocid1.tenancy.oc1..aaaa...
storage.user-id=ocid1.user.oc1..aaaa...
storage.fingerprint=68:d7:86:7e:a0:56:c7:91:14:65:6c:73:50:e2:99:38
storage.private-key-path=C:/Users/Administrator/.oci/oci_api_key.pem
```

Nested enterprise style (recommended):

```yaml
storage:
  provider: oci
  bucket-name: hims_config_E1
  oci:
    namespace: bmnnmmkx3gxo
    region: ap-mumbai-1
    tenancy-id: ocid1.tenancy.oc1..aaaa...
    user-id: ocid1.user.oc1..aaaa...
    fingerprint: 68:d7:86:7e:a0:56:c7:91:14:65:6c:73:50:e2:99:38
    private-key-path: C:/Users/Administrator/.oci/oci_api_key.pem
    # Or inline PEM instead of a file path:
    # private-key: |
    #   -----BEGIN PRIVATE KEY-----
    #   ...
    #   -----END PRIVATE KEY-----
```

### AWS S3

```yaml
storage:
  provider: s3
  bucket-name: my-bucket
  s3:
    region: ap-south-1
    access-key: AKIA...
    secret-key: ...
```

### Azure Blob

```yaml
storage:
  provider: azure
  bucket-name: my-container
  azure:
    connection-string: DefaultEndpointsProtocol=https;...
```

## Usage

```java
@Autowired
private StorageService storageService;

StoredObject uploaded = storageService.upload(file,
        StorageUploadRequest.builder()
                .storageKey("facility/path/file.png")
                .contentType("image/png")
                .build());

byte[] bytes = storageService.download(uploaded.getStorageKey());
```

Do **not** component-scan `com.mediq.storage` — Spring Boot auto-configuration loads the correct provider from `storage.provider`.
