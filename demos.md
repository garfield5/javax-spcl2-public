# Spring Cloud bevezetés

## Bevezetés a Spring Cloud használatába

Lsd. slide-ok.

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
spring.cloud.config.server.git.uri=file:///C:\\trainings\\javax-spcl2\\config
spring.cloud.config.server.git.default-label=master
```


* Git repo: `C:\trainings\javax-spcl2\config\config-client-demo.properties`

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

* Kafdrop: `http://localhost:9000`

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

## Kódolt értékek

```shell
keytool -genkeypair -alias config-server-key -keyalg RSA -keysize 4096 -sigalg SHA512withRSA -dname "CN=Config Server,OU=Spring Cloud,O=Training" -keypass changeit -keystore config-server.jks -storepass changeit
```

* `pom.xml`


```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-rsa</artifactId>
</dependency>
```

* `application.properties`

```properties
encrypt.keyStore.location=file:config-server.jks
encrypt.keyStore.password=changeit
encrypt.keyStore.alias=config-server-key
encrypt.keyStore.secret=changeit
```

```http
###
POST http://localhost:8888/encrypt

HelloEncoded
```

* `config-client-demo.properties`

```
demo.prefix = {cipher}AgC1eiSxapB6BOni6Y7t6mL+u...
```

```http
# Hello
GET http://localhost:8080/api/hello
```

## HashiCorp Vault Backend

* Tárolni: tokenek, jelszavak, tanúsítványok, kulcsok
* Webes felület, CLI, HTTP API

```
docker run -d --cap-add=IPC_LOCK -e VAULT_DEV_ROOT_TOKEN_ID=myroot -p 8200:8200 --name=vault hashicorp/vault
```

* `IPC_LOCK` - érzékeny adatokat ne swappelje a diskre
* http://localhost:8200
* Token bejelentkezés, `myroot`

```shell
docker exec -it vault sh
```

```shell
export VAULT_ADDR='http://127.0.0.1:8200'
export VAULT_TOKEN='myroot'
vault kv put secret/config-client-demo demo.prefix=HelloFromVault
```

```properties
#spring.cloud.config.server.git.uri=file:///C:\\trainings\\javax-spcl2\\config

spring.profiles.active=vault
spring.cloud.config.server.vault.kv-version=2
spring.cloud.config.server.vault.authentication=TOKEN
spring.cloud.config.server.vault.token=myroot
```

* Server indítása után a `config-server-demo.http` fájlba

```http
###
GET http://localhost:8888/config-client-demo/default
```

```http
###
GET http://localhost:8080/api/hello
```

## Frissítés HashiCorp Vault Backenddel

```shell
vault kv put secret/config-client-demo demo.prefix=HelloFromVault2
```

* Hook a `/monitor` címen, ha van `org.springframework.cloud:spring-cloud-config-monitor` és `org.springframework.cloud:spring-cloud-starter-bus-kafka`
 függőség.
  * `Content-Type` `application/x-www-form-urlencoded`
  * `path` értéke az alkalmazás neve

```http
###
POST http://localhost:8888/monitor
Content-Type: application/x-www-form-urlencoded

path=config-client-demo
```

* Kafdrop
* Client

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

* `CalculationResponse` osztályba:

```java
public RoundResponse round() {
    return new RoundResponse(BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_UP).toString());
}
```

* Application osztályba:

```java
@Bean
public Function<CalculationResponse, RoundResponse> round() {
    return response -> response.round();
}
```

```http
POST http://localhost:8080/calculate,round
Content-Type: application/json

{
  "a": 1.1234,
  "b": 3.2468
}
```

* Elválasztó karakter a `,`

## Type conversion

Jelenleg: `CalculationRequest` -> `CalculationResponse` -> `RoundResponse`
Cél: `CalculationRequest` -> `CalculationResponse` -> `RoundRequest` -> `RoundResponse`

```java
@Data
@AllArgsConstructor
public class RoundRequest {

    private double value;
}
```

`FunctionDemoApplication`

```java
@Bean
public Function<RoundRequest, RoundResponse> round() {
  return request -> new RoundResponse(BigDecimal.valueOf(request.getValue()).setScale(2, RoundingMode.HALF_UP).toString());
}
```

