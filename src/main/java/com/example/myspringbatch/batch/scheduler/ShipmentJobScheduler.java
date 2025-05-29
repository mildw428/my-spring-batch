package com.example.myspringbatch.batch.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShipmentJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job shipmentJob;

//    @Scheduled(cron = "0 0 1 * * ?") // 매일 새벽 1시
    @Scheduled(cron = "* * * * * ?")
    public void runShipmentJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();
        jobLauncher.run(shipmentJob, params);
    }
}
