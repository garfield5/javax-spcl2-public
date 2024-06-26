package employees;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@EnableConfigurationProperties(EmployeesProperties.class)
public class ClientConfig {
    @Bean
    public EmployeesClient employeesClient(WebClient.Builder builder, EmployeesProperties employeesProperties) {
        var webClient = builder
                .baseUrl(employeesProperties.getBackendUrl())
                .build();
        var factory = HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(webClient)).build();
        return factory.createClient(EmployeesClient.class);
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder(ObjectProvider<WebClientCustomizer> customizerProvider) {
        var builder = WebClient.builder();
        customizerProvider.orderedStream().forEach(customizer -> customizer.customize(builder));
        return builder;
    }
}
