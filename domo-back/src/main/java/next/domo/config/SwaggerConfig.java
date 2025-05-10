package next.domo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "NE:XT DoMo 프로젝트 API 문서",
        version = "v1.0.0",
        description = "DoMo 백엔드 서비스 API 문서입니다."
    )
)
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("accessToken", new SecurityScheme()
                                .name("Authorization") // 헤더 이름
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("accessToken"))
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("NE:XT DoMo 프로젝트 API 문서")
                        .version("v1.0.0")
                        .description("회원가입 (/api/user/signup), 로그인 (/api/user/login) 시, accessToken과 refreshToken 모두 헤더로 전달\n" +
                                "(accessToken - Authorization 헤더, refreshToken - Authorization-refresh 헤더)\n" +
                                "\n" +
                                "accessToken 만료시,\n" +
                                "{\n" +
                                "    \"error\": \"엑세스 토큰이 유효하지 않습니다.\"\n" +
                                "}\n" +
                                "반환\n" +
                                "\n" +
                                "refreshToken 만료시,\n" +
                                "{\n" +
                                "    \"error\": \"리프레시 토큰이 유효하지 않습니다.\"\n" +
                                "}\n" +
                                "반환\n" +
                                "\n" +
                                "/api/user/signup, /api/user/login을 제외한 모든 api에서 accessToken을 Authorization 헤더에 담아 요청\n" +
                                "accessToken 만료시, refreshToken을 Authorization-refresh 헤더에 담아 요청 -> Authorization 헤더로 accessToken 반환")
                );
    }
}