package techbit.snow.proxy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncConfigurationTest {

    private AsyncConfiguration conf;

    @BeforeEach
    void setup() {
        conf = new AsyncConfiguration(
                Duration.ofHours(1),
                19,
                119,
                99
        );
    }

    @Test
    void whenProvidingAsyncExecutor_thenItIsProperlyConfigured() {
        try(MockedConstruction<?> mocked = mockConstruction(ThreadPoolTaskExecutor.class)) {
            conf.getAsyncExecutor();

            ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) mocked.constructed().get(0);

            verify(executor).setCorePoolSize(19);
            verify(executor).setMaxPoolSize(119);
            verify(executor).setQueueCapacity(99);
        }
    }

    @Test
    void whenProvidingMvcConfig_thenItIsProperlyConfigured() {
        AsyncTaskExecutor taskExecutor = mock(AsyncTaskExecutor.class);
        AsyncSupportConfigurer asyncSupportConfig = mock(AsyncSupportConfigurer.class);

        WebMvcConfigurer mvcConfig = conf.webMvcConfigurer(taskExecutor);

        mvcConfig.configureAsyncSupport(asyncSupportConfig);

        verify(asyncSupportConfig).setDefaultTimeout(Duration.ofHours(1).toMillis());
        verify(asyncSupportConfig).setTaskExecutor(taskExecutor);
    }

}