```
Request processing failed: java.lang.ClassCastException: class training.functiondemo.CalculationResponse cannot be cast to class training.functiondemo.RoundRequest
```

Megoldás `ConversionService` használatával:

* `FunctionDemoApplication`, `@Slf4j` annotáció

```java
private static class CalculationResponseToRoundRequestConverter implements Converter<CalculationResponse, RoundRequest>{

  @Override
  public RoundRequest convert(CalculationResponse calculationResponse) {
    log.info("Converting: {}", calculationResponse);
    return new RoundRequest(calculationResponse.getResult());
  }
}
```

```java
@Bean
public ConversionServiceFactoryBean conversionService() {
  var bean = new ConversionServiceFactoryBean();
  bean.setConverters(Set.of(new CalculationResponseToRoundRequestConverter()));
  return bean;
}
```


## Hozzáférés a headerhöz

* Új function

```java
@Bean
public Function<Message<CalculationRequest>, Message<CalculationResponse>> multiply() {
  return requestMessage -> {
    log.info("Header: {}", requestMessage.getHeaders().get("request-id"));
    var request = requestMessage.getPayload();
    return new GenericMessage<>(request.multiply());
  };
}
```

Ehhez kell egy új metódus a `CalculationRequest` osztályba:

```java
public CalculationResponse multiply() {
    return new CalculationResponse(a * b);
}
```

Hívása:

```http
### Multiply
POST http://localhost:8080/multiply
Content-Type: application/json
Request-Id: {{$uuid}}

{
"a": 1.1234,
"b": 2.1234
}
```

## Routing

Routing megadható:

* `MessageRoutingCallback` implementációval
* HTTP headerben
* `application.properties`-ben

A `functionRouter` néven jegyez be egy függvényt

`MessageRoutingCallback` megadásával:

```java
@Bean
public MessageRoutingCallback operationRouter() {
  return new MessageRoutingCallback() {
    @Override
    public String routingResult(Message<?> message) {
      return Optional.ofNullable((String) message.getHeaders().get("operation")).orElse("calculate");
    }
  };
}
```

Hívása: 

```http
### Router
POST http://localhost:8080/functionRouter
Content-Type: application/json
Request-Id: {{$uuid}}
operation: calculate

{
  "a": 1.1234,
  "b": 2.1234
}
```

Headerben:

```http
POST http://localhost:8080/functionRouter
Content-Type: application/json
spring.cloud.function.definition: calculate

{
  "a": 1.1234,
  "b": 2.1234
}
```

Eredmény: java.net.SocketException: Connection reset

`application.properties` fájlban `definition` megadásával:

```properties
spring.cloud.function.definition=calculate
```

`application.properties` fájlban `routing-expression` megadásával:

```properties
spring.cloud.function.routing-expression=headers['operation'] ?: 'calculate'
```

## Actuator

* `pom.xml` új függőség

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

* `application.properties`

```properties
management.endpoints.web.exposure.include=*
```

## Reaktív programozási modell

```java
@Bean
public Function<Flux<CalculationRequest>, Flux<CalculationResponse>> calculate() {
  return calculationRequest -> calculationRequest
      .map(CalculationRequest::calculate)
      .log()
      ;
}

@Bean
public Function<Flux<CalculationResponse>, Flux<RoundResponse>> round() {
  return calculationResponse -> calculationResponse
      .map(CalculationResponse::round);
}
```

## Spring Cloud Stream bevezetése Kafkával

* Függőségek: Stream, Kafka
* Futtatás
* `application.properties`

```properties
spring.cloud.function.definition=calculate|round
```

* Kafka plugin
* Fogadás, küldés

## Topic-ok konfigurálása

* `application.properties`

```
spring.cloud.stream.bindings.calculate|round-in-0.destination=calculation-request
spring.cloud.stream.bindings.calculate|round-out-0.destination=calculation-response
```

## Átállás RabbitMQ-ra

RabbitMQ indítása:

```shell
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

* `org.springframework.cloud:spring-cloud-stream-binder-kafka` megjegyzésbe

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream-binder-rabbit</artifactId>
</dependency>
```

