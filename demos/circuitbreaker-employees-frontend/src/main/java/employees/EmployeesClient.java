package employees;


import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@HttpExchange("/api/employees")
public interface EmployeesClient {

    @GetExchange
//    @CircuitBreaker(name = "clientCircuitBreaker", fallbackMethod = "fallback")
//    @Retry(name = "clientRetry")
//    @Bulkhead(name = "clientBulkhead")
    @RateLimiter(name = "clientRateLimiter")
    List<Employee> listEmployees();

//    @TimeLimiter(name = "clientTimeLimiter")
//    default CompletableFuture<List<Employee>> listEmployeesWithTimeLimiter() {
//        return CompletableFuture.supplyAsync(this::listEmployees);
//    }

    default List<Employee> fallback() {
        var employee = new Employee();
        employee.setName("Dummy");
        return List.of(employee);
    }

    @PostExchange
    Employee createEmployee(@RequestBody Employee employee);

    @GetMapping
    Employee findEmployeeById(@PathVariable Long employeeId);
}
