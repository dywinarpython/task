package org.project;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@OpenAPIDefinition(
        info = @Info(
                title = "Notifications API",
                version = "1.0",
                description = "Документация API"
        ),
        security = @SecurityRequirement(name = "oauth2")
)
@SecurityScheme(
        name = "oauth2",
        type = SecuritySchemeType.OAUTH2,
        flows = @io.swagger.v3.oas.annotations.security.OAuthFlows(
                authorizationCode = @io.swagger.v3.oas.annotations.security.OAuthFlow(
                        authorizationUrl = "http://localhost:8080/realms/task/protocol/openid-connect/auth",
                        tokenUrl = "http://localhost:8080/realms/task/protocol/openid-connect/token",
                        scopes = {
                                @io.swagger.v3.oas.annotations.security.OAuthScope(
                                        name = "openid",
                                        description = "Доступ к идентификации пользователя"
                                ),
                                @io.swagger.v3.oas.annotations.security.OAuthScope(
                                        name = "profile",
                                        description = "Доступ к информации профиля"
                                )
                        }
                )
        )
)
@EnableScheduling
@SpringBootApplication
public class NotificationsServiceApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run( NotificationsServiceApplication.class, args);
    }
}
