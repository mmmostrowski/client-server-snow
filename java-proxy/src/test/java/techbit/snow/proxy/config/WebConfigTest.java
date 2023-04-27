package techbit.snow.proxy.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebConfigTest {

    @Mock
    private CorsRegistry registry;
    @Mock
    private CorsRegistration registration;

    @Test
    void whenAskingForCorsConfig_thenUseOurAllowedOrigins() {
        when(registry.addMapping("/**")).thenReturn(registration);
        WebConfig config = new WebConfig("http://origin1  http://origin:23/other");

        config.corsConfig().addCorsMappings(registry);

        verify(registration).allowedOrigins("http://origin1", "http://origin:23/other");
    }

}