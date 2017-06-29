package com.turlygazhy.tasks;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    @Bean
     SchedulingTasks schedulingTasks() {
        return new SchedulingTasks();
    }
}
