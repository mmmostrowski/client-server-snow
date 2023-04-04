package techbit.snow.proxy.service.stream.snow;

import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowBackground;
import techbit.snow.proxy.dto.SnowBasis;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.io.IOException;

public interface SnowStreamClient {

    default Object identifier() {
        return Thread.currentThread();
    }

    default boolean continueStreaming() {
        return true;
    }

    void startStreaming(SnowAnimationMetadata metadata, SnowBackground background) throws IOException;

    void streamFrame(SnowDataFrame frame, SnowBasis basis) throws IOException;

    void stopStreaming() throws IOException;

}
