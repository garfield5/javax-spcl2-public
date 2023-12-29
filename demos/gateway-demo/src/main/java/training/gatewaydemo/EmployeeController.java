package training.gatewaydemo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
public class EmployeeController {

    @GetMapping("/api/dummy-employees")
    public Flux<EmployeeResource> listEmployees() {
        return Flux.fromIterable(List.of(new EmployeeResource(0L, "Dummy Employee")));
    }
}
