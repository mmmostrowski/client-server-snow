package techbit.snow.proxy.snow.stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import techbit.snow.proxy.config.PhpSnowConfig;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowBasis;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.error.IncompatibleConfigException;
import techbit.snow.proxy.snow.stream.SnowStream.ConsumerThreadException;

import java.io.IOException;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static techbit.snow.proxy.snow.stream.TestingFrames.*;

@ExtendWith(MockitoExtension.class)
class SnowStreamTest extends SnowStreamBaseTest {

    public SnowStreamTest(@Mock SnowDataBuffer buffer) {
        super(buffer);
    }

    @Test
    void whenStartPhpApp_thenPhpAppIsStarted() throws IOException {
        snowStream.startPhpApp();

        verify(phpSnow).start();
    }

    @Test
    void whenStartPhpApp_thenItIsNotActive() throws IOException {
        snowStream.startPhpApp();

        assertFalse(snowStream.isActive());
    }

    @Test
    void whenStartConsumingSnowData_thenItIsActive() throws IOException {
        when(phpSnow.isAlive()).thenReturn(true);

        snowStream.startConsumingSnowData();

        assertTrue(snowStream.isActive());
    }

    @Test
    void givenStoppedStream_whenStartPhpApp_thenThrowException() throws IOException, InterruptedException {
        snowStream.stop();
        assertThrows(IllegalStateException.class, snowStream::startPhpApp);
    }

    @Test
    void givenStoppedStream_whenStartConsumingSnowData_thenThrowException() throws IOException, InterruptedException {
        snowStream.stop();
        assertThrows(IllegalStateException.class, snowStream::startConsumingSnowData);
    }

    @Test
    void givenStoppedStream_whenStopAgain_thenNoExceptionIsThrown() throws IOException, InterruptedException {
        snowStream.stop();
        assertDoesNotThrow(snowStream::stop);
    }

    @Test
    void whenStartPhpApp_thenPipeIsDestroyed() throws IOException {
        snowStream.startPhpApp();

        verify(pipe, times(1)).destroy();
    }

    @Test
    void whenStartPhpApp_thenBufferIsNotDestroyed() throws IOException {
        snowStream.startPhpApp();

        verify(buffer, never()).destroy();
    }

    @Test
    void whenCompatibleConfig_thenNoErrorOccurs() {
        PhpSnowConfig snowConfig = new PhpSnowConfig(
                "testingPreset", 87, 76, Duration.ofMinutes(11), 21);
        assertDoesNotThrow(() -> snowStream.ensureCompatibleWithConfig(snowConfig));
    }

    @Test
    void whenIncompatibleConfig_thenThrowException() {
        PhpSnowConfig snowConfig = new PhpSnowConfig(
                "changedTestingPreset", 87, 76, Duration.ofMinutes(11), 21);
        assertThrows(IncompatibleConfigException.class, () -> snowStream.ensureCompatibleWithConfig(snowConfig));
    }

    @Test
    void whenAskingForDetails_thenProvidingThemFromSnowStream() {
        PhpSnowConfig details = snowStream.config();

        Assertions.assertEquals(snowConfig, details);
    }

    @Test
    void givenNoPhpStart_whenStartSnowDataConsumption_thenThrowException() {
        assertThrows(IllegalStateException.class, snowStream::startConsumingSnowData);
    }

    @Test
    void givenNoPhpStart_whenStartStreamingToClient_thenThrowException() {
        assertThrows(IOException.class, () -> snowStream.streamTo(client));
    }

    @Test
    void whenStop_thenBufferIsDestroyed() throws IOException, InterruptedException {
        when(phpSnow.isAlive()).thenReturn(true);

        snowStream.startConsumingSnowData();
        snowStream.stop();

        verify(buffer, atLeastOnce()).destroy();
    }

    @Test
    void whenStopTakesTooLong_thenStreamIsShutdownAnyway() throws IOException, InterruptedException {
        when(phpSnow.isAlive()).thenReturn(true);

        doAnswer(i -> {
            synchronized (this) {
                wait(); // forever
                return null;
            }
        }).when(buffer).push(SnowDataFrame.LAST);

        snowStream.startConsumingSnowData();
        snowStream.stop();

        verify(buffer, atLeastOnce()).destroy();
    }

    @Test
    void whenStop_thenIsNotActive() throws IOException, InterruptedException {
        when(phpSnow.isAlive()).thenReturn(true);

        snowStream.startConsumingSnowData();

        assertTrue(snowStream.isActive());

        snowStream.stop();
        snowStream.waitUntilConsumerThreadFinished();

        assertFalse(snowStream.isActive());
    }

