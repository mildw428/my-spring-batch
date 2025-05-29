package com.example.myspringbatch.batch.config;

import com.example.myspringbatch.domain.Order;
import com.example.myspringbatch.domain.Shipment;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class ShipmentJobConfig {

    private final ItemReader<Order> orderReader;
    private final ItemProcessor<Order, Shipment> shipmentProcessor;
    private final ItemWriter<Shipment> shipmentWriter; // Jpa → Jdbc로 변경됨

    @Bean
    public Job shipmentJob(JobRepository jobRepository, Step shipmentStep) {
        return new JobBuilder("shipmentJob", jobRepository)
                .start(shipmentStep)
                .build();
    }

    @Bean
    public Step shipmentStep(JobRepository jobRepository,
                             PlatformTransactionManager transactionManager) {
        return new StepBuilder("shipmentStep", jobRepository)
                .<Order, Shipment>chunk(100, transactionManager)
                .reader(orderReader)
                .processor(shipmentProcessor)
                .writer(shipmentWriter)
                .build();
    }
}
