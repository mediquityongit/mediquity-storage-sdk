package com.mediq.storage.model;

import lombok.Getter;
import lombok.Builder;

@Getter
@Builder
public class StoredObject {

    private final String storageKey;

    private final String contentType;

    private final Long size;

    private final String etag;

}