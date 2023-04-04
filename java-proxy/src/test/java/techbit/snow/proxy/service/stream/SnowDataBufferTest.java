package techbit.snow.proxy.service.stream;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.service.stream.snow.SnowDataBuffer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static techbit.snow.proxy.service.stream.TestingFrames.frame;

@SuppressWarnings("unused")
@ExtendWith(MockitoExtension.class)
class SnowDataBufferTest {

    @Mock
    private BlockingBag<Integer, SnowDataFrame> bag;

    private SnowDataBuffer buffer;

    @BeforeEach
    void setUp() {
        buffer = new SnowDataBuffer(2, bag);
    }

    @Test
    void givenNoFramesInBuffer_whenAskedForFrame_thenWaitUntilFrameIsAvailable() throws Throwable {
        when(bag.take(1)).thenReturn(frame(1));

        TestFramework.runOnce(new MultithreadedTestCase() {
            void thread1() {
                waitForTick(1);
                buffer.push(frame(1));
            }

            void thread2() throws InterruptedException {
                SnowDataFrame frame = buffer.firstFrame();

                Assertions.assertEquals(frame(1), frame);
                assertTick(1);
            }
        });
    }

    @Test
    void givenRegisteredClients_whenWaitUntilAllClientsUnregister_thenNoDeadlockOccurs() throws Throwable {
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

            void thread2() {
                waitForTick(1);
                buffer.unregisterClient(clientId1);
            }

            void thread3() {
                waitForTick(1);
                buffer.unregisterClient(clientId2);
            }

            void thread4() {
                waitForTick(1);
                buffer.unregisterClient(clientId3);
            }

        });
    }

    @Test
    void whenNoRegisteredClients_thenNoBlocking() {
        assertDoesNotThrow(buffer::waitUntilAllClientsUnregister);
    }

    @Test
    void whenUnregisteringUnknownClient_thenThrowException() {
        Object known = new Object();
        buffer.registerClient(known);

        Object unknown = new Object();
        assertThrows(IllegalArgumentException.class, () -> buffer.unregisterClient(unknown));
    }


    @Test
    void whenFrameAddedToBuffer_thenItIsStoredInBag() {
        buffer.push(frame(1));

        verify(bag).put(1, frame(1));
    }

    @Test
    void whenTwoFramesAddedToBuffer_thenBothAreStoredInBag() {
        buffer.push(frame(1));
        buffer.push(frame(2));

        verify(bag).put(1, frame(1));
        verify(bag).put(2, frame(2));
    }

    @Test
    void whenThreeFramesAddedToBuffer_thenFirstOneIsRemoved() {
        buffer.push(frame(1));
        buffer.push(frame(2));
        buffer.push(frame(3));

        InOrder inOrder = inOrder(bag);

        inOrder.verify(bag).put(1, frame(1));
        inOrder.verify(bag).put(2, frame(2));
        verify(bag).put(3, frame(3));
        inOrder.verify(bag).remove(1);
    }

    @Test
    void whenFourFramesAddedToBuffer_thenTwoFirstAreRemoved() {
        buffer.push(frame(1));
        buffer.push(frame(2));
        buffer.push(frame(3));
        buffer.push(frame(4));

        InOrder inOrder = inOrder(bag);

        inOrder.verify(bag).put(1, frame(1));
        inOrder.verify(bag).put(2, frame(2));
        verify(bag).put(3, frame(3));
        verify(bag).put(4, frame(4));
        inOrder.verify(bag).remove(1);
        inOrder.verify(bag).remove(2);
    }

    @Test
    void whenFrameAdded_thenItCanBeTakenMultipleTimes() throws Exception {
        buffer.push(frame(1));

        when(bag.take(1)).thenReturn(frame(1));

        assertEquals(1, buffer.firstFrame().frameNum());
        assertEquals(1, buffer.firstFrame().frameNum());
    }

    @Test
    void whenAddingFrames_thenFirstFrameIsDrifting() throws Exception {
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
    void givenNextFrame_whenTakingIt_thenItIsAvailableWithNoBlocking() throws Exception {
        when(bag.take(2)).thenReturn(frame(2));

        buffer.push(frame(1));
        buffer.push(frame(2));
        SnowDataFrame nextFrame = buffer.nextFrame(frame(1));

        assertEquals(2, nextFrame.frameNum());
    }

    @Test
    void whenDeadFrameGiven_thenSkipToNextAlive() throws Exception {
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
    void whenFramesAddedInWrongOrder_thenExceptionIsThrown() {
        buffer.push(frame(1));

        assertThrows(IllegalArgumentException.class, () -> buffer.push(frame(3)) );
    }

    @Test
    void whenLastFrameIsAdded_thenItIsProvidedByBuffer() throws InterruptedException {
        buffer.push(frame(1));
        buffer.push(frame(2));
        buffer.push(frame(3));
        buffer.push(SnowDataFrame.LAST);

        assertEquals(SnowDataFrame.LAST, buffer.nextFrame(frame(3)));
        assertEquals(SnowDataFrame.LAST,  buffer.nextFrame(SnowDataFrame.LAST));
    }

    @Test
    void whenLastFrameIsAdded_thenPreviousFramesAreStillAvailable() throws Exception {
        when(bag.take(3)).thenReturn(frame(3));

        buffer.push(frame(1));
        buffer.push(frame(2));
        buffer.push(frame(3));
        buffer.push(SnowDataFrame.LAST);

        assertEquals(frame(3), buffer.nextFrame(frame(1)));
        assertEquals(frame(3), buffer.nextFrame(frame(2)));
    }

    @Test
    void givenLastFrameInBuffer_whenMoreFramesAdded_thenThrowException() {
        buffer.push(frame(1));
        buffer.push(frame(2));
        buffer.push(SnowDataFrame.LAST);

        assertThrows(IllegalArgumentException.class, () -> buffer.push(frame(3)));
    }

    @Test
    void whenBufferDestroyed_thenLastFrameIsAlwaysProvided() throws InterruptedException {
        buffer.push(frame(1));
        buffer.push(frame(2));
        buffer.destroy();

        assertEquals(SnowDataFrame.LAST, buffer.firstFrame());
        assertEquals(SnowDataFrame.LAST, buffer.nextFrame(frame(1)));
        assertEquals(SnowDataFrame.LAST, buffer.nextFrame(frame(2)));
        assertEquals(SnowDataFrame.LAST, buffer.nextFrame(frame(33)));
    }

    @Test
    void givenDestroyedBuffer_whenAddingFrames_thenExceptionIsThrown() {
        buffer.push(frame(1));
        buffer.destroy();

        assertThrows(IllegalStateException.class, () -> buffer.push(frame(2)));
    }

    @Test
    void whenInvalidSizeOfBuffer_thenExceptionITsThrown() {
        assertThrows(IllegalArgumentException.class, () -> new SnowDataBuffer(0, null));
        assertThrows(IllegalArgumentException.class, () -> new SnowDataBuffer(-1, null));
    }

    @Test
    void givenClientWaitingForNextFrame_whenBufferIsDestroyed_thenClientReceiveLastFrame() throws Throwable {
        buffer = new SnowDataBuffer(10, new BlockingBag<>());

        TestFramework.runOnce(new MultithreadedTestCase() {

            void thread1() {
                waitForTick(1);
                buffer.push(frame(1));
                waitForTick(2);
                buffer.destroy();
            }

            void thread2() throws InterruptedException {
                SnowDataFrame frame1 = buffer.firstFrame();
                assertTick(1);
                SnowDataFrame frame2 = buffer.nextFrame(frame1);
                assertTick(2);

                Assertions.assertEquals(SnowDataFrame.LAST, frame2);
            }
        }, 10, 100);
    }

}