```properties
spring.cloud.stream.bindings.calculate|round-in-0.destination=calculation-request
spring.cloud.stream.bindings.calculate|round-out-0.destination=calculation-response

spring.cloud.stream.bindings.calculate|round-out-0.producer.required-groups=calculator-group
# Routes all
spring.cloud.stream.rabbit.bindings.calculate|round-out-0.producer.binding-routing-key=#
```

* RabbitMQ admin felület: http://localhost:15672
  * Felhasználónév / jelszó: `guest` / `guest`

* Üzenet küldhető a `calculation-request` exchange-be

```json
{"a": 1, "b": 2}
```

* Üzenet olvasható a `calculation-response.calculator-group` sorból

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

# Spring Cloud Stream

## Bevezetés a Spring Cloud Stream használatába

Lsd. slide-ok.

## Kiinduló alkalmazások másolása

```shell
xcopy /e /i javax-spcl2-public\demos-init\employees-backend javax-spcl2\stream-employees-backend
xcopy /e /i javax-spcl2-public\demos-init\employees-frontend javax-spcl2\stream-employees-frontend
```

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

* Binding: binder hozza létre, kapcsolat a broker és a producer/consumer között. Mindig van neve.
  Alapértelmezetten: `<bean neve> + -in- + <index>`, vagy `out`. Hozzárendelhető a 
  broker topic-ja.

```
spring.cloud.stream.function.bindings.createEmployee-in-0=employee-backend-command
spring.cloud.stream.function.bindings.createEmployee-out-0=employee-backend-event
```

* Meg lehet az implicit binding névhez adni explicit nevet is, de talán ez egy felesleges absztrakciós szint.

```properties
spring.cloud.stream.function.bindings.createEmployee-in-0=createEmployeeInput
```

* Ha csak egy `java.util.function.[Supplier/Function/Consumer]` van, akkor azt automatikusan bekonfigurálja,
nem kell a `spring.cloud.function.definition` property. Azonban legjobb gyakorlat használni.

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

## Polling Supplier esetén

A `stream-employees-backend` projekt `GatewayConfig` osztályában

```java
@Bean
public Supplier<String> tick() {
    return () -> {
        log.debug("Tick");
        return "Hello from Supplier " + LocalDateTime.now();
    };

}
```

```yaml
spring:
  cloud:
    function:
          definition: createEmployee;tick
```

* `spring.cloud.function.definition` elválasztókarakter a `;`

* Alapból egy polling, másodpercenként

```yaml
spring:
    stream:
      bindings:
        tick-out-0:
          producer:
            poller:
              initial-delay: 0
              fixed-delay: 5000
          destination: employee-backend-tick
```


* Poller globálisan, és beanenként is felülírható

## Hibakezelés

```java
log.info("Command: {}", command);
if (command.getName().isBlank()) {
    throw new IllegalArgumentException("Name is blank");
}
```

Háromszor írja ki:

```plain
2024-03-13T18:07:51.986+01:00  INFO 21532 --- [container-0-C-1] employees.GatewayConfig                  : Command: {"name": ""}
```

* Először naplóz, utána eldobja az üzenetet

```java
@Bean
public Consumer<ErrorMessage> employeesErrorHandler() {
    return e -> log.error("Error handle message: {}", e.getPayload().getMessage());
}
```

```yaml
spring:
  cloud:
    stream:
      bindings:
        createEmployee-in-0:
          destination: employee-backend-command
          error-handler-definition: employeesErrorHandler
        createEmployee-out-0:
          destination: employee-backend-event
```

## DLQ

```yaml
spring:
  cloud:
    stream:
      bindings:
        createEmployee-in-0:    
          group: employee-backend
      kafka:
        bindings:
          createEmployee-in-0:
            consumer:
              enable-dlq: true
```

Ekkor nem fut le az error handler.

## Spring Cloud Stream Tracing

Zipkin indítása

```shell
docker run -d -p 9411:9411 --name zipkin openzipkin/zipkin
```

Frontend:

* `pom.xml`

```xml
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
  <groupId>io.zipkin.reporter2</groupId>
  <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

* `application.yaml`

```yaml
management:
  tracing:
    sampling:
      probability: 1.0

spring:
  application:
    name: employees-frontend

  cloud:
    stream:
      kafka:
        binder:
          enable-observation: true
