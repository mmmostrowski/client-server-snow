package techbit.snow.proxy.collection;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import techbit.snow.proxy.model.serializable.SnowDataFrame;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@RequiredArgsConstructor(staticName = "create")
public class FramesBag {
    private final Map<Integer, BlockingQueue<SnowDataFrame>> map = Maps.newConcurrentMap();

    public void putFrame(SnowDataFrame frame) throws InterruptedException {
        ensureQueueExists(frame.frameNum).put(frame);
    }

    public SnowDataFrame takeFrame(int frameNum) throws InterruptedException {
        return ensureQueueExists(frameNum).take();
    }

    public void removeFrame(SnowDataFrame frame) {
        map.remove(frame.frameNum);
    }

    public void removeFrame(int frameNum) {
        map.remove(frameNum);
    }

    private BlockingQueue<SnowDataFrame> ensureQueueExists(int frameNum) {
        return map.computeIfAbsent(frameNum, k -> new ArrayBlockingQueue<>(1));
    }
}
