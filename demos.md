# Spring Cloud Config

## Spring Cloud Config Server elindítása

* `config-server-demo`
* `spring-cloud-config-server` függőség

```java
@EnableConfigServer
```

* `application.properties`

```properties
server.port=8888
spring.cloud.config.server.git.uri=file:///C:\\trainings\\config
spring.cloud.config.server.git.default-label=master
```


* Git repo: `C:\training\config\config-client-demo.properties`

```properties
demo.prefix = Hello
logging.level.training=debug
```

Ellenőrzés URL-en: `http://localhost:8888/config-client-demo/default`

## Spring Cloud Config Client elindítása

* `config-client-demo`
* `spring-boot-starter-web`, `spring-cloud-config-client`, `lombok`


```java
@Data
@ConfigurationProperties(prefix = "demo")
public class DemoProperties {

    private String prefix;
}
```

```java
@RestController
@AllArgsConstructor
@EnableConfigurationProperties(DemoProperties.class)
@Slf4j
public class HelloController {

    private DemoProperties demoProperties;

    @GetMapping("/api/hello")
    public Message hello() {
        log.debug("Hello called");
        return new Message(demoProperties.getPrefix() + name);
    }
}
```

* `application.properties`

```properties
spring.config.import=configserver:
spring.application.name=config-client-demo
```

# Spring Cloud Bus

## Kafka indítása

Apache Kafka is an open-source distributed event streaming platform.

`docker-compose.yaml`

```yaml
services:
  kafka:
    image: docker.io/bitnami/kafka:3.4.1
    ports:
      - "9092:9092"
    environment:
      - KAFKA_ENABLE_KRAFT=yes
      - KAFKA_CFG_PROCESS_ROLES=broker,controller
      - KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER 
      - KAFKA_CFG_LISTENERS=EXTERNAL://:9092,CLIENT://:9093,CONTROLLER://:9094
      - KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=EXTERNAL:PLAINTEXT,CLIENT:PLAINTEXT,CONTROLLER:PLAINTEXT
      - KAFKA_CFG_ADVERTISED_LISTENERS=EXTERNAL://127.0.0.1:9092,CLIENT://kafka:9093
      - KAFKA_CFG_INTER_BROKER_LISTENER_NAME=CLIENT
      - KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=1@127.0.0.1:9094
      - KAFKA_CFG_NODE_ID=1
      - KAFKA_BROKER_ID=1
      - ALLOW_PLAINTEXT_LISTENER=yes
  kafdrop:
    image: obsidiandynamics/kafdrop:4.0.1
    ports:
      - "9000:9000"
    environment:
      KAFKA_BROKERCONNECT: "kafka:9093"
      JVM_OPTS: "-Xms32M -Xmx64M"
      SERVER_SERVLET_CONTEXTPATH: "/"
    depends_on:
      - "kafka"
```

## Konfiguráció újratöltése futásidőben

server:

`spring-cloud-config-monitor`, `spring-cloud-starter-bus-kafka` függőség

client:

* `spring-cloud-starter-bus-kafka` függőség


## Spring Cloud Bus event kezelése

server:

`lombok` függőség

```java
public class ClearCachesEvent extends RemoteApplicationEvent {

  public ClearCachesEvent(Object source, String originService, Destination destination) {
    super(source, originService, destination);
  }

}
```

```java
@RestController
@AllArgsConstructor
public class ClearCacheController {

    private ApplicationEventPublisher applicationEventPublisher;

    private BusProperties busProperties;

    private Destination.Factory factory;

    @DeleteMapping("/api/caches")
    public void clear() {
        publisher.publishEvent(new ClearCachesEvent(this, busProperties.getId(), factory.getDestination("config-client-demo")));
    }
}
```

client:

`ClearCachesEvent`

* application

```java
@RemoteApplicationEventScan(basePackageClasses = ClearCachesEvent.class)
```

```java
@Component
@Slf4j
public class ClearCachesEventListener {

    @EventListener
    public void handleEvent(ClearCachesEvent event) {
        log.info("Event handled: {}", event);
    }
}
```

