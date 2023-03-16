package techbit.snow.proxy.service.stream;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.service.phpsnow.PhpSnowApp;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfig;
import techbit.snow.proxy.service.stream.encoding.StreamDecoder;
import techbit.snow.proxy.service.stream.encoding.StreamEncoder;

import java.io.*;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnowStreamAsyncTest {


    @Mock
    private NamedPipe pipe;
    @Mock
    private PhpSnowApp phpSnow;
    @Spy
    private SnowDataBuffer buffer = new SnowDataBuffer(3);
    @Mock
    private StreamDecoder decoder;
    @Mock
    private StreamEncoder encoder;
    private SnowStream snowStream;
    private OutputStream outputStream;
    private List<SnowDataFrame> framesSequence;

    @BeforeEach
    void setup() throws IOException {
        PhpSnowConfig config = new PhpSnowConfig("testingPreset", 87, 76, Duration.ofMinutes(11), 21);
        snowStream = new SnowStream("session-xyz", config, pipe, phpSnow, buffer, decoder, encoder);
        InputStream inputStream = new ByteArrayInputStream(new byte[]{
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        });
        outputStream = new ByteArrayOutputStream();
        framesSequence = List.of(
                frame(1),
                frame(2),
                frame(3),
                frame(4)
        );
        lenient().when(pipe.inputStream()).thenReturn(inputStream);

        Iterator<SnowDataFrame> inputFrames = concat(framesSequence.stream(), of(SnowDataFrame.last))
                .toList().iterator();
        lenient().when(decoder.decodeFrame(any())).then(i -> inputFrames.next());
    }

    private SnowDataFrame frame(int frameNum) {
        return new SnowDataFrame(frameNum, 0, null, null, null);
    }

    @Test
    void givenHealthyConsumerThread_whenStop_thenShutdownGracefully() throws Throwable {
        AtomicInteger frameNum = new AtomicInteger(0);
        when(decoder.decodeFrame(any())).then(i -> frame(frameNum.incrementAndGet()));

        TestFramework.runOnce(new MultithreadedTestCase() {
            void thread1() throws IOException, InterruptedException {
                snowStream.startPhpApp();
                when(phpSnow.isAlive()).thenReturn(true);
                snowStream.startConsumingSnowData();
            }

            void thread2() throws IOException, InterruptedException {
                waitForTick(1);
                Assertions.assertTrue(snowStream.isActive());
                snowStream.stop();
                snowStream.waitUntilConsumerThreadFinished();
                Assertions.assertFalse(snowStream.isActive());
            }
        });
    }

    @Test
    void givenSloppyConsumerThread_whenStop_thenIsForcedToShutdown() throws Throwable {
        verify(decoder, atMostOnce()).decodeFrame(any());
        when(decoder.decodeFrame(any())).then(f -> {
            Object o = new Object();
            synchronized (o) {
                o.wait(); // forever
            }
            return frame(1);
        });

        TestFramework.runOnce(new MultithreadedTestCase() {
            void thread1() throws IOException {
                snowStream.startPhpApp();
                when(phpSnow.isAlive()).thenReturn(true);
                snowStream.startConsumingSnowData();
            }

            void thread2() throws IOException, InterruptedException {
                waitForTick(1);
                Assertions.assertTrue(snowStream.isActive());
                snowStream.stop();
                snowStream.waitUntilConsumerThreadFinished();
                Assertions.assertFalse(snowStream.isActive());
            }
        });
    }

}