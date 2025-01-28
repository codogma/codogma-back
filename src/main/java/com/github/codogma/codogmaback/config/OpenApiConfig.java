package com.github.codogma.codogmaback.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "IT Blog API", version = "0.9.25", description = "API documentation for IT Blog", contact = @Contact(name = "Email", email = "your.email@example.com"), license = @License(name = "Apache 2.0", url = "http://springdoc.org")), tags = @Tag(name = "Oauth2 authorization", description = "API for OAuth authorization"))
@SecuritySchemes({
    @SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT", in = SecuritySchemeIn.HEADER)})
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI().path("/oauth2/authorization/github", new PathItem().get(
            new Operation().summary("GitHub Authorization").description(
                    "Redirects the user to the GitHub authorization page. After successful authorization, GitHub will redirect back to your frontend.")
                .operationId("githubLogin").tags(List.of("Oauth2 authorization")).responses(
                    new ApiResponses().addApiResponse("302",
                        new ApiResponse().description("Redirect to GitHub")))))
        .path("/oauth2/authorization/gitlab", new PathItem().get(
            new Operation().summary("GitLab Authorization").description(
                    "Redirects the user to the GitLab authorization page. After successful authorization, GitLab will redirect back to your frontend.")
                .operationId("gitlabLogin").tags(List.of("Oauth2 authorization")).responses(
                    new ApiResponses().addApiResponse("302",
                        new ApiResponse().description("Redirect to GitLab")))));
  }
}