```http
DELETE http://localhost:8888/api/caches
```

## Spring Cloud Function használatba vétele

* `function-demo`
* Függőségek: Web, Function (`spring-cloud-function-context`, `spring-cloud-function-web`), Lombok

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculationRequest {

    private double a;

    private double b;
}
```

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalculationResponse {

    public double result;

}
```

```java
@Bean
public Function<CalculationRequest, CalculationResponse> calculate() {
  return request -> new CalculationResponse(request.getA() + request.getB());
}
```

```http
POST http://localhost:8080/calculate
Content-Type: application/json

{
  "a": 1.1234,
  "b": 2.2468
}
```

## Spring Cloud Function tesztelése

```java
package training.functiondemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.function.context.FunctionCatalog;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CalculateTest {

    @Autowired
    FunctionCatalog functionCatalog;

    @Test
    void calculate() {
        var function = (Function<CalculationRequest, CalculationResponse>) functionCatalog.lookup("calculate");
        var response = function.apply(new CalculationRequest(1.1234, 2.1234));
        assertEquals(3.2468, response.getResult(), 0.00005);
    }
}
```

## Function Composition (kerekítés két tizedesjegyre)

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoundResponse {

    private String result;
}
```

```java
public RoundResponse round() {
    return new RoundResponse(BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_UP).toString());
}
```

```java
@Bean
public Function<CalculationResponse, RoundResponse> round() {
    return response -> response.round();
}
```

## Spring Cloud Stream bevezetése Kafkával

* Függőségek: Stream, Kafka
* Futtatás
* `spring.cloud.function.definition`
* Kafka plugin
* Fogadás, küldés

## Topic-ok konfigurálása

```
spring.cloud.stream.function.bindings.calculate|round-in-0=calculation-request
spring.cloud.stream.function.bindings.calculate|round-out-0=calculation-response
```

## Alkalmazás bemutatása - backend

* Adatbázis

```shell
docker run -d -e POSTGRES_DB=employees -e POSTGRES_USER=employees -e POSTGRES_PASSWORD=employees -p 5432:5432 --name employees-postgres postgres
```

* Spring Boot alkalmazás, `pom.xml`
* Spring Data JPA, Spring MVC, RestController
* Alkalmazás elindítása
* SwaggerUI, `.http` file
* `application.yaml`
* Liquibase
* Felépítése: entity, repo, service, resource, controller
* Thymeleaf templates
* DataSource

## Alkalmazás bemutatása - frontend

* Spring Boot alkalmazás, `pom.xml`
* Spring Data JPA, Spring MVC, RestController
* Alkalmazás elindítása
* Felület
* `application.yaml`
* Liquibase
* Felépítése: entity, repo, service, resource, controller
* Thymeleaf templates
* DataSource

## Kafka üzenet küldése üzleti logikából

* Függőség: Stream, Kafka
* `CreateEmployeeCommand`

```java
@Service
@AllArgsConstructor
public class EmployeeBackendGateway {

    private StreamBridge streamBridge;

    public void createEmployee(Employee employee) {
        streamBridge.send("employee-backend-command",
                new CreateEmployeeCommand(employee.getName()));
    }

}
```

* Kafka topic

## Kafka üzenetfogadás és válasz

* Függőség: Stream, Kafka
* `CreateEmployeeCommand`
* `EmployeeHasBeenCreatedEvent`

```java
@Configuration(proxyBeanMethods = false)
@Slf4j
public class GatewayConfig {

    @Autowired
    private EmployeesService employeesService;

    @Bean
    public Function<CreateEmployeeCommand, EmployeeHasBeenCreatedEvent> createEmployee() {
        return command -> {
            var created = employeesService.createEmployee(new EmployeeResource(command.getName()));
            var event = new EmployeeHasBeenCreatedEvent(created.getId(), created.getName());
            log.debug("Event: {}", event);
            return event;
        };
    }
}
```

```
spring.cloud.stream.function.bindings.createEmployee-in-0=employee-backend-command
spring.cloud.stream.function.bindings.createEmployee-out-0=employee-backend-event
```

## Kafka üzenet fogadása

* `EmployeeHasBeenCreatedEvent`

```java
@Configuration(proxyBeanMethods = false)
@Slf4j
public class GatewayConfig {

