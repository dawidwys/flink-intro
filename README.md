#Flink-intro

## Run docker containers

### Prerequisites

[docker](https://www.docker.com/products/overview) >= 1.12.5(tested) 
[docker-compose](https://docs.docker.com/compose/install/) >= 1.10

### Flink docker image

This code requires Flink version 1.2-SNAPSHOT (as of 19.01). To build flink docker image one has to build flink binary version oneself.
To do that checkout [flink](https://github.com/apache/flink/tree/release-1.2) and follow instructions in official dosc to build it.
Than copy `${FLINK_DIR}/flink-dist/target/flink-1.2-SNAPSHOT-bin/flink-1.2-SNAPSHOT` into `${FLINK_INTRO_DIR}/infrastructure/flink/archive`.

Than inside `${FLINK_INTRO_DIR}/infrastructure/flink` run:

```
    docker build -t flink .
```

### Run containers

You can start containers by invoking:

```
    docker-compose -f ${FLINK_INTRO_DIR}/infrastructure/docker-compose.yml up -d
```

Then configure your `/etc/hosts` file at least for jobmanager, kafka and grafana approprietly.
You can check container ip by running (example for jobmanager):

```
    docker inspect infrastructure_jobmanager_1
```

## Create fat jars

Invoke:

```
    sbt assembly
```

There are 5 interesting modules:

* flink-event-generator - generates events and stores them in kafka
* flink-sql - example of using sql
* flink-cep - example of using cep
* flink-queryable-job - example of queryable job
* flink-state-server - server for querying flink-queryable-job

flink-event-generator, flink-sql, flink-cep and flink-queryable-job take kafka address as parameter e.g. '-k kafka:9092'. 
Moreover for flink-event-generator one can specify the max time between generating two events  e.g. '-m 1000' 