package com.github.codogma.codogmaback.util;

import com.github.codogma.codogmaback.exception.FileStorageException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileUploadUtil {

  @Value("${user.avatar.upload-dir}")
  private String avatarUploadDir;
  @Value("${article.image.upload-dir}")
  private String articleImageUploadDir;
  @Value("${category.image.upload-dir}")
  private String categoryImageUploadDir;
  @Value("${compilation.image.upload-dir}")
  private String compilationImageUploadDir;
  @Value("${server.servlet.context-path}")
  private String contextPath;

  public String uploadUserAvatar(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is empty or null");
    }

    String filename = "user-avatar-" + UUID.randomUUID() + getFileExtension(file);
    saveFile(file, avatarUploadDir, filename);
    return String.format("%s/users/avatars/%s", contextPath, filename);
  }

  public String uploadArticleImage(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is empty or null");
    }

    String filename = "article-image-" + UUID.randomUUID() + getFileExtension(file);
    saveFile(file, articleImageUploadDir, filename);
    return String.format("%s/articles/images/%s", contextPath, filename);
  }

  public String uploadCategoryImage(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is empty or null");
    }

    String filename = "category-image-" + UUID.randomUUID() + getFileExtension(file);
    saveFile(file, categoryImageUploadDir, filename);
    return String.format("%s/categories/images/%s", contextPath, filename);
  }

  public String uploadCompilationImage(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is empty or null");
    }

    String filename = "compilation-image-" + UUID.randomUUID() + getFileExtension(file);
    saveFile(file, compilationImageUploadDir, filename);
    return String.format("%s/compilations/images/%s", contextPath, filename);
  }

  private void saveFile(MultipartFile file, String dir, String filename) {
    try {
      Path path = Paths.get(dir).resolve(filename);
      Files.createDirectories(path.getParent());
      Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new FileStorageException(e.getMessage());
    }
  }

  private String getFileExtension(MultipartFile file) {
    String filename = file.getOriginalFilename();
    if (filename != null && filename.contains(".")) {
      return filename.substring(filename.lastIndexOf("."));
    }
    return "";
  }
}
