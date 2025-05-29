package com.example.myspringbatch.batch.writer;

import com.example.myspringbatch.domain.Order;
import com.example.myspringbatch.domain.Shipment;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WriterConfig {

    @Bean
    public JdbcBatchItemWriter<Shipment> shipmentWriter(DataSource dataSource) {
        log.info("쓰기");

        // 1. Shipment INSERT
        return new JdbcBatchItemWriterBuilder<Shipment>()
                .dataSource(dataSource)
                .sql("INSERT INTO shipment (order_id, address, status, shipped_at) VALUES (:orderId, :address, :status, :shippedAt)")
                .beanMapped()
                .build();
    }
}
