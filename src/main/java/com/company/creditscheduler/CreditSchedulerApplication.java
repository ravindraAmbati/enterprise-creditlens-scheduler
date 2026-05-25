package com.company.creditscheduler;

import com.company.creditscheduler.config.SchedulerProperties;
import com.company.creditscheduler.config.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({SchedulerProperties.class, SecurityProperties.class})
public class CreditSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditSchedulerApplication.class, args);
    }
}