```

Backend:

* Ugyanez, `spring.application.name` értéke `employees-backend`

* `process` Spring Cloud Function függvények is, `spring.cloud.function.definition` taggel

Az üzenet header:

```plain
contentType: application/json, spring_json_header_types: {"contentType":"java.lang.String","target-protocol":"java.lang.String"}, target-protocol: kafka, traceparent: 00-66183843384413c8f9213c5d184c8627-78c752fb59fe750d-01
```

## Schema registry

Önálló Springes alkalmazás, REST API-val, H2 adatbázissal, JPA-val

Projekt neve: `stream-employees-schema-registry`

* Spring Web függőség
* `spring-cloud-stream-schema-registry-core` függőség a `pom.xml`-be

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream-schema-registry-core</artifactId>
    <version>4.0.5</version>
</dependency>
```

* `@EnableSchemaRegistryServer` annotáció
* `application.properties`

```properties
server.port=8990
spring.application.name=schema-registry
```

## Avro formátumú üzenet küldése - frontend

* `pom.xml`

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-stream-schema-registry-client</artifactId>
</dependency>
```

* Tranzitívan hivatkozik az Avrora

```xml
<plugin>
  <groupId>org.apache.avro</groupId>
  <artifactId>avro-maven-plugin</artifactId>
  <version>1.11.3</version>
  <configuration>
    <stringType>String</stringType>
  </configuration>
  <executions>
    <execution>
      <phase>generate-sources</phase>
      <goals>
        <goal>schema</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

* Application osztályon `@EnableSchemaRegistryClient` annotáció

* IDEA Avro IDL Support plugin
* `src/main/avro/CreateEmployeeCommand.avsc`

```json
{
    "type": "record",
    "name": "CreateEmployeeCommand",
    "namespace": "employees",
    "fields": [
        {
            "name": "name",
            "type": "string"
        }
    ]
}
```

* `src/main/avro/EmployeeHasBeenCreatedEvent.avsc`

```json
{
    "type": "record",
    "name": "EmployeeHasBeenCreatedEvent",
    "namespace": "employees",
    "fields": [
        {
            "name": "id",
            "type": "long"
        },
        {
            "name": "name",
            "type": "string"
        }
    ]
}
```

* `CreateEmployeeCommand`, `EmployeeHasBeenCreatedEvent` törlése
* `mvn clean package`, Maven frissítés
* `EmployeeBackendGateway`

Átírni a binding nevét:

```java
streamBridge.send("createEmployee", command);
```

* `application.yaml`

```yaml
spring:
  cloud:
    stream:
      bindings:
          createEmployee:
            destination: employee-backend-command
            contentType: application/*+avro
          employeeCreated-in-0:
            destination: employee-backend-event
            contentType: application/*+avro
```

Schema registry:

```http
### Schema registry
GET http://localhost:8990/createemployeecommand/avro
```

## Avro formátumú üzenet fogadása, válasz - backend


```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-stream-schema-registry-client</artifactId>
</dependency>
```

