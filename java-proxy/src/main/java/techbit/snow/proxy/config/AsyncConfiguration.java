package techbit.snow.proxy.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

@Configuration
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {

    private final Duration timeout = Duration.ofMinutes(60);

    @Override
    @Bean(name = "streamExecutor")
    public AsyncTaskExecutor getAsyncExecutor() {
//        executor.setCorePoolSize(5);
//        executor.setMaxPoolSize(5);
//        executor.setQueueCapacity(25);
        return new ThreadPoolTaskExecutor();
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer(@Qualifier("streamExecutor") AsyncTaskExecutor taskExecutor) {
        return new WebMvcConfigurer() {
            @Override
            public void configureAsyncSupport(@NotNull AsyncSupportConfigurer config) {
                config.setDefaultTimeout(timeout.get(SECONDS) * 1000).setTaskExecutor(taskExecutor);
                WebMvcConfigurer.super.configureAsyncSupport(config);
            }
        };
    }
}