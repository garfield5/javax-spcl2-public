# Fontosabb Spring Cloud projektek a gyakorlatban

Ez a repository tartalmazza a tanfolyam anyagát.

A [demos.md](demos.md) fájl tartalmazza a videón szereplő feladatok leírását, bemásolható
parancsokat és forráskódrészleteket.

A `demos` könyvtár tartalmazza a videón szereplő feladatok megoldását.

* Spring Cloud Config
    * Spring Cloud Config Server elindítása (`config-server-demo`)
    * Spring Cloud Config Client elindítása (`config-client-demo`)
* Spring Cloud Bus
    * Kafka indítása (`kafka`)
    * Konfiguráció újratöltése futásidőben (`config-server-demo`, `config-client-demo`)
    * Spring Cloud Bus event kezelése (`config-server-demo`, `config-client-demo`)
* Spring Cloud Function
    * Spring Cloud Function használatba vétele (`function-demo`)
    * Spring Cloud Function tesztelése (`function-demo`)
    * Function Composition (`function-demo`)
* Spring Cloud Stream bevezetés
    * Spring Cloud Stream bevezetése Kafkával (`function-demo-stream`)
    * Topic-ok konfigurálása (`function-demo-stream`)
* Frontend és backend alkalmazás bemutatása
    * Alkalmazás bemutatása - Backend (`employees-backend`)
    * Alkalmazás bemutatása - Frontend (`employees-frontend`)
* Spring Cloud Stream
    * Kafka üzenet küldése üzleti logikából (`stream-employees-frontend`)
    * Kafka üzenetfogadás és válasz (`stream-employees-backend`)
    * Kafka üzenet fogadása (`stream-employees-frontend`)
* Spring Cloud Gateway
    * Spring Cloud Gateway használatba vétele (`gateway-employees-backend`, `gateway-employees-frontend`, `gateway-demo`)
    * Header módosítása (`gateway-employees-backend`, `gateway-demo`)
    * Circuit breaker beállítása (`gateway-employees-backend`, `gateway-demo`)
    * Fallback beállítása (`gateway-employees-backend`, `gateway-demo`)
* Spring Cloud Task
    * Spring Cloud Task használatba vétele (`task-demo`)
    * Launcher használata (`task-launcher`)
