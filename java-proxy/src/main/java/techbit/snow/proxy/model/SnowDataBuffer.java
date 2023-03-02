package techbit.snow.proxy.model;

import lombok.RequiredArgsConstructor;
import techbit.snow.proxy.collection.FramesBag;

@RequiredArgsConstructor(staticName = "ofSize")
public class SnowDataBuffer {

    private final int maxNumOfFrames;

    private int numOfFrames = 0;

    private int headFrameNum = 0;

    private int tailFrameNum = -1;

    private final FramesBag frames = FramesBag.create();

    public void push(SnowDataFrame frame) throws InterruptedException {
        synchronized(this) {
            if (numOfFrames < maxNumOfFrames) {
                ++numOfFrames;
                tailFrameNum = 0;
            } else {
                frames.removeFrame(++tailFrameNum);
            }
            if (++headFrameNum != frame.frameNum()) {
                throw new IllegalStateException("Expected sequenced frames");
            }
        }
        frames.putFrame(frame);
    }

    public SnowDataFrame nextFrame(SnowDataFrame frame) throws InterruptedException {
        return frames.takeFrame(Math.max(frame.frameNum(), tailFrameNum) + 1);
    }
}
