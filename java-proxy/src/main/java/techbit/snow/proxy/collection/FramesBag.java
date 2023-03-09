package techbit.snow.proxy.collection;

import techbit.snow.proxy.model.serializable.SnowDataFrame;

public class FramesBag {

    public static FramesBag create() {
        return new FramesBag(new BlockingBag<>());
    }

    private final BlockingBag<Integer, SnowDataFrame> bag;

    public FramesBag(BlockingBag<Integer, SnowDataFrame> bag) {
        this.bag = bag;
    }

    public void putFrame(SnowDataFrame frame) {
        bag.put(frame.frameNum, frame);
    }

    public SnowDataFrame takeFrame(int frameNum) throws InterruptedException {
        return bag.take(frameNum);
    }

    public void removeFrame(int frameNum) {
        bag.remove(frameNum);
    }

    public void removeAllFrames() {
        bag.removeAll();
    }
}
