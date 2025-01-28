package com.github.codogma.codogmaback.interceptor.localization;

import com.github.codogma.codogmaback.model.Language;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class LocalizationInterceptor implements HandlerInterceptor {

  private final LocalizationContext localizationContext;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) {
    Language intl = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
        .filter(cookie -> "intl".equals(cookie.getName())).map(Cookie::getValue)
        .map(Language::fromCode).filter(Objects::nonNull).findFirst().orElseGet(() -> {
          String acceptLanguage = request.getHeader("Accept-Language");
          if (acceptLanguage != null) {
            return Arrays.stream(acceptLanguage.split(",")).map(lang -> lang.split(";")[0])
                .map(String::trim).map(Language::fromCode).filter(Objects::nonNull).findFirst()
                .orElse(Language.EN);
          }
          return Language.EN;
        });

    localizationContext.setLanguage(intl);

    String contlCookie = Arrays.stream(
            Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
        .filter(cookie -> "contl".equals(cookie.getName())).map(Cookie::getValue).findFirst()
        .orElse(Language.EN.getCode());
    contlCookie = URLDecoder.decode(contlCookie, StandardCharsets.UTF_8);
    String[] languages = contlCookie.split(",");
    List<Language> supportedLanguages = Arrays.stream(languages).map(String::trim)
        .map(Language::fromCode).toList();
    localizationContext.setSupportedLanguages(supportedLanguages);
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) {
    localizationContext.clear();
  }
}