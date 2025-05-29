Spring batch란?

배치 처리란 대량의 데이터를 일정 단위로 나누어 일괄 처리하는 방식이다. Spring Batch는 안정적인 트랜잭션 관리, 재시작, 스킵/재시도 기능, 메타 데이터 저장소 등을 제공하는 프레임 워크이다

메타 데이터 저장소란?

메타 데이터 저장소는 Spring Batch가 Job과 Step 실행에 필요한 상태 정보와 이력을 저장,관리하기 위해 사용하는 중앙 저장소입니다. 주로 관계형 데이터베이스를 활용하며, 다음과 같은 역할을 한다.

1. 실행 이력 관리
2. 동시성 제어
3. 재시작,복구 지원
4. ExecutionContext 저장


ExecutionContext는 Spring Batch에서 Job 실행 중에 공유하거나 중단된 지점부터 재시작할 때 필요한 정보를 키-값 형태로 저장,관리하는 구조체이다.

JobExecutionContext: Job 전체 범위에서 공유되는 데이터
StepExecutionContext: 각 Step 단위로 관리되는 데이터
크게 두가지 범위로 나뉘며, 이를 통해 배치 작업이 중간에 실패했을 때 마지막 커밋 시점부터 재실행하거나, 파티셔닝,분산 처리 시 컨텍스트를 전달할 수 있다.

메타 데이터 저장소 테이블 구조

BATCH_JOB_INSTANCE
같은 파라미터(JobParameters)로 수행된 Job 인스턴스 식별자 저장

BATCH_JOB_EXECUTION
각 Job 인스턴스 실행 기록(시작시간, 종료시간, 상태 등)

BATCH_JOB_EXECUTION_PARAMS
Job 실행 시 전달된 파라미터값

BATCH_JOB_EXECUTION_CONTEXT
Job 범위의 ExecutionContext(키·값 형태) 저장

BATCH_STEP_EXECUTION
각 Step 실행 기록(시작시간, 종료시간, 상태, 처리 건수 등)

BATCH_STEP_EXECUTION_CONTEXT
Step 범위의 ExecutionContext 저장




핵심 개념

Job
- 배치 프로세스의 단위(흐름 전체)
- 여러 Step을 순차 또는 병렬로 묶어 실행

Step
- Job을 구성하는 각각의 처리 단계
- 한 단계 내에서 Chunk 지향 처리 또는 Tasklet 처리

Chunk 기반 처리
- ItemReader → ItemProcessor → ItemWriter 의 순으로 N건씩 처리
- 예: chunk(100)이면 100건을 읽고, 처리하고, 저장

Tasklet 기반 처리
- 개발자가 한 번에 처리 로직을 구현할 때 사용
- 단순한 파일 삭제, 외부 시스템 호출 등에 유용

JobRepository & JobLauncher
- Job 실행 정보(파라미터, 상태, 메타데이터)를 저장/관리
- JobLauncher가 Job을 호출

JobParameters
- Job 실행 시점에 외부에서 입력하는 파라미터
- 재시작 시 중복 실행 방지를 위해 중요



Spring Batch 기본 코드
```
│           └── myspringbatch
│               ├── MySpringBatchApplication.java
│               ├── batch
│               │   ├── config
│               │   │   └── ShipmentJobConfig.java
│               │   ├── processor
│               │   │   └── ProcessorConfig.java
│               │   ├── reader
│               │   │   └── ReaderConfig.java
│               │   ├── scheduler
│               │   │   └── ShipmentJobScheduler.java
│               │   └── writer
│               │       └── WriterConfig.java
│               └── domain
│                   ├── Order.java
│                   └── Shipment.java
└── resources
└── application.yml
```
```
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
```

각 Step은 별개의 트랜잭션을 가진다. Job에서 .next()를 통해 Step을 순차적으로 진행 시키거나 splitFlow를 통해 병렬 실행 시킬 수 있다.


```
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
```
JdbcPagingItemReader를 통해 size를 나누어 읽고 처리할 수 있다. JpaPagingItemReader를 사용하여 하이버네이트를 사용할 수도있지만, 복잡한 join이 들어가는 경우에는 성능 저하로 인해 잘 사용하지 않을 듯 하다.


```
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
```
간단하게 주문 정보를 배송정보로 등록하는 과정이다. 현재 @Bean으로 등록하고있는데, 로직이 복잡해질 경우 @Component로 구현하고 복잡한 비즈니스로직은 Service로 빼서 사용해야 코드의 복잡도가 완화된다.


```
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
```
마지막으로 쓰기이다. 쓰기에서는 return으로 보내진 값만 데이터가 처리된다. 때문에 두가지 이상의 쿼리를 처리하려고 한다면 코드가 조금 복잡해진다. 실무에서는 write 과정에서 kafka메시지를 발송하고 다른 노드에서 처리 후 주문의 상태값을 바꿀것이다.



로직 동작 수행시 에러가 발생한다면(Step/Job 실패) Job 전체가 종료된다. 별도 설정이 없다면 처음부터 다시 시작한다.

.faultTolerant()                         // 예외 허용 모드
.skip(NullPointerException.class)       // NullPointer 발생하면 건너뜀
.skipLimit(10)
해당 코드처럼 실패시 건너뛰기도 가능하며, 재시도도 설정을 통해 가능하다.



