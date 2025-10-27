package com.github.codogma.codogmaback.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

public class UrlMultipartFile implements MultipartFile {

  private final byte[] content;
  private final String name;
  private final String originalFilename;
  private final String contentType;

  public UrlMultipartFile(String url) {
    RestTemplate restTemplate = new RestTemplate();
    this.content = restTemplate.getForObject(url, byte[].class);
    this.name = "avatar-from-url";
    this.originalFilename = getFilenameFromUrl(url);
    this.contentType = "image/jpeg"; // GitHub аватары обычно в JPEG
  }

  private String getFilenameFromUrl(String url) {
    int lastSlash = url.lastIndexOf('/');
    if (lastSlash != -1 && lastSlash < url.length() - 1) {
      return url.substring(lastSlash + 1);
    }
    return "avatar.jpg";
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getOriginalFilename() {
    return originalFilename;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public boolean isEmpty() {
    return content == null || content.length == 0;
  }

  @Override
  public long getSize() {
    return content.length;
  }

  @Override
  public byte[] getBytes() throws IOException {
    return content;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new ByteArrayInputStream(content);
  }

  @Override
  public void transferTo(File dest) throws IOException, IllegalStateException {
    new FileOutputStream(dest).write(content);
  }
}
