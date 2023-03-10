package techbit.snow.proxy.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

@Configuration
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {

    @Value("#{ ${phpsnow.threads.timeout} * 1000 }")
    private final Duration timeout = Duration.ofHours(1);

    @Value("${phpsnow.threads.core-pool-size}")
    private final int threadCorePoolSize = 5;

    @Value("${phpsnow.threads.max-pool-size}")
    private final int threadMaxPoolSize = 10;

    @Value("${phpsnow.threads.queue-capacity}")
    private final int threadQueueCapacity = 25;


    @Override
    @Bean(name = "streamExecutor")
    public AsyncTaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadCorePoolSize);
        executor.setMaxPoolSize(threadMaxPoolSize);
        executor.setQueueCapacity(threadQueueCapacity);
        return executor;
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer(
            @Qualifier("streamExecutor") AsyncTaskExecutor taskExecutor
    ) {
        return new WebMvcConfigurer() {
            @Override
            public void configureAsyncSupport(@NotNull AsyncSupportConfigurer config) {
                config.setDefaultTimeout(timeout.toMillis()).setTaskExecutor(taskExecutor);
            }
        };
    }
}