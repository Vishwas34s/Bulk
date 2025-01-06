package com.Bulk.config;

import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.Bulk.service.AutowiringSpringBeanJobFactory;

@Configuration
public class QuartzConfig {

    private final AutowireCapableBeanFactory beanFactory;

    public QuartzConfig(AutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactory(JobFactory jobFactory) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setJobFactory(jobFactory);
        schedulerFactory.setOverwriteExistingJobs(true);
        schedulerFactory.setWaitForJobsToCompleteOnShutdown(true);
        schedulerFactory.setAutoStartup(true);

        // Add logging to ensure the scheduler is being created
        System.out.println("Initializing Quartz Scheduler");

        return schedulerFactory;
    }

    @Bean
    public AutowiringSpringBeanJobFactory jobFactory() {
        return new AutowiringSpringBeanJobFactory(beanFactory);
    }
}
