package com.glomming.shared.sgs.test;


import com.glomming.shared.sgs.SimpleGroupServiceConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = {SimpleGroupServiceConfiguration.class})
public class SimpleGroupServiceTestConfiguration {
}
