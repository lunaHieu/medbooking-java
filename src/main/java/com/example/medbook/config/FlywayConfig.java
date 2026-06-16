package com.example.medbook.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.persistence.EntityManagerFactory;

@Configuration
public class FlywayConfig {
    @Bean
    public static BeanFactoryPostProcessor dependsOnPostProcessor() {
        return beanFactory -> {
            String[] jpaBeans = beanFactory.getBeanNamesForType(EntityManagerFactory.class);
            for (String beanName : jpaBeans) {
                var beanDefinition = beanFactory.getBeanDefinition(beanName);
                beanDefinition.setDependsOn("flyway");
            }
        };
    }
}
