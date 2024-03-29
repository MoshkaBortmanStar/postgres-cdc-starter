# 🤖 Postgres CDC Starter

## Description

Postgres CDC (Change Data Capture) Starter is a tool designed for capturing changes in PostgreSQL tables and processing them in your application. It allows easy integration of change tracking functionality in your Spring Boot application.

## Key Features

- Automatic capturing of changes in PostgreSQL tables.
- Configuration of the list of tables to be monitored.
- Ability to add tables to the CDC publication for tracking their changes.
- Automatic processing and forwarding of changes to your application for further logic.


## Setup and Usage

To use Postgres CDC Starter in your project, you need to include it in your project's dependencies.

Example of adding dependency in `build.gradle`:

```groovy
dependencies {
    implementation 'com.bortmanco:postgres-cdc-starter:0.0.3'
}
``` 

In application.yml enable starter

```yml
postgres-cdc:
  decoder:
    enabled: true
```

##Starting the Engine

```java
PostgresCDCEngineImpl engine = PostgresCDCEngineImpl.builder()
                .slotName("test_starter")
                .engineName(engineName)
                .properties(dataSourceProperties)
                .pgoutHendler(pgoutHendler)
                .orchestrator(orchestrator)
                .changesStructureConsumer(changesStructureConsumer)
                .build();
```
- engineName -- the name of your engine, can be any.
- slotName -- the name of the replication slot that will be created in the database.
- properties -- properties for database connection.
- changesStructureConsumer -- Class that should implement the interface Consumer<List<RowChangesStructure>>, where you define your logic for handling the received records.
- orchestrator -- Class implementing CdcEngineOrchestrator interface and overriding its two methods void startEngine(String engineName) and void restartEngine(String engineName). This class should orchestrate the engines.

 ## Example of Implementing CdcEngineOrchestrator and Starting PostgresCDCEngine
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class ExampleCdcEngineOrchestratorService implements CdcEngineOrchestrator {

    private final PgoutHendler pgoutHendler;
    private final DataSourceProperties dataSourceProperties;
    private final Consumer<List<RowChangesStructure>> changesStructureConsumer;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @PostConstruct
    public void run() {
        startEngine("test_engine");
       /* ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> startEngine("test_engine"), 10, TimeUnit.SECONDS);*/
    }

    public void startEngine(String engineName) {
        PostgresCDCEngineImpl engine = PostgresCDCEngineImpl.builder()
                .slotName("test_starter")
                .engineName(engineName)
                .properties(dataSourceProperties)
                .pgoutHendler(pgoutHendler)
                .orchestrator(this)
                .changesStructureConsumer(changesStructureConsumer)
                .build();

        executorService.submit(engine);
    }

    public void restartEngine(String engineName) {
        startEngine(engineName);
    }
}
```

## Example of Implementing Consumer<List<RowChangesStructure>>

```java
@Slf4j
@Service
public class ExampleRowChangesStructureListConsumerService implements Consumer<List<RowChangesStructure>> {
    @Override
    public void accept(List<RowChangesStructure> rowChangesStructureList) {
        log.info("RowChangesStructureList: {}", rowChangesStructureList);
    }
}
```


## Add a Table for Tracking
Call the utility method:

```java
ReplicationSlotPublicationUtil.addTableToPublication(connection, publicationName, tableName)
```
- publicationName -- the name of the publication, equal to the replication slot name.
- tableName -- the name of the table with schema (public.table_name).

## Remove a Table from Tracking
Call the utility method:

```java
ReplicationSlotPublicationUtil.dropTableFromPublication(connection, publicationName, tableName)
```
- publicationName -- the name of the publication.
- tableName -- the table to be removed.

## Important: Clearing the Replication Slot
To clear, the starter automatically creates public.heartbeat_{slot_name} (the slot name defined when initializing the engine), which is added to the publication for reading upon creation of PostgresCDCEngine and initializes a record in it with id = 1 and created_at = current time.
Therefore, space in the replication slot can be released by creating a process to update this table at intervals of time, for example, every 5 minutes.

Example:

```java
  @Scheduled(initialDelay = 300000, fixedRate = 300000)
    public void updateHeartbeat() {
        try (var connection = dataSource.getConnection()) {
            ReplicationSlotPublicationUtil.updateHeartbeatTable(connection, "public.heartbeat_{your_slot_name_defined_in_PostgresCDCEngineImpl}");
        } catch (Exception e) {
            log.error("Error while updating heartbeat", e);
        }
    }
```
PostgresCDCEngineImpl is the class that acts as the engine and is launched to perform CDC using the run method.
your_slot_name_defined_in_PostgresCDCEngineImpl -- the name of the slot that you set when creating the engine using ReplicationSlotPublicationUtil.builder().slotName("test_starter")...
In this case, an engine will be created which in turn will create a replication slot named test_starter, a heartbeat_test_starter table, and will add a publication named test_starter (the name is the same as the replication slot) 
for reading changes in the tables added for tracking by the method - ReplicationSlotPublicationUtil.addTableToPublication(connection, publicationName, tableName).

## Example of Integration with postgres-cdc-starter

An example can be viewed in the git project - https://github.com/MoshkaBortmanStar/postgres-cdc-service