```xml
<plugin>
  <groupId>org.apache.avro</groupId>
  <artifactId>avro-maven-plugin</artifactId>
  <version>1.11.3</version>
  <configuration>
    <stringType>String</stringType>
  </configuration>
  <executions>
    <execution>
      <phase>generate-sources</phase>
      <goals>
        <goal>schema</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

* Application osztályon `@EnableSchemaRegistryClient` annotáció
* `.avsc` fájlok másolása
* `CreateEmployeeCommand`, `EmployeeHasBeenCreatedEvent` törlése
* `mvn clean package`, Maven frissítés
* `application.yaml`

```yaml
spring:
  cloud:
    function:
      definition: createEmployee
    stream:
        bindings:
          createEmployee-in-0:
            destination: employee-backend-command
            contentType: application/*+avro
          createEmployee-out-0:
            destination: employee-backend-event
            contentType: application/*+avro
```

* Avro formátumú üzenetek kezelése az IDEA Kafka pluginban


# Spring Cloud Circuit Breaker

* Absztrakciós réteg, támogatott implementációk:
  * Resilience4J
  * Spring Retry
* Támogatott mechanizmusok:
  * CircuitBreaker
  * Bulkhead
* Resilience4J
  * CircuitBreaker
  * Bulkhead
  * RateLimiter
  * Retry
  * TimeLimiter

## Kiinduló alkalmazások másolása

```shell
xcopy /e /i javax-spcl2-public\demos-init\employees-backend javax-spcl2\circuitbreaker-employees-backend
xcopy /e /i javax-spcl2-public\demos-init\employees-frontend javax-spcl2\circuitbreaker-employees-frontend
```

## Backend alkalmazás előkészítése

* [Chaos Monkey for Spring Boot](https://codecentric.github.io/chaos-monkey-spring-boot/)

```xml
<dependency>
  <groupId>de.codecentric</groupId>
  <artifactId>chaos-monkey-spring-boot</artifactId>
  <version>3.1.0</version>
</dependency>
```

```yaml
spring:
  profiles:
    active: chaos-monkey
management:
  endpoint:
    chaosmonkey:
      enabled: true
```

```http
### Chaos Monkey állapotának lekérdezése
GET http://localhost:8081/actuator/chaosmonkey

### Chaos Monkey bekapcsolása
POST http://localhost:8081/actuator/chaosmonkey/enable
Content-Type: application/json

{
  "enabled": true
}

### Chaos Monkey - RestController watcher bekapcsolása
POST http://localhost:8081/actuator/chaosmonkey/watchers
Content-Type: application/json

{
  "restController": "true"
}


### Chaos Monkey - EmployeesController.listEmployees metódus dobjon kivételt
POST http://localhost:8081/actuator/chaosmonkey/assaults
Content-Type: application/json

{
  "level": 1,
  "latencyActive": false,
  "exceptionsActive": true,
  "exception": {
    "type": "java.lang.RuntimeException",
    "method": "<init>",
    "arguments": [
      {
        "type": "java.lang.String",
        "value": "Chaos Monkey - RuntimeException"
      }
    ]
  },
  "watchedCustomServices": ["employees.EmployeesController.listEmployees"]
}

### Chaos Monkey - EmployeesController.listEmployees metódus latency
POST http://localhost:8081/actuator/chaosmonkey/assaults
Content-Type: application/json

{
  "level": 1,
  "latencyActive": true,
  "latencyRangeStart": 1000,
  "latencyRangeEnd": 3000,
  "exceptionsActive": false,
  "exception": {
    "type": "java.lang.RuntimeException",
    "method": "<init>",
    "arguments": [
      {
        "type": "java.lang.String",
        "value": "Chaos Monkey - RuntimeException"
      }
    ]
  },
  "watchedCustomServices": ["employees.EmployeesController.listEmployees"]
}
```

## Resilience4j bevezetése, circuit breaker

Frontend projekt:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-aop</artifactId>
</dependency>

<dependency>
  <groupId>io.github.resilience4j</groupId>
  <artifactId>resilience4j-spring-boot3</artifactId>
</dependency>
```

```java
@GetExchange
@CircuitBreaker(name = "clientCircuitBreaker")
List<Employee> listEmployees();
```


```yaml
resilience4j:
  circuitbreaker:
    instances:
      clientCircuitBreaker:
        slidingWindowSize: 3
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        slidingWindowType: count_based
        waitDurationInOpenState: 60s
        failureRateThreshold: 50
```

* Actuator

```http
### Circuit breakers
GET http://localhost:8080/actuator/circuitbreakers

### Circuit breaker events
GET http://localhost:8080/actuator/circuitbreakerevents

### Metrics
GET http://localhost:8080/actuator/metrics

### Failure rate
GET http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.failure.rate
```

## Fallback

```java
@GetExchange
@CircuitBreaker(name = "clientCircuitBreaker", fallbackMethod = "fallback")
List<Employee> listEmployees();

default List<Employee> fallback(Throwable throwable) {
  return List.of(new Employee(0L, "Dummy"));
}
```

## TimeLimiter

* `EmployeesClient`

```java
public interface EmployeesClient {

    @GetExchange
    List<Employee> listEmployees();

    @TimeLimiter(name = "clientTimeLimiter")
    default CompletableFuture<List<Employee>> listEmployeesWithTimeLimiter() {
        return CompletableFuture.supplyAsync(this::listEmployees);
    }

    // ...

}
```

* `EmployeesController`

```java
model.put("employees", employeesClient.listEmployeesWithTimeLimiter().get(2500, TimeUnit.MILLISECONDS));
```

```http
###
GET http://localhost:8080/actuator/timelimiters

###
GET http://localhost:8080/actuator/timelimiterevents
```

## Retry

```java
@Retry(name = "clientRetry")
```


```yaml
resilience4j:
  retry:
    instances:
      clientRetry:
        max-attempts: 3
```

* Actuator

```http
###
GET http://localhost:8080/actuator/retries

###
GET http://localhost:8080/actuator/retryevents
```

## Bulkhead

```java
@Bulkhead(name="clientBulkhead")
```


```yaml
resilience4j:
  bulkhead:
    instances:
      clientBulkhead:
        max-concurrent-calls: 5
        max-wait-duration: 10s
```

* JMeter

* Actuator

```http
###
GET http://localhost:8080/actuator/bulkheads

###
GET http://localhost:8080/actuator/bulkheadevents
```

## RateLimiter

```java
@RateLimiter(name="clientRateLimiter")
```


```yaml
resilience4j:
  ratelimiter:
    instances:
      clientRateLimiter:
        limit-refresh-period: 1s
        limit-for-period: 5
        timeout-duration: 10s
        subscribe-for-events: true
```

* JMeter

* Actuator

```http
###
GET http://localhost:8080/actuator/ratelimiters

###
GET http://localhost:8080/actuator/ratelimiterevents
```

# Spring Cloud Gateway

## Kiinduló alkalmazások másolása

```shell
xcopy /e /i javax-spcl2-public\demos-init\employees-backend javax-spcl2\gateway-employees-backend
xcopy /e /i javax-spcl2-public\demos-init\employees-frontend javax-spcl2\gateway-employees-frontend
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

## Backend alkalmazás előkészítése

Lásd Spring Cloud Circuit Breaker / Backend alkalmazás előkészítése

## HTTP timeout

```yaml
resilience4j:
  timelimiter:
    configs:
      default:
        timeoutDuration: 5s
```

## Retry

```yaml
          filters:
            - name: Retry
              args:
                retries: 3
```

## Cache

* `pom.xml`

```xml
<dependency>
  <groupId>com.github.ben-manes.caffeine</groupId>
  <artifactId>caffeine</artifactId>
</dependency>

<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

```yaml
spring:
  cloud:
    gateway:
      filter:
        local-response-cache:
          enabled: true
```

```yaml
          filters:
            - LocalResponseCache=30m,500MB
```

## Trace

Amennyiben nem fut:

```shell
docker run -d -p 9411:9411 --name zipkin openzipkin/zipkin
```

Mindhárom alkalmazásban:

* `pom.xml`

```xml
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
  <groupId>io.zipkin.reporter2</groupId>
  <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
```

```yaml
spring:
  application:
    name: gateway-demo
```

* Rendre `employees-backend`, `gateway-demo`, `employees-frontend`

http://localhost:9411/

# Service discovery és Eureka

## Eureka Service Discovery

Spring Cloud Eureka projekt létrehozása (`employees-eureka`)

* Netflix Eureka Server függőség
* `@EnableEurekaServer` annotáció

`application.yaml`

```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-demo
```

`employees-backend` projekt módosítások

* `spring-cloud-starter-netflix-eureka-client` függőség

```xml
<properties>
    <spring-cloud.version>2022.0.4</spring-cloud.version>
</properties>
```

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

* Több példány indítása

`gateway-demo` projekt módosítások

* `spring-cloud-starter-netflix-eureka-client` függőség

## Spring Cloud Gateway load balancing 

* Backend alkalmazásban átállni ip-címre

```yaml
eureka:
  instance:
    prefer-ip-address: true
```

* Gatewayen 

* Eureka Client

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: employees-backend
          uri: lb://employees-backend
```

## WebClient load balancing

Gateway:

```yaml
eureka:
  instance:
    prefer-ip-address: true
```

Frontend:

* `pom.xml`

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

* `application.yaml`

```yaml
employees:
  backend-url: lb://gateway
```

* `ClientConfig`

```java
@Bean
@LoadBalanced
public WebClient.Builder loadBalancedWebClientBuilder(ObjectProvider<WebClientCustomizer> customizerProvider) {
    var builder = WebClient.builder();
    customizerProvider.orderedStream().forEach((customizer) -> {
        customizer.customize(builder);
    });
    return builder;
}
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