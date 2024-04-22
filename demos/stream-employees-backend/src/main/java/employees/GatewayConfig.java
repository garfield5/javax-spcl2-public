package employees;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.ErrorMessage;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Configuration(proxyBeanMethods = false)
@Slf4j
public class GatewayConfig {

    @Autowired
    private EmployeesService employeesService;

    @Bean
    public Function<CreateEmployeeCommand, EmployeeHasBeenCreatedEvent> createEmployee() {
        return command -> {
            log.info("Command: {}", command);
            if (command.getName().isBlank()) {
                throw new IllegalArgumentException("Name is blank");
            }
            var created = employeesService.createEmployee(new EmployeeResource(command.getName()));
            var event = new EmployeeHasBeenCreatedEvent(created.getId(), created.getName());
            log.debug("Event: {}", event);
            return event;
        };
    }

    @Bean
    public Consumer<ErrorMessage> employeesErrorHandler() {
        return e ->
                log.error("Error handling message: {}", e.getPayload().getMessage());
    }

    @Bean
    public Supplier<String> tick() {
        return () -> {
            log.info("Tick");
            return "Hello from Supplier " + LocalDateTime.now();
        };
    }
}
