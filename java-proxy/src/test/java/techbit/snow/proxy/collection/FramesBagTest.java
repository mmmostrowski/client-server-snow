package techbit.snow.proxy.collection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import techbit.snow.proxy.model.serializable.SnowDataFrame;

@ExtendWith(MockitoExtension.class)
class FramesBagTest {

    @Mock private BlockingBag<Integer, SnowDataFrame> bag;

    @InjectMocks
    private FramesBag framesBag;

    @Test
    void whenPutFrame_needsToBeStoredInBag() {
        framesBag.putFrame(SnowDataFrame.empty);

        Mockito.verify(bag).put(SnowDataFrame.empty.frameNum, SnowDataFrame.empty);
    }

    @Test
    void whenTakeFrame_needsToTakeItFromBag() throws InterruptedException {
        framesBag.takeFrame(12);

        Mockito.verify(bag).take(12);
    }

    @Test
    void whenRemoveFrame_needsToRemoveItFromBag() {
        framesBag.removeFrame(4);

        Mockito.verify(bag).remove(4);
    }
}