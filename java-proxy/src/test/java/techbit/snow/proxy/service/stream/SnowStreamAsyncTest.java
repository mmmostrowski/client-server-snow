package techbit.snow.proxy.service.stream;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.service.stream.SnowStream.ConsumerThreadException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnowStreamAsyncTest extends SnowStreamBaseTest {

    public SnowStreamAsyncTest() {
        super(spy(new SnowDataBuffer(13)));
    }

    @Test
    void whenStreamingSnowData_thenMultipleClientsCanReceiveData() throws Throwable {
        CountDownLatch[] latches = new CountDownLatch[]{
                new CountDownLatch(0), // last frame(-1)
                new CountDownLatch(0), // empty frame(0)
                new CountDownLatch(3), // frame(1)
                new CountDownLatch(3), // frame(2)
                new CountDownLatch(3), // frame(3)
                new CountDownLatch(3), // frame(4)
                new CountDownLatch(3)  // frame(5)
        };

        Answer<Void> awaitLatch = i -> {
            SnowDataFrame frame = i.getArgument(0);
            i.callRealMethod();
            latches[frame.frameNum() + 1].await();
            return null;
        };

        Answer<SnowDataFrame> countDownLatch = i -> {
            SnowDataFrame frame = (SnowDataFrame) i.callRealMethod();
            latches[frame.frameNum() + 1].countDown();
            return frame;
        };

        doAnswer(awaitLatch).when(buffer).push(any());
        doAnswer(countDownLatch).when(buffer).firstFrame();
        doAnswer(countDownLatch).when(buffer).nextFrame(any());

        TestFramework.runOnce(new MultithreadedTestCase() {
            void threadConsumer() throws IOException {
                snowStream.startPhpApp();
                when(phpSnow.isAlive()).thenReturn(true);
                snowStream.startConsumingSnowData();
            }

            void thread1() throws ConsumerThreadException, IOException, InterruptedException {
                testThread();
            }

            void thread2() throws ConsumerThreadException, IOException, InterruptedException {
                testThread();
            }

            void thread3() throws ConsumerThreadException, IOException, InterruptedException {
                testThread();
            }

            private void testThread() throws IOException, InterruptedException, ConsumerThreadException {
                ByteArrayOutputStream outputStream = mock(ByteArrayOutputStream.class);

                snowStream.streamTo(outputStream);

                verify(encoder, times(5)).encodeFrame(any(), eq(outputStream));
                verify(encoder, times(1)).encodeMetadata(any(), eq(outputStream));
            }
        });
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
    void givenSluggishConsumerThread_whenStop_thenIsForcedToShutdown() throws Throwable {
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