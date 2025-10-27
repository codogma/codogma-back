package com.github.codogma.codogmaback.handler.oauth;

import com.github.codogma.codogmaback.model.Role;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.util.FileUploadUtil;
import com.github.codogma.codogmaback.util.UrlMultipartFile;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class GitlabOAuth2ProviderHandler implements OAuth2ProviderHandler {

  private final FileUploadUtil fileUploadUtil;

  @Override
  public boolean supports(final String registrationId) {
    return "gitlab".equals(registrationId);
  }

  @Override
  public UserModel processOAuth2User(final OAuth2User oAuth2User) {
    final Integer gitlabId = oAuth2User.getAttribute("id");
    final String username = oAuth2User.getAttribute("username");
    final String email = oAuth2User.getAttribute("email");
    final String avatarUrl = oAuth2User.getAttribute("avatar_url");

    String uploadedAvatarUrl = null;
    if (avatarUrl != null && !avatarUrl.isEmpty()) {
      try {
        MultipartFile avatarFile = new UrlMultipartFile(avatarUrl);
        uploadedAvatarUrl = fileUploadUtil.uploadUserAvatar(avatarFile);
      } catch (IllegalArgumentException e) {
        // Логирование ошибки можно добавить здесь
      }
    }

    return UserModel.builder().gitlabId(gitlabId).username(username).email(email)
        .avatarUrl(uploadedAvatarUrl).role(Role.ROLE_USER).enabled(true).build();
  }
}