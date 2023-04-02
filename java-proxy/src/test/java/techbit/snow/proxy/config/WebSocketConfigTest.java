package techbit.snow.proxy.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketConfigTest {

    @Mock
    private StompWebSocketEndpointRegistration endpointRegistration;
    @Mock
    private MessageBrokerRegistry messageBrokerRegistry;
    @Mock
    private StompEndpointRegistry registry;
    private WebSocketConfig config;

    @BeforeEach
    void setup() {
        config = new WebSocketConfig();
    }


    @Test
    void whenRegisteringStompEndpoints_thenItIsProperlyConfigured() {
        when(registry.addEndpoint(any())).thenReturn(endpointRegistration);

        config.registerStompEndpoints(registry);

        verify(registry).addEndpoint(any());
        verify(endpointRegistration).setAllowedOrigins(any());
    }

    @Test
    void whenConfiguringMessageBroker_thenSetupRequiredParameters() {
        config.configureMessageBroker(messageBrokerRegistry);

        verify(messageBrokerRegistry).setApplicationDestinationPrefixes(any());
        verify(messageBrokerRegistry).enableSimpleBroker(any());
        verify(messageBrokerRegistry).setUserDestinationPrefix(any());
    }

}