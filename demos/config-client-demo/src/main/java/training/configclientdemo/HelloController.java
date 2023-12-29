package training.configclientdemo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableConfigurationProperties(DemoProperties.class)
@AllArgsConstructor
@Slf4j
public class HelloController {

    private DemoProperties demoProperties;

    @GetMapping("/api/hello")
    public Message hello() {
        log.debug("Hello");
        return new Message(demoProperties.getPrefix());
    }
}
