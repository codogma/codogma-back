package com.github.codogma.codogmaback.handler.oauth;

import com.github.codogma.codogmaback.model.Role;
import com.github.codogma.codogmaback.model.UserModel;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class GithubOAuth2ProviderHandler implements OAuth2ProviderHandler {

  @Override
  public boolean supports(final String registrationId) {
    return "github".equals(registrationId);
  }

  @Override
  public UserModel processOAuth2User(final OAuth2User oAuth2User) {
    final Integer githubId = oAuth2User.getAttribute("id");
    final String username = oAuth2User.getAttribute("login");
    final String email = oAuth2User.getAttribute("email");

    return UserModel.builder().githubId(githubId).username(username).email(email)
        .role(Role.ROLE_USER).build();
  }
}