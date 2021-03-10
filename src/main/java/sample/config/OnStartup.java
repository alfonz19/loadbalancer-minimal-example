package sample.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@AllArgsConstructor
public class OnStartup {

    private final ReactorLoadBalancerExchangeFilterFunction lbFunction;

    @EventListener(ApplicationReadyEvent.class)
    public void doOnStartup() {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://complicated/test-consumer")
                .filter(lbFunction)
                .build();
        Flux.interval(Duration.ofSeconds(1))
                .map(e->Long.toString(e))
                .concatMap(e -> webClient.post()
                .body(Mono.just(e), String.class)
                .retrieve()
                .bodyToMono(String.class))
                .doOnNext(response -> log.trace("Got response {}", response))
                .blockLast(Duration.ofMinutes(1));

    }
}
