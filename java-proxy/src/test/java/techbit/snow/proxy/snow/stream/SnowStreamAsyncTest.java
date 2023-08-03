package techbit.snow.proxy.snow.stream;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.snow.stream.SnowStream.ConsumerThreadException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;
import static techbit.snow.proxy.snow.stream.TestingFrames.frame;

@SuppressWarnings("unused")
@ExtendWith(MockitoExtension.class)
class SnowStreamAsyncTest extends SnowStreamBaseTest {

    public SnowStreamAsyncTest() {
        super(spy(new SnowDataBuffer(13, new BlockingBag<>())));
    }

    @Test
    void whenStreamingSnowData_thenMultipleClientsCanReceiveData() throws Throwable {
        final CountDownLatch[] latches = new CountDownLatch[]{
                new CountDownLatch(0), // last frame(-1)
                new CountDownLatch(0), // empty frame(0)
                new CountDownLatch(3), // frame(1)
                new CountDownLatch(3), // frame(2)
                new CountDownLatch(3), // frame(3)
                new CountDownLatch(3), // frame(4)
                new CountDownLatch(3)  // frame(5)
        };

        final Answer<SnowDataFrame> countDownLatch = i -> {
            // buffer.firstFrame|nextFrame(frame)
            SnowDataFrame frame = (SnowDataFrame) i.callRealMethod();
            latches[frame.frameNum() + 1].countDown();
            latches[frame.frameNum() + 1].await();
            return frame;
        };

        final Answer<Void> awaitLatch = i -> {
            // buffer.push(frame)
            SnowDataFrame frame = i.getArgument(0);
            i.callRealMethod();
            latches[frame.frameNum() + 1].await();
            return null;
        };

        // each thread signal after frame consumed
        doAnswer(countDownLatch).when(buffer).firstFrame();
        doAnswer(countDownLatch).when(buffer).nextFrame(any());
        // wait until previous frame consumed by all threads
        doAnswer(awaitLatch).when(buffer).push(any());

        when(phpSnow.isAlive()).thenReturn(true);

        TestFramework.runOnce(new MultithreadedTestCase() {
            final Semaphore readyToStream = new Semaphore(0);

            void threadConsumer() throws IOException {
                snowStream.startConsumingSnowData();
                readyToStream.release(3);
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
                readyToStream.acquire();

                final ByteArrayOutputStream outputStream = mock(ByteArrayOutputStream.class);
                final SnowStreamSimpleClient client = new SnowStreamSimpleClient(encoder, outputStream);

                snowStream.streamTo(client);

                verify(encoder, times(6)).encodeFrame(any(), eq(outputStream));
                verify(encoder, times(1)).encodeMetadata(any(), eq(outputStream));
            }
        });
    }

    @Test
    void givenHealthyConsumerThread_whenStop_thenShutdownGracefully() throws Throwable {
        // produce infinite stream of frames
        AtomicInteger frameNum = new AtomicInteger(0);
        when(decoder.decodeFrame(any())).then(i -> frame(frameNum.incrementAndGet()));

        TestFramework.runOnce(new MultithreadedTestCase() {
            final Semaphore readyToStream = new Semaphore(0);

            void thread1() throws IOException {
                when(phpSnow.isAlive()).thenReturn(true);
                snowStream.startConsumingSnowData();
                readyToStream.release(2);
            }

            void thread2() throws IOException, InterruptedException, ConsumerThreadException {
                readyToStream.acquire();
                Assertions.assertTrue(snowStream.isActive());
                snowStream.streamTo(client);
            }

            void thread3() throws IOException, InterruptedException {
                readyToStream.acquire();
                Assertions.assertTrue(snowStream.isActive());
                Thread.sleep(100);
                snowStream.stop();
                snowStream.waitUntilConsumerThreadFinished();
                Assertions.assertFalse(snowStream.isActive());
                verify(decoder, atLeastOnce()).decodeFrame(any());
                verify(encoder, atLeastOnce()).encodeFrame(SnowDataFrame.LAST, outputStream);
            }
        }, 10, 100);
    }

    @Test
    void givenSluggishConsumerThread_whenStop_thenIsForcedToShutdown() throws Throwable {
        when(decoder.decodeFrame(any())).then(f -> {
            synchronized (this) {
                wait(); // forever
            }
            throw new IllegalStateException("Unreachable");
        });

        TestFramework.runOnce(new MultithreadedTestCase() {
            final Semaphore readyToStream = new Semaphore(0);

            void thread1() throws IOException {
                when(phpSnow.isAlive()).thenReturn(true);
                snowStream.startConsumingSnowData();
                readyToStream.release(2);
            }

            void thread2() throws InterruptedException {
                readyToStream.acquire();
                Assertions.assertTrue(snowStream.isActive());
            }

            void thread3() throws IOException, InterruptedException {
                readyToStream.acquire();
                Assertions.assertTrue(snowStream.isActive());
                Thread.sleep(100);
                snowStream.stop();
                snowStream.waitUntilConsumerThreadFinished(); // not forever
                Assertions.assertFalse(snowStream.isActive());
                verify(decoder, atMostOnce()).decodeFrame(any());
            }
        });
    }

