package techbit.snow.proxy.service.stream.encoding;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StreamEncoderFactory {

    private final BeanFactory beanFactory;

    public StreamEncoderFactory(@Autowired BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public StreamEncoder createEncoder(String typeOf) {
        return beanFactory.getBean(typeOf, StreamEncoder.class);
    }

}
