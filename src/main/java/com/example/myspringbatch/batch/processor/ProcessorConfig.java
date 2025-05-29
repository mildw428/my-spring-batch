package com.example.myspringbatch.batch.processor;

import com.example.myspringbatch.domain.Order;
import com.example.myspringbatch.domain.Shipment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Slf4j
@Configuration
public class ProcessorConfig {

    @Bean
    public ItemProcessor<Order, Shipment> shipmentProcessor() {
        log.info("프로세스");
        return order -> {
            Shipment shipment = new Shipment();
            shipment.setOrderId(order.getId());
            shipment.setAddress(order.getAddress());
            shipment.setStatus("READY");
            shipment.setShippedAt(LocalDateTime.now());
            return shipment;
        };
    }
}
