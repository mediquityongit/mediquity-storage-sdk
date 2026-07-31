package com.mediq.storage.model;

import lombok.Builder;
import lombok.Getter;

import java.io.InputStream;

@Getter
@Builder
public class StorageDownload {

    private byte[] bytes;

    private InputStream inputStream;

    private String contentType;

    private Long contentLength;


}