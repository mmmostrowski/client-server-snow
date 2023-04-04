package techbit.snow.proxy.service.stream.snow;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.context.ApplicationEventPublisher;
import techbit.snow.proxy.dto.SnowBasis;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.service.phpsnow.PhpSnowApp;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfig;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfigConverter;
import techbit.snow.proxy.service.stream.NamedPipe;
import techbit.snow.proxy.service.stream.TestingFrames;
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
import static org.mockito.Mockito.spy;
import static techbit.snow.proxy.service.stream.TestingFrames.*;

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
    protected PhpSnowConfigConverter converter;
    protected SnowStreamSimpleClient client;
    @Mock
    protected ApplicationEventPublisher eventPublisher;
    protected final SnowDataBuffer buffer;
    protected PhpSnowConfig snowConfig;
    protected SnowStream snowStream;

    public SnowStreamBaseTest(SnowDataBuffer buffer) {
        this.buffer = buffer;
    }

    @BeforeEach
    void setup() throws IOException {
        snowConfig = new PhpSnowConfig(
                "testingPreset", 87, 76, Duration.ofMinutes(11), 21);

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

        lenient().when(pipe.inputStream()).thenReturn(new ByteArrayInputStream(new byte[]{}));

//        lenient().when(client.continueStreaming()).thenReturn(true);

        snowStream = new SnowStream("session-xyz", snowConfig, pipe, phpSnow, buffer, decoder, eventPublisher);
    }
}
