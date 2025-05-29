package com.example.myspringbatch.batch.reader;

import com.example.myspringbatch.domain.Order;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ReaderConfig {

    @Bean
    public JdbcPagingItemReader<Order> orderReader(DataSource dataSource) throws Exception {
        log.info("읽기");

        JdbcPagingItemReader<Order> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setPageSize(100);
        reader.setRowMapper(new BeanPropertyRowMapper<>(Order.class));

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("SELECT id, address, order_date, processed");
        queryProvider.setFromClause("FROM `order`");
        queryProvider.setWhereClause("processed = false");
        queryProvider.setSortKey("id");

        reader.setQueryProvider(queryProvider.getObject());
        return reader;
    }
}