    @Test
    void whenStop_thenSnowStreamFinishedEventIsEmitted() throws IOException, InterruptedException {
        when(phpSnow.isAlive()).thenReturn(true);

        snowStream.startConsumingSnowData();
        snowStream.stop();
        snowStream.waitUntilConsumerThreadFinished();

        verify(eventPublisher).publishEvent(any(SnowStream.SnowStreamFinishedEvent.class));
    }

    @Test
    void whenSnowStreamFinishedEventIsEmitted_thenItProvidesSessionId() {
        SnowStream.SnowStreamFinishedEvent event = snowStream.createSnowStreamFinishedEvent();
        assertEquals("session-xyz", event.getSessionId());
    }

    @Test
    void givenCustomClient_whenStream_thenInitializationHookIsInvoked() throws ConsumerThreadException, IOException, InterruptedException {
        when(phpSnow.isAlive()).thenReturn(true);
        when(buffer.firstFrame()).thenReturn(SnowDataFrame.LAST);

        snowStream.startConsumingSnowData();
        snowStream.streamTo(client);

        verify(client).startStreaming(any(), any());
    }

    @Test
    void givenCustomClient_whenStream_thenFinishHookIsInvoked() throws ConsumerThreadException, IOException, InterruptedException {
        when(phpSnow.isAlive()).thenReturn(true);
        when(buffer.firstFrame()).thenReturn(frame(1));
        when(buffer.nextFrame(frame(1))).thenReturn(SnowDataFrame.LAST);

        snowStream.startConsumingSnowData();
        snowStream.streamTo(client);

        verify(client).stopStreaming();
    }

    @Test
    void givenCustomClient_whenStream_thenOnFrameEncodedHookInvoked() throws ConsumerThreadException, IOException, InterruptedException {
        when(phpSnow.isAlive()).thenReturn(true);
        when(buffer.firstFrame()).thenReturn(frame(1));
        when(buffer.nextFrame(frame(1))).thenReturn(frame(2));
        when(buffer.nextFrame(frame(2))).thenReturn(SnowDataFrame.LAST);

        snowStream.startConsumingSnowData();
        snowStream.streamTo(client);

        verify(client).streamFrame(frame(1), SnowBasis.NONE);
        verify(client).streamFrame(frame(2), SnowBasis.NONE);
    }

    @Test
    void givenCustomClient_whenStreamIsDeactivated_thenFrameLoopBreaks() throws ConsumerThreadException, IOException, InterruptedException {
        when(phpSnow.isAlive()).thenReturn(true);
        when(buffer.firstFrame()).thenReturn(frame(1));
        when(buffer.nextFrame(frame(1))).thenReturn(frame(2));
        when(buffer.nextFrame(frame(2))).thenReturn(frame(3));
        when(buffer.nextFrame(frame(3))).thenReturn(SnowDataFrame.LAST);

        final AtomicInteger c = new AtomicInteger();
        when(client.continueStreaming()).then((i) -> c.incrementAndGet() < 3);

        snowStream.startConsumingSnowData();
        snowStream.streamTo(client);

        verify(client).streamFrame(frame(1), SnowBasis.NONE);
        verify(client).streamFrame(frame(2), SnowBasis.NONE);
        verify(client, never()).streamFrame(frame(3), SnowBasis.NONE);
    }

    @Test
    void whenStop_thenPhpAppIsStopped() throws IOException, InterruptedException {
        snowStream.stop();

        verify(phpSnow, times(1)).stop();
    }

    @Test
    void whenStop_thenPipeIsDestroyed() throws IOException, InterruptedException {
        snowStream.stop();

        verify(pipe, times(1)).destroy();
    }

    @Test
    void whenInputDataIsStreamed_thenMetadataIsStreamedToOutput() throws IOException, InterruptedException, ConsumerThreadException {
        SnowAnimationMetadata metadata = mock(SnowAnimationMetadata.class);
        when(decoder.decodeMetadata(any(), any(), any())).thenReturn(metadata);
        when(buffer.firstFrame()).thenReturn(SnowDataFrame.LAST);
        when(phpSnow.isAlive()).thenReturn(true);

        snowStream.startConsumingSnowData();
        snowStream.streamTo(client);

        verify(encoder, times(1)).encodeMetadata(metadata, outputStream);
    }

