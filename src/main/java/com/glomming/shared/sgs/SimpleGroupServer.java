package com.glomming.shared.sgs;

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;

/**
 * Main entry point for the simple group service.
 *
 * @author mahesh.subramanian
 */
public class SimpleGroupServer extends SpringBootServletInitializer implements ApplicationContextAware {

  @SuppressWarnings({"unused"})
  private ApplicationContext applicationContext;


  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }


  public static void main(String[] args) {

    System.setProperty("endpoints.autoconfig.enabled", "true");
    System.setProperty("endpoints.beans.enabled", "true");
    System.setProperty("endpoints.configprops.enabled", "true");
    System.setProperty("endpoints.dump.enabled", "true");
    System.setProperty("endpoints.env.enabled", "true");
    System.setProperty("endpoints.health.enabled", "true");
    System.setProperty("endpoints.info.enabled", "true");
    System.setProperty("endpoints.metrics.enabled", "true");
    System.setProperty("endpoints.mappings.enabled", "true");
    System.setProperty("endpoints.shutdown.enabled", "true");
    System.setProperty("endpoints.trace.enabled", "true");
    System.setProperty("endpoints.health.sensitive", "false");

    SpringApplication application = new SpringApplication(SimpleGroupServiceConfiguration.class, SimpleGroupServer.class);
    application.setShowBanner(false);
    application.run();
//        application.run("--debug");

  }

  @Autowired
  private SpringSwaggerConfig swaggerConfig;

  @Bean
  public SwaggerSpringMvcPlugin groupOnePlugin() {
    return new SwaggerSpringMvcPlugin(swaggerConfig);
//            .directModelSubstitute(LocalDate.class, String.class)
//            .directModelSubstitute(LocalDateTime.class, String.class);
  }

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(SimpleGroupServiceConfiguration.class, SimpleGroupServer.class);
  }
}
