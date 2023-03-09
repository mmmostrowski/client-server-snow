package techbit.snow.proxy.model;

import techbit.snow.proxy.collection.FramesBag;
import techbit.snow.proxy.model.serializable.SnowDataFrame;

public class SnowDataBuffer {

    private boolean destroyed = false;

    public static SnowDataBuffer ofSize(int maxNumOfFrames) {
        return new SnowDataBuffer(maxNumOfFrames);
    }

    private final int maxNumOfFrames;

    private final FramesBag frames;

    private int numOfFrames = 0;

    private int headFrameNum = 0;

    volatile private int tailFrameNum = 0;

    private SnowDataBuffer(int maxNumOfFrames) {
        this.frames = FramesBag.create();
        this.maxNumOfFrames = maxNumOfFrames;
    }

    public void push(SnowDataFrame frame) throws InterruptedException {
        synchronized(this) {
            if (numOfFrames == 0) {
                numOfFrames = 1;
                tailFrameNum = frame.frameNum;
            } else if (numOfFrames < maxNumOfFrames) {
                ++numOfFrames;
            } else {
                frames.removeFrame(tailFrameNum++);
            }
            if (!frame.isLast() && ++headFrameNum != frame.frameNum) {
                throw new IllegalStateException("Expected sequenced frames");
            }
            if (numOfFrames == 1) {
                notifyAll();
            }
        }
        frames.putFrame(frame);
    }

    public SnowDataFrame firstFrame() throws InterruptedException {
        waitUntilFrameAvailable();
        if (destroyed) {
            return SnowDataFrame.last;
        }
        SnowDataFrame frame = frames.takeFrame(tailFrameNum);
        if (frame == null) {
            return SnowDataFrame.last;
        }
        return frame;
    }

    public SnowDataFrame nextFrame(SnowDataFrame frame) throws InterruptedException {
        waitUntilFrameAvailable();
        if (destroyed) {
            return SnowDataFrame.last;
        }
        SnowDataFrame nextFrame = frames.takeFrame(Math.max(frame.frameNum, tailFrameNum) + 1);
        if (nextFrame == null) {
            return SnowDataFrame.last;
        }
        return nextFrame;
    }

    private void waitUntilFrameAvailable() throws InterruptedException {
        if (destroyed) {
            return;
        }
        synchronized(this) {
            if (numOfFrames < 1) {
                wait();
            }
        }
    }

    public void destroy() {
        destroyed = true;
        frames.removeAllFrames();
        notifyAll();
    }
}
