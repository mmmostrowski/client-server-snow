package techbit.snow.proxy.service.stream.encoding;

import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.io.IOException;
import java.io.OutputStream;

public interface StreamEncoder {


    void encodeMetadata(SnowAnimationMetadata metadata, OutputStream out) throws IOException;

    void encodeFrame(SnowDataFrame frame, OutputStream out) throws IOException;

}
