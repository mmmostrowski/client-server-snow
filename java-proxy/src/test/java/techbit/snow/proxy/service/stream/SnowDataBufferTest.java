package techbit.snow.proxy.service.stream;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class SnowDataBufferTest {

    @Mock
    BlockingBag<Integer, SnowDataFrame> bag;

    SnowDataBuffer buffer;

    @BeforeEach
    void setUp() {
        buffer = new SnowDataBuffer(2, bag);
    }

    @Test
    void givenNoFramesInBuffer_whenAskedForFrame_thenWaitUntilFrameIsAvailable() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            void thread1() throws InterruptedException {
                buffer.firstFrame();
                assertTick(1);
            }

            void thread2() throws InterruptedException {
                waitForTick(1);
                buffer.push(frame(1));
            }
        });
    }

    @Test
    void whenFrameAddedToBuffer_thenItIsStoredInBag() throws Throwable {
        buffer.push(frame(1));

        Mockito.verify(bag).put(1, frame(1));
    }

    @Test
    void whenTwoFramesAddedToBuffer_thenBothAreStoredInBag() throws Throwable {
        buffer.push(frame(1));
        buffer.push(frame(2));

        Mockito.verify(bag).put(1, frame(1));
        Mockito.verify(bag).put(2, frame(2));
    }

    @Test
    void whenThreeFramesAddedToBuffer_thenFirstOneIsRemoved() throws Throwable {
        buffer.push(frame(1));
        buffer.push(frame(2));
        buffer.push(frame(3));

        InOrder inOrder = Mockito.inOrder(bag);

        inOrder.verify(bag).put(1, frame(1));
        inOrder.verify(bag).put(2, frame(2));
        Mockito.verify(bag).put(3, frame(3));
        inOrder.verify(bag).remove(1);
    }

    @Test
    void whenFourFramesAddedToBuffer_thenTwoFirstAreRemoved() throws Throwable {
        buffer.push(frame(1));
        buffer.push(frame(2));
        buffer.push(frame(3));
        buffer.push(frame(4));

        InOrder inOrder = Mockito.inOrder(bag);

        inOrder.verify(bag).put(1, frame(1));
        inOrder.verify(bag).put(2, frame(2));
        Mockito.verify(bag).put(3, frame(3));
        Mockito.verify(bag).put(4, frame(4));
        inOrder.verify(bag).remove(1);
        inOrder.verify(bag).remove(2);
    }

    @Test
    void whenFrameAdded_thenItCanBeTakenMultipleTimes() throws InterruptedException {
        buffer.push(frame(1));

        Mockito.when(bag.take(1)).thenReturn(Optional.of(frame(1)));

        Assertions.assertEquals(1, buffer.firstFrame().frameNum());
        Assertions.assertEquals(1, buffer.firstFrame().frameNum());
    }

    @Test
    void whenAddingFrames_thenFirstFrameIsDrifting() throws InterruptedException {
        Mockito.when(bag.take(1)).thenReturn(Optional.of(frame(1)));
        Mockito.when(bag.take(2)).thenReturn(Optional.of(frame(2)));
        Mockito.when(bag.take(3)).thenReturn(Optional.of(frame(3)));

        buffer.push(frame(1));
        Assertions.assertEquals(1, buffer.firstFrame().frameNum());

        buffer.push(frame(2));
        Assertions.assertEquals(1, buffer.firstFrame().frameNum());

        buffer.push(frame(3));
        Assertions.assertEquals(2, buffer.firstFrame().frameNum());

        buffer.push(frame(4));
        Assertions.assertEquals(3, buffer.firstFrame().frameNum());
    }

    @Test
    void whenFrameGiven_thenNextCanBeTaken() throws InterruptedException {
        Mockito.when(bag.take(2)).thenReturn(Optional.of(frame(2)));

        buffer.push(frame(1));
        buffer.push(frame(2));

        SnowDataFrame nextFrame = buffer.nextFrame(frame(1));
        Assertions.assertEquals(2, nextFrame.frameNum());
    }

    @Test
    void whenDeadFrameGiven_thenSkipToNextAlive() throws InterruptedException {
        Mockito.when(bag.take(3)).thenReturn(Optional.of(frame(3)));

        buffer.push(frame(1));
        buffer.push(frame(2));
        buffer.push(frame(3));
        buffer.push(frame(4));

        SnowDataFrame deadFrame = frame(1);

        SnowDataFrame nextFrame = buffer.nextFrame(deadFrame);
        Assertions.assertEquals(3, nextFrame.frameNum());
    }

    @Test
    void whenFramesAddedInWrongOrder_thenExceptionIsThrown() throws InterruptedException {
        buffer.push(frame(1));

        Assertions.assertThrows(Exception.class, () -> buffer.push(frame(3)) );
    }

    @Test
    void whenEmptyFrameAdded_thenItIsAvailableInBuffer() throws InterruptedException {
        buffer.push(frame(1));
        buffer.push(frame(2));
        buffer.push(SnowDataFrame.empty);

        Mockito.when(bag.take(3)).thenReturn(Optional.of(SnowDataFrame.empty));

        Assertions.assertEquals(SnowDataFrame.empty, buffer.nextFrame(frame(2)));
    }

    @Test
    void whenLastFrameAdded_thenItIsAvailableInBuffer() throws InterruptedException {
        buffer.push(frame(1));
        buffer.push(frame(2));
        buffer.push(SnowDataFrame.last);

        Mockito.when(bag.take(3)).thenReturn(Optional.of(SnowDataFrame.last));

        Assertions.assertEquals(SnowDataFrame.last, buffer.nextFrame(frame(2)));
    }

    @Test
    void whenBufferDestroyed_thenLastFrameIsProvided() throws InterruptedException {
        buffer.push(frame(1));
        buffer.push(frame(2));
        buffer.destroy();

        Assertions.assertEquals(SnowDataFrame.last, buffer.firstFrame());
        Assertions.assertEquals(SnowDataFrame.last, buffer.nextFrame(frame(1)));
        Assertions.assertEquals(SnowDataFrame.last, buffer.nextFrame(frame(2)));
    }

    @Test
    void givenDestroyedBuffer_whenAddingFrames_thenExceptionIsThrown() throws InterruptedException {
        buffer.push(frame(1));
        buffer.destroy();

        Assertions.assertThrows(Exception.class, () -> buffer.push(frame(2)));
    }

    @Test
    void whenNegativeSizeOfBuffer_thenExceptionIsThrown() {
        Assertions.assertThrows(Exception.class, () -> new SnowDataBuffer(-1));
    }

    private SnowDataFrame frame(int frameNum) {
        return new SnowDataFrame(frameNum, 0, null, null, null);
    }
}
