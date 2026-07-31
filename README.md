# Mediq Storage SDK

## Maven

```xml
<dependency>
    <groupId>com.mediq</groupId>
    <artifactId>mediq-storage-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Configuration

```yaml
storage:
  provider: oci
  bucket-name: mediq-hims

oci:
  tenant-id:
  user-id:
  fingerprint:
  private-key-path:
  region:
  namespace:
```

## Usage

```java
@Autowired
private StorageService storageService;

StoredObject object = storageService.upload(request);

StorageDownload download = storageService.download(objectKey);
```
