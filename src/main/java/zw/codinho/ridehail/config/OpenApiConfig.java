package zw.codinho.ridehail.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    OpenAPI rideHailingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ride Hailing Backend API")
                        .description("Driver onboarding, rider onboarding, fare quotes, dispatching, and ride lifecycle APIs.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Codinho Labs")
                                .email("hello@codinho.zw")
                                .url("https://codinho.zw")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .schemaRequirement(BEARER_SCHEME, new SecurityScheme()
                        .name(BEARER_SCHEME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"))
                .externalDocs(new ExternalDocumentation()
                        .description("Reference project conventions")
                        .url("https://storo.example/internal"));
    }
}
