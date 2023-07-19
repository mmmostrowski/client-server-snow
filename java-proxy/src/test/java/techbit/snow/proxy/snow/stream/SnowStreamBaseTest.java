package techbit.snow.proxy.snow.stream;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import techbit.snow.proxy.config.PhpSnowConfig;
import techbit.snow.proxy.dto.ServerMetadata;
import techbit.snow.proxy.dto.SnowBasis;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.lang.EmptyArray;
import techbit.snow.proxy.snow.php.NamedPipe;
import techbit.snow.proxy.snow.php.PhpSnowApp;
import techbit.snow.proxy.snow.transcoding.StreamDecoder;
import techbit.snow.proxy.snow.transcoding.StreamEncoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static techbit.snow.proxy.snow.stream.TestingFrames.frame;

@SuppressWarnings("ALL")
abstract public class SnowStreamBaseTest {

    protected final SnowDataBuffer buffer;
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
    protected ApplicationEventPublisher eventPublisher;
    protected SnowStreamSimpleClient client;
    protected ServerMetadata serverMetadata;
    protected PhpSnowConfig snowConfig;
    protected SnowStream snowStream;

    public SnowStreamBaseTest(SnowDataBuffer buffer) {
        this.buffer = buffer;
    }

    @BeforeEach
    void setup() throws IOException {
        serverMetadata = new ServerMetadata(Duration.ofSeconds(7));
        snowConfig = new PhpSnowConfig(
                "testingPreset", "BASE64BASE64==", 87, 76, Duration.ofMinutes(11), 21);

        client = spy(new SnowStreamSimpleClient(encoder, outputStream));

        final Iterator<SnowDataFrame> inputFrames = List.of(
                frame(1),
                frame(2),
                frame(3),
                frame(4),
                SnowDataFrame.LAST
        ).iterator();

        lenient().when(decoder.decodeFrame(any())).then(i -> inputFrames.next());
        lenient().when(decoder.decodeBasis(any())).thenReturn(SnowBasis.NONE);
        lenient().when(pipe.inputStream()).thenReturn(new ByteArrayInputStream(EmptyArray.NO.BYTES));

        snowStream = new SnowStream("session-xyz", snowConfig,
                serverMetadata,
                pipe, phpSnow, buffer, decoder, eventPublisher);
    }
}
