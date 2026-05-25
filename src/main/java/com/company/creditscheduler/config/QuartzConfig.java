package com.company.creditscheduler.config;

import com.company.creditscheduler.scheduler.listeners.AutowiringSpringBeanJobFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    SchedulerFactoryBeanCustomizer schedulerFactoryBeanCustomizer(AutowireCapableBeanFactory beanFactory) {
        return factory -> factory.setJobFactory(new AutowiringSpringBeanJobFactory(beanFactory));
    }
}
