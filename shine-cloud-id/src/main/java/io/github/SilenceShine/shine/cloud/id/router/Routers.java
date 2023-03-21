package io.github.SilenceShine.shine.cloud.id.router;

import io.github.SilenceShine.shine.cloud.id.handler.SnowflakeIdHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * @author SilenceShine
 * @since 1.0
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class Routers {

    @Bean
    public RouterFunction<ServerResponse> uid(SnowflakeIdHandler handler) {
        return nest(path("/snowflakeId"),
                route(GET("/single"), handler::single)
                        .andRoute(GET("batch"), handler::batch)
        );
    }

}