    @Test
    void whenFramesInBuffer_thenFramesAreStreamedToOutput() throws IOException, InterruptedException, ConsumerThreadException {
        when(phpSnow.isAlive()).thenReturn(true);
        when(buffer.firstFrame()).thenReturn(frame(1));
        when(buffer.nextFrame(frame(1))).thenReturn(frame(2));
        when(buffer.nextFrame(frame(2))).thenReturn(frame(3));
        when(buffer.nextFrame(frame(3))).thenReturn(frame(4));
        when(buffer.nextFrame(frame(4))).thenReturn(SnowDataFrame.LAST);

        snowStream.startConsumingSnowData();
        snowStream.streamTo(client);

        InOrder inOrder = inOrder(encoder);

        inOrder.verify(encoder).encodeFrame(frame(1), outputStream);
        inOrder.verify(encoder).encodeFrame(frame(2), outputStream);
        inOrder.verify(encoder).encodeFrame(frame(3), outputStream);
        inOrder.verify(encoder).encodeFrame(frame(4), outputStream);
        inOrder.verify(encoder).encodeFrame(SnowDataFrame.LAST, outputStream);
    }

    @Test
    void givenBasisChangesEveryNthFrame_whenTakingFramesFromBuffer_thenOnlyBasisChangesAreStreamedToOutput() throws InterruptedException, IOException, ConsumerThreadException {
        SnowDataFrame frame1 = frameWithBasis(1, 1);
        SnowDataFrame frame2 = frameWithBasis(2, 1);
        SnowDataFrame frame3 = frameWithBasis(3, 2);
        SnowDataFrame frame4 = frameWithBasis(4, 2);
        SnowDataFrame frame5 = frameWithBasis(5, 2);
        SnowDataFrame frame6 = frameWithBasis(6, 3);
        SnowDataFrame frame7 = frameWithBasis(7, 3);
        SnowDataFrame frame8 = frameWithBasis(8, 3);
        SnowDataFrame frame9 = frameWithBasis(9, 3);

        when(phpSnow.isAlive()).thenReturn(true);
        when(buffer.firstFrame()).thenReturn(frame1);
        when(buffer.nextFrame(frame1)).thenReturn(frame2);
        when(buffer.nextFrame(frame2)).thenReturn(frame3);
        when(buffer.nextFrame(frame3)).thenReturn(frame4);
        when(buffer.nextFrame(frame4)).thenReturn(frame5);
        when(buffer.nextFrame(frame5)).thenReturn(frame6);
        when(buffer.nextFrame(frame6)).thenReturn(frame7);
        when(buffer.nextFrame(frame7)).thenReturn(frame8);
        when(buffer.nextFrame(frame8)).thenReturn(frame9);
        when(buffer.nextFrame(frame9)).thenReturn(SnowDataFrame.LAST);


        snowStream.startConsumingSnowData();
        snowStream.streamTo(client);

        InOrder inOrder = inOrder(encoder);

        inOrder.verify(encoder).encodeBasis(basis( 1), outputStream);
        inOrder.verify(encoder).encodeBasis(SnowBasis.NONE, outputStream);
        inOrder.verify(encoder).encodeBasis(basis( 2), outputStream);
        inOrder.verify(encoder).encodeBasis(SnowBasis.NONE, outputStream);
        inOrder.verify(encoder).encodeBasis(SnowBasis.NONE, outputStream);
        inOrder.verify(encoder).encodeBasis(basis( 3), outputStream);
        inOrder.verify(encoder).encodeBasis(SnowBasis.NONE, outputStream);
        inOrder.verify(encoder).encodeBasis(SnowBasis.NONE, outputStream);
        inOrder.verify(encoder).encodeBasis(SnowBasis.NONE, outputStream);
    }

    @Test
    void givenSequenceOfDataFramesIncludingLastOne_whenStreamingFrames_thenFramesArePushedToBuffer() throws IOException, InterruptedException {
        when(phpSnow.isAlive()).thenReturn(true);

        snowStream.startConsumingSnowData();
        snowStream.waitUntilConsumerThreadFinished();

        InOrder inOrder = inOrder(buffer);
        inOrder.verify(buffer).push(frame(1));
        inOrder.verify(buffer).push(frame(2));
        inOrder.verify(buffer).push(frame(3));
        inOrder.verify(buffer).push(frame(4));
        inOrder.verify(buffer).push(SnowDataFrame.LAST);
    }

