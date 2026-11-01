
package com.dsabg.customroom.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class BattleSchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler battleTaskScheduler() {
        ThreadPoolTaskScheduler scheduler =
                new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(8);
        scheduler.setThreadNamePrefix("battle-countdown-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);

        return scheduler;
    }
}
