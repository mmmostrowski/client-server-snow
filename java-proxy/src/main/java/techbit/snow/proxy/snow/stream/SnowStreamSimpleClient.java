package techbit.snow.proxy.snow.stream;

import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowBackground;
import techbit.snow.proxy.dto.SnowBasis;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.snow.transcoding.StreamEncoder;

import java.io.IOException;
import java.io.OutputStream;

public final class SnowStreamSimpleClient implements SnowStreamClient {

    private final StreamEncoder encoder;
    private final OutputStream output;

    public SnowStreamSimpleClient(StreamEncoder encoder, OutputStream output) {
        this.encoder = encoder;
        this.output = output;
    }

    @Override
    public void startStreaming(SnowAnimationMetadata metadata, SnowBackground background) throws IOException {
        encoder.encodeMetadata(metadata, output);
        encoder.encodeBackground(background, output);
    }

    @Override
    public void streamFrame(SnowDataFrame frame, SnowBasis basis) throws IOException {
        encoder.encodeFrame(frame, output);
        encoder.encodeBasis(basis, output);
    }

    @Override
    public void stopStreaming() throws IOException {
        encoder.encodeFrame(SnowDataFrame.LAST, output);
    }

}
