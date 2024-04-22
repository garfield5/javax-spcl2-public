package employees;

import lombok.AllArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EmployeeBackendGateway {

    private StreamBridge streamBridge;

    public void createEmployee(Employee employee) {
        var command = new CreateEmployeeCommand(employee.getName());
        streamBridge.send("createEmployee", command);
    }
}
