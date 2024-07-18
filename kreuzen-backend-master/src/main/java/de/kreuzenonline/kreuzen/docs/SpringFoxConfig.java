package de.kreuzenonline.kreuzen.docs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
@Import(BeanValidatorPluginsConfiguration.class)
public class SpringFoxConfig {

    public static ApiInfo metadata() {
        return new ApiInfoBuilder()
                .title("Kreuzenonline")
                .description("Kreuzen backend documentation")
                .version("0.x")
                .build();
    }

    /**
     * Defines how the swagger documentation should be generated.
     *
     * @return Documentation
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(metadata())
                .select()
                .apis(RequestHandlerSelectors.basePackage("de.kreuzenonline.kreuzen"))
                .paths(PathSelectors.any())
                .build();
    }
}
