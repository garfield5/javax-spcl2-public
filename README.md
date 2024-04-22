# Fontosabb Spring Cloud projektek a gyakorlatban

Ez a repository tartalmazza a tanfolyam anyagát.

A [demos.md](demos.md) fájl tartalmazza a videón szereplő feladatok leírását, bemásolható
parancsokat és forráskódrészleteket.

A `demos` könyvtár tartalmazza a videón szereplő feladatok megoldását.

* Spring Cloud bevezetés
    * Bevezetés a Spring Cloud használatába
* Spring Cloud Config
    * Spring Cloud Config Server elindítása (`config-server-demo`)
    * Spring Cloud Config Client elindítása (`config-client-demo`)
* Spring Cloud Bus
    * Kafka indítása (`kafka`)
    * Konfiguráció újratöltése futásidőben (`config-server-demo`, `config-client-demo`)
    * Spring Cloud Bus event kezelése (`config-server-demo`, `config-client-demo`)
* Spring Cloud Config és a biztonságos tárolás
    * Kódolt értékek (`config-server-demo-encoded`)
    * HashiCorp Vault Backend (`config-server-demo-vault`)
    * Frissítés HashiCorp Vault Backenddel (`config-server-demo-vault`)
* Spring Cloud Function
    * Spring Cloud Function használatba vétele (`function-demo`)
    * Spring Cloud Function tesztelése (`function-demo`)
    * Function Composition (`function-demo`)
    * Type conversion (`function-demo-advanced`)
    * Hozzáférés a headerhöz (`function-demo-advanced`)
    * Routing (`function-demo-advanced`)
    * Routing application.properties alapján (`function-demo-advanced`)
    * Spring Cloud Function Actuator (`function-demo-advanced`)
    * Reaktív programozási modell (`function-demo-reactive`)
* Spring Cloud Stream bevezetés
    * Spring Cloud Stream bevezetése Kafkával (`function-demo-stream`)
    * Topic-ok konfigurálása (`function-demo-stream`)
    * Átállás RabbitMQ-ra (`function-demo-stream-rabbitmq`)
* Frontend és backend alkalmazás bemutatása
    * Alkalmazás bemutatása - Backend (`employees-backend`)
    * Alkalmazás bemutatása - Frontend (`employees-frontend`)
* Spring Cloud Stream
    * Bevezetés a Spring Cloud Stream használatába
    * Kafka üzenet küldése üzleti logikából (`stream-employees-frontend`)
    * Kafka üzenetfogadás és válasz (`stream-employees-backend`)
    * Kafka üzenet fogadása (`stream-employees-frontend`)
    * Polling Supplier esetén (`stream-employees-backend`)
    * Hibakezelés (`stream-employees-backend`)
    * DLQ (`stream-employees-backend`)
    * Spring Cloud Stream Tracing (`stream-employees-backend`, `stream-employees-frontend`)
    * Schema registry (`stream-employees-schema-registry`)
    * Avro formátumú üzenet küldése - frontend (`stream-employees-frontend`)
    * Avro formátumú üzenet fogadása, válasz - backend (`stream-employees-backend`)
* Spring Cloud Circuit Breaker
    * Backend alkalmazás előkészítése (`circuitbreaker-employees-backend`)
    * Resilience4j bevezetése, circuit breaker (`circuitbreaker-employees-frontend`)
    * Fallback (`circuitbreaker-employees-frontend`)
    * TimeLimiter (`circuitbreaker-employees-frontend`)
    * Retry (`circuitbreaker-employees-frontend`)
    * Bulkhead (`circuitbreaker-employees-frontend`)
    * RateLimiter (`circuitbreaker-employees-frontend`)
* Spring Cloud Gateway
    * Spring Cloud Gateway használatba vétele (`gateway-employees-backend`, `gateway-employees-frontend`, `gateway-demo`)
    * Header módosítása (`gateway-employees-backend`, `gateway-demo`)
    * Circuit breaker beállítása (`gateway-employees-backend`, `gateway-demo`)
    * Fallback beállítása (`gateway-employees-backend`, `gateway-demo`)
    * Backend alkalmazás előkészítése (`gateway-employees-backend`)
    * HTTP timeout (`gateway-demo`)
    * Retry (`gateway-demo`)
    * Cache (`gateway-demo`)
    * Trace (`gateway-demo`)
* Service discovery és Eureka
    * Eureka Service Discovery (`eureka-demo`)
    * Spring Cloud Gateway load balancing (`gateway-demo`)
    * WebClient load balancing (`gateway-employees-frontend`)
* Spring Cloud Task
    * Spring Cloud Task használatba vétele (`task-demo`)
    * Launcher használata (`task-launcher`)
