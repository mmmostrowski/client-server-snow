package techbit.snow.proxy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.ConfigurableTomcatWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

@EnableAsync
@Configuration
@SuppressWarnings("NullableProblems")
public class AsyncConfiguration implements AsyncConfigurer {

    private final Duration timeout;
    private final int threadMaxPoolSize;
    private final int threadCorePoolSize;
    private final int threadQueueCapacity;

    public AsyncConfiguration(
            @Value("#{ ${phpsnow.threads.timeout} * 1000 }") Duration timeout,
            @Value("${phpsnow.threads.max-pool-size}") int threadMaxPoolSize,
            @Value("${phpsnow.threads.core-pool-size}") int threadCorePoolSize,
            @Value("${phpsnow.threads.queue-capacity}") int threadQueueCapacity
    ) {
        this.timeout = timeout;
        this.threadMaxPoolSize = threadMaxPoolSize;
        this.threadCorePoolSize = threadCorePoolSize;
        this.threadQueueCapacity = threadQueueCapacity;
    }

    @Override
    @Bean("streamAsyncTaskExecutor")
    public AsyncTaskExecutor getAsyncExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(threadMaxPoolSize);
        executor.setCorePoolSize(threadCorePoolSize);
        executor.setQueueCapacity(threadQueueCapacity);
        return executor;
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer(
            AsyncTaskExecutor streamAsyncTaskExecutor
    ) {
        return new WebMvcConfigurer() {
            @Override
            public void configureAsyncSupport(AsyncSupportConfigurer config) {
                config.setDefaultTimeout(timeout.toMillis());
                config.setTaskExecutor(streamAsyncTaskExecutor);
            }
        };
    }

    @Bean
    public WebServerFactoryCustomizer<ConfigurableTomcatWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                connector.setProperty("packetSize", "1");
            });
        };
    }

}