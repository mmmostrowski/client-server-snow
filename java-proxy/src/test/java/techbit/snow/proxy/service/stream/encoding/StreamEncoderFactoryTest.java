package techbit.snow.proxy.service.stream.encoding;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.BeanFactory;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StreamEncoderFactoryTest {

    @Mock
    private BeanFactory beanFactory;

    @Test
    void whenCreatingEncoder_thenUseBeanFactory() {
        StreamEncoderFactory factory = new StreamEncoderFactory(beanFactory);
        factory.createEncoder("encoder-type");

        verify(beanFactory).getBean("encoder-type", StreamEncoder.class);
    }

}