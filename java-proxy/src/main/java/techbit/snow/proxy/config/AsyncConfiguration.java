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

@Configuration
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {

    private final Duration timeout = Duration.ofMinutes(60);

    @Override
    @Bean(name = "streamExecutor")
    public AsyncTaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        return executor;
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer(@Qualifier("streamExecutor") AsyncTaskExecutor taskExecutor) {
        return new WebMvcConfigurer() {
            @Override
            public void configureAsyncSupport(@NotNull AsyncSupportConfigurer config) {
                config.setDefaultTimeout(timeout.toMillis()).setTaskExecutor(taskExecutor);
            }
        };
    }
}