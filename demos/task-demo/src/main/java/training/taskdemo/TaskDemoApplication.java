package training.taskdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;

import java.util.Random;

@SpringBootApplication
@Slf4j
@EnableTask
public class TaskDemoApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(TaskDemoApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.debug("Random number: {}", new Random().nextInt(1, 7));
    }
}
