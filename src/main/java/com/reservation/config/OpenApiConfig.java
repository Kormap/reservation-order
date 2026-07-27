package com.reservation.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openAPI() {
        String csrfSchemeName = "csrfToken";

        return new OpenAPI()
                .info(new Info()
                        .title("Reservation Order API")
                        .version("v1")
                        .description("회원, 상품, 재고, 예약 주문 MVP API. 로그인 후 발급되는 SESSION 쿠키로 인증합니다."))
                .addSecurityItem(new SecurityRequirement().addList(csrfSchemeName))
                .components(new Components().addSecuritySchemes(csrfSchemeName,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-XSRF-TOKEN")))
                .paths(new Paths().addPathItem("/api/v1/auth/login", new PathItem()
                        .post(new Operation()
                                .tags(List.of("인증"))
                                .summary("로그인")
                                .description("이메일과 비밀번호로 로그인하고 SESSION 쿠키를 발급합니다.")
                                .security(List.of())
                                .requestBody(new RequestBody()
                                        .required(true)
                                        .content(new Content().addMediaType("application/json",
                                                new io.swagger.v3.oas.models.media.MediaType()
                                                        .schema(loginRequestSchema()))))
                                .responses(new ApiResponses()
                                        .addApiResponse("200", new ApiResponse().description("로그인 성공"))
                                        .addApiResponse("400", new ApiResponse().description("잘못된 요청"))
                                        .addApiResponse("401", new ApiResponse().description("인증 실패"))))));
    }

    private ObjectSchema loginRequestSchema() {
        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("email", new StringSchema()
                .format("email")
                .example("member@gmail.com"));
        schema.addProperty("password", new StringSchema()
                .format("password")
                .example("password123"));
        schema.required(List.of("email", "password"));
        return schema;
    }
}
