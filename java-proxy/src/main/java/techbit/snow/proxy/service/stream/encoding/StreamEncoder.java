package techbit.snow.proxy.service.stream.encoding;

import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.io.DataOutputStream;
import java.io.IOException;

public interface StreamEncoder {

    void encodeMetadata(SnowAnimationMetadata metadata, DataOutputStream out) throws IOException;

    void encodeFrame(SnowDataFrame frame, DataOutputStream out) throws IOException;

}