    @Autowired
    private EmployeesService employeesService;

    @Bean
    public Consumer<EmployeeHasBeenCreatedEvent> employeeCreated() {
        return command -> log.debug("Event: {}", event);
    }
}
```

```
spring.cloud.stream.function.bindings.employeeCreated-in-0=employee-backend-event
```

## Spring Cloud Gateway használatba vétele

* Függőségek: Gateway

```yaml
server:
  port: 8000

spring:
  cloud:
    gateway:
      routes:
        - id: employees
          uri: http://localhost:8081/
          predicates:
            - Path=/api/employees/**            
```

## Header módosítása

```yaml
filters:
    - AddRequestHeader=X-Gateway, Hello
```

```
@RequestHeader HttpHeaders headers

log.debug("Headers: {}", headers);
```

## Circuit breaker beállítása

`pom.xml`

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
</dependency>

<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

`application.yaml`

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: employees-backend
          uri: http://localhost:8081
          predicates:
            - Path=/api/employees/**
          filters:
            - AddRequestHeader=X-Gateway, Hello
            - CircuitBreaker=employees-backend

resilience4j.circuitbreaker:
  configs:
    default:
      slidingWindowSize: 10
      minimumNumberOfCalls: 5
      permittedNumberOfCallsInHalfOpenState: 3
      waitDurationInOpenState: 30s
  instances:
    employees-backend:
      baseConfig: default

management:
  endpoints:
    web:
      exposure:
        include: "*"
```

```http
### Circuit breakers
GET http://localhost:8000/actuator/circuitbreakers

### List employees
GET http://localhost:8000/api/employees
```


`Internal Server Error` -> `Service Unavailable`

## Fallback beállítása

* Lombok

```java
@RestController
@RequestMapping("/api/dummy-employees")
public class EmployeeController {

    @GetMapping
    public Flux<EmployeeResource> employees() {
        return Flux.fromIterable(List.of(new EmployeeResource(0L, "Dummy Employee")));
    }
}
```

```yaml
- name: CircuitBreaker
  args:
    name: employees-backend
    fallbackUri: forward:/api/dummy-employees
```

# Spring Cloud Task

## Spring Cloud Task használatba vétele

```shell
docker run -d -e POSTGRES_DB=tasks -e POSTGRES_USER=tasks -e POSTGRES_PASSWORD=tasks -p 5432:5432  --name tasks-postgres postgres
```

* Függőségek: Task, PostgreSQL Driver, Lombok

```java
@SpringBootApplication
@Slf4j
@EnableTask
public class TasksDemoApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(TasksDemoApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.debug("Random number: {}", new Random().nextInt(1, 7));
    }
}
```

```properties
logging.level.training=debug

spring.datasource.url=jdbc:postgresql://localhost:5432/tasks
spring.datasource.username=tasks
spring.datasource.password=tasks

spring.application.name=tasks-demo
```

## Launcher használata

* Név: `task-launcher`
* Függőségek: Task, Cloud Stream, Spring for Apache Kafka, PostgreSQL
* `@EnableTaskLauncher`
* `pom.xml`

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-deployer-local</artifactId>
    <version>2.8.3</version>
</dependency>
```

* `application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/tasks
spring.datasource.username=tasks
spring.datasource.password=tasks
```

```json
{"applicationName": "tasks-demo", "uri": "maven:training:task-demo:0.0.1-SNAPSHOT", "commandlineArguments": []}
```

`Caused by: org.eclipse.aether.transfer.ArtifactNotFoundException: Could not find artifact training:task-demo:jar:0.0.1-SNAPSHOT`

* `TaskDemoApplicationTest` -> `TaskDemoApplicationIT`
* `mvnw install`