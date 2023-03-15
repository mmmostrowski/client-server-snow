package techbit.snow.proxy.service.stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;
import techbit.snow.proxy.service.phpsnow.PhpSnowApp;
import techbit.snow.proxy.service.phpsnow.PhpSnowConfig;
import techbit.snow.proxy.service.stream.encoding.StreamDecoder;
import techbit.snow.proxy.service.stream.encoding.StreamEncoder;

import java.io.*;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnowStreamTest {

    @Mock
    private NamedPipe pipe;
    @Mock
    private PhpSnowApp phpSnow;
    @Mock
    private SnowDataBuffer buffer;
    @Mock
    private StreamDecoder decoder;
    @Mock
    private StreamEncoder encoder;

    private SnowStream snowStream;
    private InputStream inputStream;
    private OutputStream outputStream;
    private List<SnowDataFrame> framesSequence;

    private List<SnowDataFrame> framesSequenceWithLast;

    @BeforeEach
    void setup() throws IOException {
        PhpSnowConfig config = new PhpSnowConfig("testingPreset", 87, 76, Duration.ofMinutes(11), 21);
        snowStream = new SnowStream("session-xyz", config, pipe, phpSnow, buffer, decoder, encoder);
        inputStream = new ByteArrayInputStream(new byte[] {
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        });
        outputStream = new ByteArrayOutputStream();
        framesSequence = List.of(
                frame(1),
                frame(2),
                frame(3),
                frame(4)
        );
        framesSequenceWithLast = Stream.concat(framesSequence.stream(), Stream.of(SnowDataFrame.last))
                        .collect(Collectors.toList());

        lenient().when(pipe.inputStream()).thenReturn(inputStream);

        final Iterator<?> inputFrames = framesSequenceWithLast.iterator();
        lenient().when(decoder.decodeFrame(any())).then(i -> inputFrames.next());
    }

    private SnowDataFrame frame(int frameNum) {
        return new SnowDataFrame(frameNum, 0, null, null, null);
    }

    @Test
    void whenStartPhpApp_thenPhpAppIsStarted() throws IOException, InterruptedException {
        snowStream.startPhpApp();

        verify(phpSnow).start();
    }

    @Test
    void whenStartPhpApp_thenItIsNotActive() throws IOException, InterruptedException {
        snowStream.startPhpApp();

        assertFalse(snowStream.isActive());
    }

    @Test
    void whenStartPhpApp_thenPhpAppIsStopped() throws IOException, InterruptedException {
        snowStream.startPhpApp();

        verify(phpSnow).stop();
    }

    @Test
    void whenStartPhpApp_thenPipeIsDestroyed() throws IOException, InterruptedException {
        snowStream.startPhpApp();

        verify(pipe).destroy();
    }

    @Test
    void whenStartPhpApp_thenBufferIsNotDestroyed() throws IOException, InterruptedException {
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
        assertThrows(Exception.class, () -> snowStream.startConsumingSnowData());
    }

    @Test
    void givenNoPhpStart_whenStartStreamingToClient_thenThrowException() {
        assertThrows(Exception.class, () -> snowStream.streamTo(outputStream));
    }

    @Test
    void givenPhpStart_whenStop_thenBufferIsDestroyed() throws IOException, InterruptedException {
        when(phpSnow.isAlive()).thenReturn(true);

        snowStream.startPhpApp();
        snowStream.startConsumingSnowData();;

        snowStream.stop();

        verify(buffer, atLeastOnce()).destroy();
    }

    @Test
    void givenPhpStart_whenStop_thenIsNotActive() throws IOException, InterruptedException {
        snowStream.startPhpApp();
        when(phpSnow.isAlive()).thenReturn(true);
        snowStream.startConsumingSnowData();

        Assertions.assertTrue(snowStream.isActive());
        snowStream.stop();
        snowStream.waitUntilConsumerThreadFinished();
        Assertions.assertFalse(snowStream.isActive());
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
    void whenInputDataIsStreamed_thenMetadataIsStreamedToOutput() throws IOException, InterruptedException {
        SnowAnimationMetadata metadata = mock(SnowAnimationMetadata.class);
        when(decoder.decodeMetadata(any())).thenReturn(metadata);
        when(decoder.decodeFrame(any())).thenReturn(SnowDataFrame.last);
        when(buffer.firstFrame()).thenReturn(SnowDataFrame.last);
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
        inOrder.verify(buffer).push(SnowDataFrame.last);
    }

    @Test
    void givenSequenceOfDataFramesWithoutLastOne_whenInputDataIsStreamed_thenLastFrameIsAlsoStoredInBuffer() throws IOException, InterruptedException {
        final Iterator<?> inputFrames = framesSequence.iterator();
        when(decoder.decodeFrame(any())).then(i -> inputFrames.next());
        when(phpSnow.isAlive()).then(i -> inputFrames.hasNext());

        snowStream.startConsumingSnowData();
        snowStream.waitUntilConsumerThreadFinished();

        InOrder inOrder = Mockito.inOrder(buffer);
        inOrder.verify(buffer).push(frame(1));
        inOrder.verify(buffer).push(frame(2));
        inOrder.verify(buffer).push(frame(3));
        inOrder.verify(buffer).push(frame(4));

        inOrder.verify(buffer).push(SnowDataFrame.last);
    }

    @Test
    void whenAddingFramesToBuffer_thenFramesAreStreamedToOutput() throws IOException, InterruptedException {
        when(phpSnow.isAlive()).thenReturn(true);
        when(buffer.firstFrame()).thenReturn(frame(1));
        when(buffer.nextFrame(frame(1))).thenReturn(frame(2));
        when(buffer.nextFrame(frame(2))).thenReturn(frame(3));
        when(buffer.nextFrame(frame(3))).thenReturn(frame(4));
        when(buffer.nextFrame(frame(4))).thenReturn(SnowDataFrame.last);

        snowStream.startConsumingSnowData();
        snowStream.streamTo(outputStream);

        InOrder inOrder = inOrder(encoder);

        inOrder.verify(encoder).encodeFrame(frame(1), outputStream);
        inOrder.verify(encoder).encodeFrame(frame(2), outputStream);
        inOrder.verify(encoder).encodeFrame(frame(3), outputStream);
        inOrder.verify(encoder).encodeFrame(frame(4), outputStream);
        inOrder.verify(encoder).encodeFrame(SnowDataFrame.last, outputStream);
    }

    @Test
    void whenEmptyFrameAddedToBuffer_thenEmptyFrameIsNotStreamedToOutput() throws IOException, InterruptedException {
        when(phpSnow.isAlive()).thenReturn(true);
        when(buffer.firstFrame()).thenReturn(frame(1));
        when(buffer.nextFrame(frame(1))).thenReturn(frame(2));
        when(buffer.nextFrame(frame(2))).thenReturn(SnowDataFrame.empty);
        when(buffer.nextFrame(SnowDataFrame.empty)).thenReturn(frame(3));
        when(buffer.nextFrame(frame(3))).thenReturn(SnowDataFrame.last);

        snowStream.startConsumingSnowData();
        snowStream.streamTo(outputStream);

        InOrder inOrder = inOrder(encoder);

        verify(encoder, never()).encodeFrame(SnowDataFrame.empty, outputStream);

        inOrder.verify(encoder).encodeFrame(frame(1), outputStream);
        inOrder.verify(encoder).encodeFrame(frame(2), outputStream);
        inOrder.verify(encoder).encodeFrame(frame(3), outputStream);
        inOrder.verify(encoder).encodeFrame(SnowDataFrame.last, outputStream);
    }

    @Test
    void whenAllInputDataAreStreamed_thenConsumerThreadIsFinished() throws IOException, InterruptedException {
        when(phpSnow.isAlive()).thenReturn(true);

        snowStream.startConsumingSnowData();
        snowStream.waitUntilConsumerThreadFinished();

        Assertions.assertFalse(snowStream.isActive());
    }

    @Test
    void whenPhpAppIsDown_thenConsumerThreadIsFinished() throws IOException, InterruptedException {
        final Iterator<Boolean> isAliveFlags = List.of(true, true, false, false, false).iterator();
        when(phpSnow.isAlive()).then(i -> isAliveFlags.next());

        snowStream.startConsumingSnowData();
        snowStream.waitUntilConsumerThreadFinished();

        verify(buffer, times(2)).push(any());

        Assertions.assertFalse(snowStream.isActive());
    }


    @Test
    void whenConsumerThreadThrowingException_thenExceptionIsPassedToStreamingClients() throws IOException, InterruptedException {
        doNothing().when(buffer).push(any(SnowDataFrame.class));
        doThrow(InterruptedException.class).when(buffer).push(frame(3));

        when(phpSnow.isAlive()).thenReturn(true);

        snowStream.startConsumingSnowData();
        snowStream.waitUntilConsumerThreadFinished();
        assertThrows(IOException.class, () -> snowStream.streamTo(outputStream));

        InOrder inOrder = inOrder(buffer);
        inOrder.verify(buffer).push(frame(1));
        inOrder.verify(buffer).push(frame(2));
        inOrder.verify(buffer).push(frame(3));
        inOrder.verify(buffer).destroy();
        verify(buffer, never()).push(frame(4));
    }

}