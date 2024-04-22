class: inverse, center, middle

# Bevezetés a Spring Cloud használatába

## &nbsp;

---

## Spring Cloud célja

Gyakori minták megvalósítása elosztott környezetben:

* Konfigurációkezelés
* Service discovery
* Routing
* Load balancing
* Circuit breakers
* Rövidéletű microservice-ek (taskok)
* Contract testing

---

## Kubernetes és felhőszolgáltatók támogatása

* AWS: Spring Cloud for Amazon Web Services - community project
* Azure
    * Dokumentáció - https://learn.microsoft.com/en-us/azure/developer/java/spring-framework/developer-guide-overview
    * Forráskód - https://github.com/microsoft/spring-cloud-azure
* GCP: Spring Cloud GCP is no longer part of the Spring Cloud release train.
    * https://github.com/GoogleCloudPlatform/spring-cloud-gcp

---

## Története

* 2015-ben jött ki
* Netflix eszközök köré egy wrapper
  * Eureka - service discovery
  * Hystrix - circuit breaker  
  * Zuul - API gateway
  * Ribbon - client-side load balancer

---

## Újítások

* Azóta bővítve, és a régi eszközök helyett érdemes újabbakat használni
  * Hystrix -> Resilience4j
  * Zuul -> Spring Cloud Gateway
  * Ribbon -> Spring Cloud LoadBalancer
  * Egyedüli megmaradó komponens az Eureka

---

## Spring Boot kapcsolat

* Spring Initializr támogatás
* Erősen függ a Spring Boot verziótól
  * Spring Boot 3.0.x, 3.1.x -> 2022.0.x aka Kilburn (2022.0.3-tól)
  * Spring Boot 3.2.x -> 2023.0.x aka Leyton
  * 2020.0 (aka Ilford) és az előttiek már nem támogatottak

---

class: inverse, center, middle

# Bevezetés a Spring Cloud Stream használatába

## &nbsp;

---

## Spring Cloud Stream célja

* Spring Integration projekt az EAI egy implementációja
* A Spring Boot és Spring Integration integrálásából született a Spring Cloud Stream projekt
* Különösen alkalmas microservice architektúrában event driven megközelítés implementálására
* Message broker implementáció cserélhető

---

## Tulajdonságai

* Erős content-type támogatás, konverziók
* Alkalmazásban input és output van
* Binder köti ezt össze a brokerrel. Ilyen binder van pl. Kafkához, RabbitMQ-hoz,
  és a teszteléshez test binder.
* Egyszerre akár több bindert is használhat az alkalmazás, konfigurációban választható, hogy hol melyiket
  használja
* Az input/output és a broker topic-jai közötti kapcsolat a binding
* Támogatja a publish-subscribe modellt
* Támogatja a terheléselosztást, Consumer Group használatával (Kafka alapján). Ugyanabba a Consumer
  Groupba tartozó alkalmazások közül csak egy kapja meg az üzenetet.
* Partícionálás: több példány esetén az összetartozó üzeneteket ugyanaz a példány kapja meg.
  Hasznos az állapotfüggő feldolgozáskor. Küldő és fogadó oldalon is be kell állítani.

---

## Felépítése

<p align="center">
<img src="images/spring-cloud-stream.drawio.svg" alt="Felépítése" width="500" />
</p>

