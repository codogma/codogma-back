package com.github.codogma.codogmaback.config;


import com.github.codogma.codogmaback.converter.localization.StringToLanguageConverter;
import com.github.codogma.codogmaback.converter.localization.StringToMapConverter;
import com.github.codogma.codogmaback.exception.ExceptionFactory;
import com.github.codogma.codogmaback.interceptor.localization.LocalizationInterceptor;
import com.github.codogma.codogmaback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;

@EnableAsync
@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration implements WebMvcConfigurer {

  private final ExceptionFactory exceptionFactory;
  private final LocalizationInterceptor localizationInterceptor;
  private final StringToMapConverter stringToMapConverter;
  private final StringToLanguageConverter stringToLanguageConverter;
  private final UserRepository userRepository;

  @Value("${user.avatar.upload-dir}")
  private String avatarUploadDir;
  @Value("${article.image.upload-dir}")
  private String articleImageUploadDir;
  @Value("${category.image.upload-dir}")
  private String categoryImageUploadDir;
  @Value("${compilation.image.upload-dir}")
  private String compilationImageUploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/users/avatars/**")
        .addResourceLocations("file:" + avatarUploadDir + "/");
    registry.addResourceHandler("/articles/images/**")
        .addResourceLocations("file:" + articleImageUploadDir + "/");
    registry.addResourceHandler("/categories/images/**")
        .addResourceLocations("file:" + categoryImageUploadDir + "/");
    registry.addResourceHandler("/compilations/images/**")
        .addResourceLocations("file:" + compilationImageUploadDir + "/").setCachePeriod(3600)
        .resourceChain(true).addResolver(new EncodedResourceResolver());
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(localizationInterceptor);
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(stringToMapConverter);
    registry.addConverter(stringToLanguageConverter);
  }

  @Bean
  UserDetailsService userDetailsService() {
    return usernameOrEmail -> userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
        .orElseThrow(exceptionFactory::usernameOrEmailNotFound);
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
    try {
      return config.getAuthenticationManager();
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize AuthenticationManager", e);
    }
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService());
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }
}