    @Test
    void givenSluggishClients_whenAnimationFinished_thenEachClientCanReadBufferTillEnd() throws Throwable {
        CountDownLatch firstLoopLatch = new CountDownLatch(3);
        CountDownLatch remainingLoopsLatch = new CountDownLatch(1);

        Answer<Void> awaitLatch = i -> {
            i.callRealMethod(); // buffer.push(frame)

            SnowDataFrame frame = i.getArgument(0);
            if (frame.frameNum() == 1) {
                firstLoopLatch.await();
            } else if (frame == SnowDataFrame.LAST) {
                remainingLoopsLatch.countDown();
            }

            return null;
        };

        Answer<SnowDataFrame> countDownLatch = i -> {
            Thread.sleep(100);
            // buffer.firstFrame|nextFrame(frame)
            SnowDataFrame frame = (SnowDataFrame) i.callRealMethod();
            firstLoopLatch.countDown();
            remainingLoopsLatch.await();
            return frame;
        };

        // signal frame consumed
        doAnswer(countDownLatch).when(buffer).firstFrame();
        doAnswer(countDownLatch).when(buffer).nextFrame(any());
        // wait until all frames consumed
        doAnswer(awaitLatch).when(buffer).push(any());

        when(phpSnow.isAlive()).thenReturn(true);

        TestFramework.runOnce(new MultithreadedTestCase() {
            final Semaphore readyToStream = new Semaphore(0);

            void threadConsumer() throws IOException, InterruptedException {
                snowStream.startConsumingSnowData();
                readyToStream.release(3);
                snowStream.waitUntilConsumerThreadFinished();
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
                readyToStream.acquire();

                final ByteArrayOutputStream outputStream = mock(ByteArrayOutputStream.class);
                final SnowStreamSimpleClient client = new SnowStreamSimpleClient(encoder, outputStream);

                snowStream.streamTo(client);

                InOrder inOrder = inOrder(encoder);

                inOrder.verify(encoder).encodeMetadata(any(), eq(outputStream));
                inOrder.verify(encoder).encodeFrame(frame(1), outputStream);
                inOrder.verify(encoder).encodeFrame(frame(2), outputStream);
                inOrder.verify(encoder).encodeFrame(frame(3), outputStream);
                inOrder.verify(encoder).encodeFrame(frame(4), outputStream);
                inOrder.verify(encoder).encodeFrame(SnowDataFrame.LAST, outputStream);
            }
        });
    }

    @RepeatedTest(3)
    void givenMassiveChunkOfFrames_whenFiveThreadsAreStreaming_thenNoDeadlockOccurs() throws Throwable {
        final int numOfFramesToTest = 5_000;

        AtomicInteger frameNum = new AtomicInteger(0);
        when(decoder.decodeFrame(any())).then(i -> {
            int num = frameNum.incrementAndGet();
            return num <= numOfFramesToTest ? frame(num) : SnowDataFrame.LAST;

        });

        testUsingFiveStreamingThreads();
    }

    @RepeatedTest(3)
    void givenMassiveChunkOfFrames_whenFiveThreadsAreStreamingFromSluggishConsumer_thenNoDeadlockOccurs() throws Throwable {
        final int numOfFramesToTest = 5_000;

        AtomicInteger frameNum = new AtomicInteger(0);
        when(decoder.decodeFrame(any())).then(i -> {
            Thread.sleep(ThreadLocalRandom.current().nextInt(0, 1));

            final int num = frameNum.incrementAndGet();
            return num <= numOfFramesToTest ? frame(num) : SnowDataFrame.LAST;
        });

        testUsingFiveStreamingThreads();
    }

    @RepeatedTest(3)
    void givenMassiveChunkOfFrames_whenFiveSluggishThreadsAreStreaming_thenNoDeadlockOccurs() throws Throwable {
        final int numOfFramesToTest = 5_000;

        AtomicInteger frameNum = new AtomicInteger(0);
        when(decoder.decodeFrame(any())).then(i -> {
            final int num = frameNum.incrementAndGet();
            return num <= numOfFramesToTest ? frame(num) : SnowDataFrame.LAST;
        });

        doAnswer(i -> {
            Thread.sleep(ThreadLocalRandom.current().nextInt(0, 2));
            return null;
        }).when(encoder).encodeFrame(any(), any());

        testUsingFiveStreamingThreads();
    }

    @RepeatedTest(3)
    void givenMassiveChunkOfFrames_whenFiveSluggishThreadsAreStreamingFromSluggishConsumer_thenNoDeadlockOccurs() throws Throwable {
        final int numOfFramesToTest = 5_000;

        AtomicInteger frameNum = new AtomicInteger(0);
        when(decoder.decodeFrame(any())).then(i -> {
            Thread.sleep(ThreadLocalRandom.current().nextInt(0, 1));

            final int num = frameNum.incrementAndGet();
            return num <= numOfFramesToTest ? frame(num) : SnowDataFrame.LAST;
        });

        doAnswer(i -> {
            Thread.sleep(ThreadLocalRandom.current().nextInt(0, 2));
            return null;
        }).when(encoder).encodeFrame(any(), any());

        testUsingFiveStreamingThreads();
    }

    private void testUsingFiveStreamingThreads() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            final Semaphore readyToStream = new Semaphore(0);

            void threadConsumer() throws IOException, InterruptedException {
                when(phpSnow.isAlive()).thenReturn(true);
                snowStream.startConsumingSnowData();
                readyToStream.release(5);
                snowStream.waitUntilConsumerThreadFinished();
            }

            void thread1() throws IOException, InterruptedException, ConsumerThreadException {
                testThread();
            }

            void thread2() throws IOException, InterruptedException, ConsumerThreadException {
                testThread();
            }

            void thread3() throws IOException, InterruptedException, ConsumerThreadException {
                testThread();
            }

            void thread4() throws IOException, InterruptedException, ConsumerThreadException {
                testThread();
            }

            void thread5() throws IOException, InterruptedException, ConsumerThreadException {
                testThread();
            }

            private void testThread() throws InterruptedException, ConsumerThreadException, IOException {
                ByteArrayOutputStream outputStream = mock(ByteArrayOutputStream.class);

                readyToStream.acquire();

                snowStream.streamTo(client);
            }
        }, 10, 30);
    }

}