package training.functiondemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@SpringBootApplication
@Slf4j
public class FunctionDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(FunctionDemoApplication.class, args);
	}

	// CalculationRequest -> CalculationResponse -> RoundRequest -> RoundResponse

	@Bean
	public Function<Flux<CalculationRequest>, Flux<CalculationResponse>> calculate() {
		return request -> request
				.map(CalculationRequest::calculate)
				.log();
	}

	@Bean
	public Function<Flux<Message<CalculationRequest>>, Flux<Message<CalculationResponse>>> multiply() {
		return requestMessage -> requestMessage
				.doOnNext(message -> log.info("Header: {}", message.getHeaders().get("request-id")))
				.map(message -> new GenericMessage<>(new CalculationResponse(message.getPayload().getA() * message.getPayload().getB())));
	}

//	@Bean
//	public MessageRoutingCallback operationRouter() {
//		return new MessageRoutingCallback() {
//			@Override
//			public String routingResult(Message<?> message) {
//				return Optional.ofNullable((String) message.getHeaders().get("operation")).orElse("calculate");
//			}
//		};
//	}

	@Bean
	public Function<RoundRequest, RoundResponse> round() {
		return request -> new RoundResponse(BigDecimal.valueOf(request.getValue())
				.setScale(2, RoundingMode.HALF_UP).toString());
	}

	@Bean
	public ConversionServiceFactoryBean conversionService() {
		var bean = new ConversionServiceFactoryBean();
		bean.setConverters(Set.of(new CalculationResponseToRoundRequestConverter()));
		return bean;
	}

	private static class CalculationResponseToRoundRequestConverter implements Converter<CalculationResponse, RoundRequest> {
		@Override
		public RoundRequest convert(CalculationResponse source) {
			log.info("Convert: {}", source);
			return new RoundRequest(source.getResult());
		}
	}
}
