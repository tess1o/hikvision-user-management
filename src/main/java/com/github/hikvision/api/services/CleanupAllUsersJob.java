package com.github.hikvision.api.services;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@Slf4j
public class CleanupAllUsersJob implements Job {

    @SneakyThrows
    @Override
    public void execute(JobExecutionContext context) {
        log.info("Executing job to remove all users from hikvision");
        HikVisionService hikVisionService = (HikVisionService) context.getJobDetail().getJobDataMap().get("hikvisionService");
        hikVisionService.removeAll();
    }
}
