package techbit.snow.proxy.service.stream;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.service.phpsnow.PhpSnowApp;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfig;
import techbit.snow.proxy.service.stream.encoding.StreamDecoder;
import techbit.snow.proxy.service.stream.encoding.StreamEncoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

abstract public class SnowStreamBaseTest {

    @Mock
    protected NamedPipe pipe;
    @Mock
    protected PhpSnowApp phpSnow;
    @Mock
    protected StreamDecoder decoder;
    @Mock
    protected StreamEncoder encoder;
    @Mock
    protected OutputStream outputStream;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    protected final SnowDataBuffer buffer;
    protected SnowStream snowStream;

    public SnowStreamBaseTest(SnowDataBuffer buffer) {
        this.buffer = buffer;
    }

    @BeforeEach
    void setup() throws IOException {
        final PhpSnowConfig config = new PhpSnowConfig(
                "testingPreset", 87, 76, Duration.ofMinutes(11), 21);

        final Iterator<SnowDataFrame> inputFrames = List.of(
                frame(1),
                frame(2),
                frame(3),
                frame(4),
                SnowDataFrame.LAST
        ).iterator();

        lenient().when(decoder.decodeFrame(any())).then(i -> inputFrames.next());

        lenient().when(pipe.inputStream()).thenReturn(new ByteArrayInputStream(new byte[]{}));

        snowStream = new SnowStream("session-xyz", config, pipe, phpSnow, buffer, decoder, eventPublisher);
    }

    protected SnowDataFrame frame(int frameNum) {
        return new SnowDataFrame(frameNum, 0, null, null, null);
    }
}
