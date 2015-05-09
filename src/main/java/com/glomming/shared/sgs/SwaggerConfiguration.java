package com.glomming.shared.sgs;

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.models.dto.ApiInfo;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableSwagger
@ComponentScan("com.glomming.shared.drs.controller")
public class SwaggerConfiguration {

  private SpringSwaggerConfig springSwaggerConfig;

  @Autowired
  public void setSpringSwaggerConfig(SpringSwaggerConfig springSwaggerConfig) {
    this.springSwaggerConfig = springSwaggerConfig;
  }

  @Bean
  public SwaggerSpringMvcPlugin customImplementation() {
    return new SwaggerSpringMvcPlugin(this.springSwaggerConfig)
        .apiInfo(apiInfo())
        .apiVersion(SwaggerConfiguration.class.getPackage().getImplementationVersion() != null ? SwaggerConfiguration.class.getPackage().getImplementationVersion() : "1.0")
        .includePatterns(".*" + ServiceName.SERVICE_NAME + ".*");
  }

  private ApiInfo apiInfo() {
    ApiInfo apiInfo = new ApiInfo(
        "Dynamo REST Service",
        "Dynamo REST service stores generic attributes in Dynamo DB",
        "",
        "",
        "",
        ""
    );
    return apiInfo;
  }
}
