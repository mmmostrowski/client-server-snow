package techbit.snow.proxy.service.stream;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.service.stream.SnowStream.ConsumerThreadException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnowStreamAsyncTest extends SnowStreamBaseTest {

    public SnowStreamAsyncTest() {
        super(spy(new SnowDataBuffer(13, new BlockingBag<>())));
    }

    @RepeatedTest(5)
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

        Answer<SnowDataFrame> countDownLatch = i -> {
            // buffer.firstFrame|nextFrame(frame)
            SnowDataFrame frame = (SnowDataFrame) i.callRealMethod();
            latches[frame.frameNum() + 1].countDown();
            latches[frame.frameNum() + 1].await();
            return frame;
        };

        Answer<Void> awaitLatch = i -> {
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

                ByteArrayOutputStream outputStream = mock(ByteArrayOutputStream.class);

                snowStream.streamTo(outputStream);

                verify(encoder, times(5)).encodeFrame(any(), eq(outputStream));
                verify(encoder, times(1)).encodeMetadata(any(), eq(outputStream));
            }
        });
    }

    @RepeatedTest(3)
    void givenHealthyConsumerThread_whenStop_thenShutdownGracefully() throws Throwable {
        AtomicInteger frameNum = new AtomicInteger(0);
        when(decoder.decodeFrame(any())).then(i -> frame(frameNum.incrementAndGet()));

        TestFramework.runOnce(new MultithreadedTestCase() {
            void thread1() throws IOException {
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

    @RepeatedTest(2)
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

    @RepeatedTest(5)
    void whenAnimationFinished_thenEachClientCanReadBufferTillEnd() throws Throwable {
        CountDownLatch firstLoopLatch = new CountDownLatch(3);
        CountDownLatch remainingLoopsLatch = new CountDownLatch(1);

        Answer<Void> awaitLatch = i -> {
            SnowDataFrame frame = i.getArgument(0);
            i.callRealMethod(); // buffer.push(frame)

            if (frame.frameNum() == 1) {
                firstLoopLatch.await();
            } else if (frame.frameNum() == -1) {
                remainingLoopsLatch.countDown();
            }

            return null;
        };

        Answer<SnowDataFrame> countDownLatch = i -> {
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

                ByteArrayOutputStream outputStream = mock(ByteArrayOutputStream.class);

                snowStream.streamTo(outputStream);

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


    @RepeatedTest(2)
    void givenMassiveChunkOfFrames_whenFiveThreadsAreStreaming_thenNoDeadlockOccurs() throws Throwable {
        final int numOfFramesToTest = 10000;

        AtomicInteger frameNum = new AtomicInteger(0);
        when(decoder.decodeFrame(any())).then(i -> {
            int num = frameNum.incrementAndGet();
            return num <= numOfFramesToTest ? frame(num) : SnowDataFrame.LAST;

        });

        testUsingFiveStreamingThreads();
    }

    @RepeatedTest(2)
    void givenMassiveChunkOfFrames_whenFiveThreadsAreStreamingFromSluggishConsumer_thenNoDeadlockOccurs() throws Throwable {
        final int numOfFramesToTest = 10000;

        AtomicInteger frameNum = new AtomicInteger(0);
        when(decoder.decodeFrame(any())).then(i -> {
            Thread.sleep(ThreadLocalRandom.current().nextInt(0, 1));
            int num = frameNum.incrementAndGet();
            return num <= numOfFramesToTest ? frame(num) : SnowDataFrame.LAST;
        });

        testUsingFiveStreamingThreads();
    }

    @RepeatedTest(2)
    void givenMassiveChunkOfFrames_whenFiveSluggishThreadsAreStreaming_thenNoDeadlockOccurs() throws Throwable {
        final int numOfFramesToTest = 10000;

        AtomicInteger frameNum = new AtomicInteger(0);
        when(decoder.decodeFrame(any())).then(i -> {
            int num = frameNum.incrementAndGet();
            return num <= numOfFramesToTest ? frame(num) : SnowDataFrame.LAST;
        });

        doAnswer(i -> {
            Thread.sleep(ThreadLocalRandom.current().nextInt(0, 3));
            return null;
        }).when(encoder).encodeFrame(any(), any());

        testUsingFiveStreamingThreads();
    }

    @RepeatedTest(2)
    void givenMassiveChunkOfFrames_whenFiveSluggishThreadsAreStreamingFromSluggishConsumer_thenNoDeadlockOccurs() throws Throwable {
        final int numOfFramesToTest = 10000;

        AtomicInteger frameNum = new AtomicInteger(0);
        when(decoder.decodeFrame(any())).then(i -> {
            Thread.sleep(ThreadLocalRandom.current().nextInt(0, 2));
            int num = frameNum.incrementAndGet();
            return num <= numOfFramesToTest ? frame(num) : SnowDataFrame.LAST;
        });

        doAnswer(i -> {
            Thread.sleep(ThreadLocalRandom.current().nextInt(0, 4));
            return null;
        }).when(encoder).encodeFrame(any(), any());

        testUsingFiveStreamingThreads();
    }

    private void testUsingFiveStreamingThreads() throws Throwable {
        TestFramework.runOnce( new MultithreadedTestCase() {
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

                snowStream.streamTo(outputStream);

                verify(encoder, atLeastOnce()).encodeFrame(any(SnowDataFrame.class), eq(outputStream));
            }
        }, 10, 30);
    }


}