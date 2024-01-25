package com.github.hikvision.api.services;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@Slf4j
public class CleanupTrigger {

    @SneakyThrows
    public void scheduleCleanupJob(Scheduler scheduler, String cron, HikVisionService hikVisionService) {
        log.info("Scheduling a user cleanup job with cron :{}", cron);
        JobDetail job = JobBuilder.newJob(CleanupAllUsersJob.class)
                .withIdentity("CleanupAllUsersJob", "Cleanup")
                .build();
        job.getJobDataMap().put("hikvisionService", hikVisionService);

        CronTrigger trigger = newTrigger()
                .withIdentity("CleanupAllUsersJobTrigger", "Cleanup")
                .withSchedule(cronSchedule(cron))
                .build();

        scheduler.scheduleJob(job, trigger);
    }
}
