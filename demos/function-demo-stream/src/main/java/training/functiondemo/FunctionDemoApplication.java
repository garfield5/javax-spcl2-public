package training.functiondemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Function;

@SpringBootApplication
public class FunctionDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(FunctionDemoApplication.class, args);
	}

	@Bean
	public Function<CalculationRequest, CalculationResponse> calculate() {
		return CalculationRequest::calculate;
	}

	@Bean
	public Function<CalculationResponse, RoundResponse> round() {
		return CalculationResponse::round;
	}
}
