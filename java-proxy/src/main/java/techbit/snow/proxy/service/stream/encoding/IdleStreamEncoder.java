package techbit.snow.proxy.service.stream.encoding;

import org.springframework.stereotype.Component;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.io.DataOutputStream;

@Component(IdleStreamEncoder.ENCODER_NAME)
public class IdleStreamEncoder implements StreamEncoder {

    public static final String ENCODER_NAME = "IDLE_ENCODER";


    @Override
    public void encodeMetadata(SnowAnimationMetadata metadata, DataOutputStream out) {
    }

    @Override
    public void encodeFrame(SnowDataFrame frame, DataOutputStream out) {
    }

}
