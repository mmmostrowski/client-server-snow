package techbit.snow.proxy.service.stream;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfig;
import techbit.snow.proxy.service.stream.SnowStream.ConsumerThreadException;

import java.io.IOException;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    void givenStoppedStream_whenStartPhpApp_thenThrowException() throws IOException, InterruptedException {
        snowStream.stop();
        assertThrows(Exception.class, snowStream::startPhpApp);
    }

    @Test
    void givenStoppedStream_whenStartConsumingSnowData_thenThrowException() throws IOException, InterruptedException {
        snowStream.stop();
        assertThrows(Exception.class, snowStream::startConsumingSnowData);
    }

    @Test
    void givenStoppedStream_whenStopAgain_thenNoExceptionIsThrown() throws IOException, InterruptedException {
        snowStream.stop();
        assertDoesNotThrow(snowStream::stop);
    }

    @Test
    void whenStartPhpApp_thenPipeIsDestroyed() throws IOException {
        snowStream.startPhpApp();

        verify(pipe).destroy();
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
        assertThrows(Exception.class, () -> snowStream.ensureCompatibleWithConfig(snowConfig));
    }

    @Test
    void givenNoPhpStart_whenStartSnowDataConsumption_thenThrowException() {
        assertThrows(Exception.class, snowStream::startConsumingSnowData);
    }

    @Test
    void givenNoPhpStart_whenStartStreamingToClient_thenThrowException() {
        assertThrows(Exception.class, () -> snowStream.streamTo(outputStream));
    }

    @RepeatedTest(20)
    void whenStop_thenBufferIsDestroyed() throws IOException, InterruptedException {
        when(phpSnow.isAlive()).thenReturn(true);

        snowStream.startConsumingSnowData();

        snowStream.stop();

        verify(buffer, atLeastOnce()).destroy();
    }

    @RepeatedTest(20)
    void givenPhpStart_whenStop_thenIsNotActive() throws IOException, InterruptedException {
        when(phpSnow.isAlive()).thenReturn(true);

        snowStream.startConsumingSnowData();

        assertTrue(snowStream.isActive());
        snowStream.stop();
        snowStream.waitUntilConsumerThreadFinished();
        assertFalse(snowStream.isActive());
    }

    @Test
    void whenStop_thenPhpAppIsStopped() throws IOException, InterruptedException {
        snowStream.stop();

        verify(phpSnow).stop();
    }

    @Test
    void whenStop_thenPipeIsDestroyed() throws IOException, InterruptedException {
        snowStream.stop();

        verify(pipe).destroy();
    }

    @Test
    void whenInputDataIsStreamed_thenMetadataIsStreamedToOutput() throws IOException, InterruptedException, ConsumerThreadException {
        SnowAnimationMetadata metadata = mock(SnowAnimationMetadata.class);
        when(decoder.decodeMetadata(any())).thenReturn(metadata);
        when(buffer.firstFrame()).thenReturn(SnowDataFrame.LAST);
        when(phpSnow.isAlive()).thenReturn(true);

        snowStream.startConsumingSnowData();
        snowStream.streamTo(outputStream);

        verify(encoder).encodeMetadata(metadata, outputStream);
    }

    @Test
    void givenSequenceOfDataFramesWithLastOne_whenInputDataIsStreamed_thenFramesAreStoredInBuffer() throws IOException, InterruptedException {
        when(phpSnow.isAlive()).thenReturn(true);

        snowStream.startConsumingSnowData();
        snowStream.waitUntilConsumerThreadFinished();

        InOrder inOrder = Mockito.inOrder(buffer);
        inOrder.verify(buffer).push(frame(1));
        inOrder.verify(buffer).push(frame(2));
        inOrder.verify(buffer).push(frame(3));
        inOrder.verify(buffer).push(frame(4));
        inOrder.verify(buffer).push(SnowDataFrame.LAST);
    }

    @Test
    void givenSequenceOfDataFramesWithoutLastOne_whenInputDataIsStreamed_thenLastFrameIsStoredInBufferAnyway() throws IOException, InterruptedException {
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

        InOrder inOrder = Mockito.inOrder(buffer);
        inOrder.verify(buffer).push(frame(1));
        inOrder.verify(buffer).push(frame(2));
        inOrder.verify(buffer).push(frame(3));
        inOrder.verify(buffer).push(frame(4));
        inOrder.verify(buffer).push(SnowDataFrame.LAST);
    }

    @Test
    void whenAddingFramesToBuffer_thenFramesAreStreamedToOutput() throws IOException, InterruptedException, ConsumerThreadException {
        when(phpSnow.isAlive()).thenReturn(true);
        when(buffer.firstFrame()).thenReturn(frame(1));
        when(buffer.nextFrame(frame(1))).thenReturn(frame(2));
        when(buffer.nextFrame(frame(2))).thenReturn(frame(3));
        when(buffer.nextFrame(frame(3))).thenReturn(frame(4));
        when(buffer.nextFrame(frame(4))).thenReturn(SnowDataFrame.LAST);

        snowStream.startConsumingSnowData();
        snowStream.streamTo(outputStream);

        InOrder inOrder = inOrder(encoder);

        inOrder.verify(encoder).encodeFrame(frame(1), outputStream);
        inOrder.verify(encoder).encodeFrame(frame(2), outputStream);
        inOrder.verify(encoder).encodeFrame(frame(3), outputStream);
        inOrder.verify(encoder).encodeFrame(frame(4), outputStream);
        inOrder.verify(encoder).encodeFrame(SnowDataFrame.LAST, outputStream);
    }

    @Test
    void whenAllInputDataAreStreamed_thenConsumerThreadIsFinished() throws IOException, InterruptedException {
        when(phpSnow.isAlive()).thenReturn(true);

        snowStream.startConsumingSnowData();
        snowStream.waitUntilConsumerThreadFinished();

        assertFalse(snowStream.isActive());
    }

    @Test
    void whenPhpAppIsDown_thenConsumerThreadIsFinished() throws IOException, InterruptedException {
        final Iterator<Boolean> isAliveFlags = List.of(true, true, false, false, false).iterator();
        when(phpSnow.isAlive()).then(i -> isAliveFlags.next());

        snowStream.startConsumingSnowData();
        snowStream.waitUntilConsumerThreadFinished();

        verify(buffer, times(2)).push(any());

        assertFalse(snowStream.isActive());
    }

    @Test
    void whenConsumerThreadThrowingException_thenStopStreamingAndPassExceptionToClient() throws IOException, InterruptedException {
        doNothing().when(buffer).push(any(SnowDataFrame.class));
        RuntimeException customException = new RuntimeException() {};
        doThrow(customException).when(buffer).push(frame(3));

        when(phpSnow.isAlive()).thenReturn(true);

        snowStream.startConsumingSnowData();
        snowStream.waitUntilConsumerThreadFinished();
        Throwable thrownException = assertThrows(ConsumerThreadException.class, () -> snowStream.streamTo(outputStream));

        verify(buffer).push(frame(1));
        verify(buffer).push(frame(2));
        verify(buffer).push(frame(3));
        verify(buffer, never()).push(frame(4));

        assertSame(customException, thrownException.getCause());
    }

}