    @Test
    void givenSequenceOfDataFramesWithoutLastOne_whenStreamingFrames_thenLastFrameIsPushedToBufferAnyway() throws IOException, InterruptedException {
        final Iterator<?> inputFrames = List.of(
                frame(1),
                frame(2),
                frame(3),
                frame(4)
                // no SnowDataFrame.last
        ).iterator();

        when(decoder.decodeFrame(any())).then(i -> inputFrames.next());
        when(phpSnow.isAlive()).then(i -> inputFrames.hasNext());

        snowStream.startConsumingSnowData();
        snowStream.waitUntilConsumerThreadFinished();

        InOrder inOrder = inOrder(buffer);
        inOrder.verify(buffer).push(frame(1));
        inOrder.verify(buffer).push(frame(2));
        inOrder.verify(buffer).push(frame(3));
        inOrder.verify(buffer).push(frame(4));
        inOrder.verify(buffer).push(SnowDataFrame.LAST);
    }

    @Test
    void givenBasisChangesEveryNthFrame_whenStreamingFrames_thenEachFramePointsToRecentBasis() throws IOException, InterruptedException {
        List<SnowDataFrame> frames = List.of(
                frameWithNoBasis(1),
                frameWithBasis(2, 1),
                frameWithNoBasis(3),
                frameWithNoBasis(4),
                frameWithBasis(5, 2),
                frameWithNoBasis(6),
                frameWithNoBasis(7),
                frameWithNoBasis(8),
                frameWithBasis(9, 3),
                SnowDataFrame.LAST
        );
        final Iterator<SnowDataFrame> inputFrames = frames.iterator();
        when(decoder.decodeFrame(any())).then(i -> inputFrames.next());
        final Iterator<SnowDataFrame> inputBasis = frames.iterator();
        when(decoder.decodeBasis(any())).then(i -> inputBasis.next().basis());
        when(phpSnow.isAlive()).then(i -> inputFrames.hasNext());

        snowStream.startConsumingSnowData();
        snowStream.waitUntilConsumerThreadFinished();

        InOrder inOrder = inOrder(buffer);
        inOrder.verify(buffer).push(frameWithNoBasis(1));
        inOrder.verify(buffer).push(frameWithBasis(2, 1));
        inOrder.verify(buffer).push(frameWithBasis(3, 1));
        inOrder.verify(buffer).push(frameWithBasis(4, 1));
        inOrder.verify(buffer).push(frameWithBasis(5, 2));
        inOrder.verify(buffer).push(frameWithBasis(6, 2));
        inOrder.verify(buffer).push(frameWithBasis(7, 2));
        inOrder.verify(buffer).push(frameWithBasis(8, 2));
        inOrder.verify(buffer).push(frameWithBasis(9, 2));
        inOrder.verify(buffer).push(SnowDataFrame.LAST);
    }

    @Test
    void whenPhpAppIsGoingDownAtSomePoint_thenFinishingConsumerThreadGracefully() throws IOException, InterruptedException {
        final Iterator<Boolean> isAliveFlags = List.of(true, true, false, false, false).iterator();
        when(phpSnow.isAlive()).then(i -> isAliveFlags.next());

        snowStream.startConsumingSnowData();
        snowStream.waitUntilConsumerThreadFinished();

        verify(buffer, times(2)).push(any());
        verify(buffer, times(1)).push(SnowDataFrame.LAST);

        assertFalse(snowStream.isActive());
    }

    @Test
    void whenConsumerThreadIsThrowingException_thenStopStreaming() throws IOException, InterruptedException {
        when(phpSnow.isAlive()).thenReturn(true);
        doNothing().when(buffer).push(any(SnowDataFrame.class));
        doThrow(RuntimeException.class).when(buffer).push(frame(3));

        snowStream.startConsumingSnowData();
        snowStream.waitUntilConsumerThreadFinished();

        verify(buffer).push(frame(1));
        verify(buffer).push(frame(2));
        verify(buffer).push(frame(3));
        verify(buffer, never()).push(frame(4));
    }

    @Test
    void whenConsumerThreadIsThrowingException_thenPassExceptionToClient() throws IOException, InterruptedException {
        final RuntimeException customException = new RuntimeException("Custom Test Exception Passed Properly") {};
        when(phpSnow.isAlive()).thenReturn(true);
        doNothing().when(buffer).push(any(SnowDataFrame.class));
        doThrow(customException).when(buffer).push(frame(3));

        snowStream.startConsumingSnowData();
        snowStream.waitUntilConsumerThreadFinished();

        Throwable thrownException = assertThrows(ConsumerThreadException.class, () -> snowStream.streamTo(client));
        assertSame(customException, thrownException.getCause());
    }

}