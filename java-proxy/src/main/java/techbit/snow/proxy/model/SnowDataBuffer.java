package techbit.snow.proxy.model;

import techbit.snow.proxy.collection.FramesBag;
import techbit.snow.proxy.model.serializable.SnowDataFrame;

public class SnowDataBuffer {

    public static SnowDataBuffer ofSize(int maxNumOfFrames) {
        return new SnowDataBuffer(maxNumOfFrames);
    }

    private final int maxNumOfFrames;

    private final FramesBag frames;

    private int numOfFrames = 0;

    private int headFrameNum = 0;

    private int tailFrameNum = 0;

    private SnowDataBuffer(int maxNumOfFrames) {
        this.frames = FramesBag.create();
        this.maxNumOfFrames = maxNumOfFrames;
    }

    public void push(SnowDataFrame frame) throws InterruptedException {
        synchronized(this) {
            if (numOfFrames < maxNumOfFrames) {
                ++numOfFrames;
                tailFrameNum = 1;
            } else {
                frames.removeFrame(tailFrameNum++);
            }
            if (++headFrameNum != frame.frameNum) {
                throw new IllegalStateException("Expected sequenced frames");
            }
        }
        frames.putFrame(frame);
    }

    public SnowDataFrame firstFrame() throws InterruptedException {
        return frames.takeFrame(tailFrameNum);
    }

    public SnowDataFrame nextFrame(SnowDataFrame frame) throws InterruptedException {
        return frames.takeFrame(Math.max(frame.frameNum, tailFrameNum) + 1);
    }
}
