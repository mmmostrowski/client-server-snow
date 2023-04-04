package techbit.snow.proxy.snow.stream;

import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowBackground;
import techbit.snow.proxy.dto.SnowBasis;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.snow.transcoding.StreamEncoder;

import java.io.IOException;
import java.io.OutputStream;

public class SnowStreamSimpleClient implements SnowStreamClient {

    private final StreamEncoder encoder;
    private final OutputStream out;

    public SnowStreamSimpleClient(StreamEncoder encoder, OutputStream out) {
        this.encoder = encoder;
        this.out = out;
    }

    @Override
    public void startStreaming(SnowAnimationMetadata metadata, SnowBackground background) throws IOException {
        encoder.encodeMetadata(metadata, out);
        encoder.encodeBackground(background, out);
    }

    @Override
    public void streamFrame(SnowDataFrame frame, SnowBasis basis) throws IOException {
        encoder.encodeFrame(frame, out);
        encoder.encodeBasis(basis, out);
    }

    @Override
    public void stopStreaming() throws IOException {
        encoder.encodeFrame(SnowDataFrame.LAST, out);
    }

}
