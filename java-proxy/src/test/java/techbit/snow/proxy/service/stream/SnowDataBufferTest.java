package techbit.snow.proxy.service.stream;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        when(bag.take(1)).thenReturn(frame(1));

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
    void whenHaveRegisteredClients_thenWaitUntilAllUnregistered() throws Throwable {
        Object clientId1 = new Object();
        Object clientId2 = new Object();
        Object clientId3 = new Object();

        buffer.registerClient(clientId1);
        buffer.registerClient(clientId2);
        buffer.registerClient(clientId3);

        TestFramework.runOnce(new MultithreadedTestCase() {
            void thread1() throws InterruptedException {
                buffer.waitUntilAllClientsUnregister();
                assertTick(1);
            }

            void thread2() throws InterruptedException {
                waitForTick(1);
                buffer.unregisterClient(clientId1);
            }

            void thread3() throws InterruptedException {
                waitForTick(1);
                buffer.unregisterClient(clientId2);
            }

            void thread4() throws InterruptedException {
                waitForTick(1);
                buffer.unregisterClient(clientId3);
            }

        });
    }

    @Test
    void whenNoRegisteredClients_thenNoBlocking() {
        assertDoesNotThrow(() -> buffer.waitUntilAllClientsUnregister());
    }

    @Test
    void whenUnregisteringUnknownClient_thenThrowException() {
        Object known = new Object();
        buffer.registerClient(known);

        Object unknown = new Object();
        assertThrows(IllegalArgumentException.class, () -> buffer.unregisterClient(unknown));
    }


    @Test
    void whenFrameAddedToBuffer_thenItIsStoredInBag() throws Throwable {
        buffer.push(frame(1));

        verify(bag).put(1, frame(1));
    }

    @Test
    void whenTwoFramesAddedToBuffer_thenBothAreStoredInBag() throws Throwable {
        buffer.push(frame(1));
        buffer.push(frame(2));

        verify(bag).put(1, frame(1));
        verify(bag).put(2, frame(2));
    }

    @Test
    void whenThreeFramesAddedToBuffer_thenFirstOneIsRemoved() throws Throwable {
        buffer.push(frame(1));
        buffer.push(frame(2));
        buffer.push(frame(3));

        InOrder inOrder = Mockito.inOrder(bag);

        inOrder.verify(bag).put(1, frame(1));
        inOrder.verify(bag).put(2, frame(2));
        verify(bag).put(3, frame(3));
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
        verify(bag).put(3, frame(3));
        verify(bag).put(4, frame(4));
        inOrder.verify(bag).remove(1);
        inOrder.verify(bag).remove(2);
    }

    @Test
    void whenFrameAdded_thenItCanBeTakenMultipleTimes() throws InterruptedException {
        buffer.push(frame(1));

        when(bag.take(1)).thenReturn(frame(1));

        assertEquals(1, buffer.firstFrame().frameNum());
        assertEquals(1, buffer.firstFrame().frameNum());
    }

    @Test
    void whenAddingFrames_thenFirstFrameIsDrifting() throws InterruptedException {
        when(bag.take(1)).thenReturn(frame(1));
        when(bag.take(2)).thenReturn(frame(2));
        when(bag.take(3)).thenReturn(frame(3));

        buffer.push(frame(1));
        assertEquals(1, buffer.firstFrame().frameNum());

        buffer.push(frame(2));
        assertEquals(1, buffer.firstFrame().frameNum());

        buffer.push(frame(3));
        assertEquals(2, buffer.firstFrame().frameNum());

        buffer.push(frame(4));
        assertEquals(3, buffer.firstFrame().frameNum());
    }

    @Test
    void whenFrameGiven_thenNextCanBeTaken() throws InterruptedException {
        when(bag.take(2)).thenReturn(frame(2));

        buffer.push(frame(1));
        buffer.push(frame(2));

        SnowDataFrame nextFrame = buffer.nextFrame(frame(1));
        assertEquals(2, nextFrame.frameNum());
    }

    @Test
    void whenDeadFrameGiven_thenSkipToNextAlive() throws InterruptedException {
        when(bag.take(3)).thenReturn(frame(3));

        buffer.push(frame(1));
        buffer.push(frame(2));
        buffer.push(frame(3));
        buffer.push(frame(4));

        SnowDataFrame deadFrame = frame(1);

        SnowDataFrame nextFrame = buffer.nextFrame(deadFrame);
        assertEquals(3, nextFrame.frameNum());
    }

    @Test
    void whenFramesAddedInWrongOrder_thenExceptionIsThrown() throws InterruptedException {
        buffer.push(frame(1));

        assertThrows(Exception.class, () -> buffer.push(frame(3)) );
    }

    @Test
    void whenLastFrameAdded_thenItIsProvidedFromBuffer() throws InterruptedException {
        buffer.push(frame(1));
        buffer.push(frame(2));
        buffer.push(frame(3));
        buffer.push(SnowDataFrame.last);

        when(bag.take(3)).thenReturn(frame(3));

        assertEquals(frame(3), buffer.nextFrame(frame(1)));
        assertEquals(frame(3), buffer.nextFrame(frame(2)));
        assertEquals(SnowDataFrame.last, buffer.nextFrame(frame(3)));
    }

    @Test
    void givenLastFrameInBuffer_whenNewFrameAdded_thenThrowException() throws InterruptedException {
        buffer.push(frame(1));
        buffer.push(frame(2));
        buffer.push(SnowDataFrame.last);
        assertThrows(IllegalArgumentException.class, () -> buffer.push(frame(3)));
    }

    @Test
    void whenBufferDestroyed_thenLastFrameIsProvided() throws InterruptedException {
        buffer.push(frame(1));
        buffer.push(frame(2));
        buffer.destroy();

        assertEquals(SnowDataFrame.last, buffer.firstFrame());
        assertEquals(SnowDataFrame.last, buffer.nextFrame(frame(1)));
        assertEquals(SnowDataFrame.last, buffer.nextFrame(frame(2)));
    }

    @Test
    void givenDestroyedBuffer_whenAddingFrames_thenExceptionIsThrown() throws InterruptedException {
        buffer.push(frame(1));
        buffer.destroy();

        assertThrows(Exception.class, () -> buffer.push(frame(2)));
    }

    @Test
    void whenNegativeSizeOfBuffer_thenExceptionIsThrown() {
        assertThrows(Exception.class, () -> new SnowDataBuffer(-1, null));
    }

    private SnowDataFrame frame(int frameNum) {
        return new SnowDataFrame(frameNum, 0, null, null, null);
    }
}
