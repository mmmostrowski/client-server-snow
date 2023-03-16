package techbit.snow.proxy.service.stream;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
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
    protected SnowStream snowStream;
    protected SnowDataBuffer buffer;

    public SnowStreamBaseTest(SnowDataBuffer buffer) {
        this.buffer = buffer;
    }

    @BeforeEach
    void setup() throws IOException {
        PhpSnowConfig config = new PhpSnowConfig("testingPreset", 87, 76, Duration.ofMinutes(11), 21);
        snowStream = new SnowStream("session-xyz", config, pipe, phpSnow, buffer, decoder, encoder);

        lenient().when(pipe.inputStream()).thenReturn(new ByteArrayInputStream(new byte[]{}));

        final Iterator<SnowDataFrame> inputFrames = List.of(
                frame(1),
                frame(2),
                frame(3),
                frame(4),
                SnowDataFrame.last
        ).iterator();

        lenient().when(decoder.decodeFrame(any())).then(i -> inputFrames.next());
    }

    protected SnowDataFrame frame(int frameNum) {
        return new SnowDataFrame(frameNum, 0, null, null, null);
    }
}
