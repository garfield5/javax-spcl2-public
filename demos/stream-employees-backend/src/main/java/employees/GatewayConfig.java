package employees;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

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
