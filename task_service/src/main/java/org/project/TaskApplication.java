package org.project;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Task API",
                version = "1.0",
                description = "Документация API"
        ),
        security = @SecurityRequirement(name = "oauth2")
)
@SecurityScheme(
        name = "oauth2",
        type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(
                authorizationCode = @OAuthFlow(
                        authorizationUrl = "${KEYCLOAK_URI:http://localhost:8080/realms/task}/protocol/openid-connect/auth",
                        tokenUrl         = "${KEYCLOAK_URI:http://localhost:8080/realms/task}/protocol/openid-connect/token",
                        scopes = {
                                @OAuthScope(name = "openid",  description = "OpenID scope"),
                                @OAuthScope(name = "profile", description = "User profile")
                        }
                )
        )
)
public class TaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskApplication.class, args);
    }

}
