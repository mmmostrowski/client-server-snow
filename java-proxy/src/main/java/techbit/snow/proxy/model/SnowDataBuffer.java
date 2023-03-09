package techbit.snow.proxy.model;

import techbit.snow.proxy.collection.BlockingBag;
import techbit.snow.proxy.model.serializable.SnowDataFrame;

public class SnowDataBuffer {

    public static SnowDataBuffer ofSize(int maxNumOfFrames) {
        return new SnowDataBuffer(maxNumOfFrames);
    }

    private final BlockingBag<Integer, SnowDataFrame> frames;

    private boolean destroyed = false;

    private final int maxNumOfFrames;

    private int numOfFrames = 0;

    private int headFrameNum = 0;

    volatile private int tailFrameNum = 0;

    private SnowDataBuffer(int maxNumOfFrames) {
        this.frames = new BlockingBag<>();
        this.maxNumOfFrames = maxNumOfFrames;
        if (maxNumOfFrames < 1) {
            throw new IllegalArgumentException("Buffer must have a positive size!");
        }
    }

    public void push(SnowDataFrame frame) throws InterruptedException {
        if (destroyed) {
            throw new IllegalStateException("You cannot push to snow buffer because it has been destroyed!");
        }
        synchronized(this) {
            if (numOfFrames == 0) {
                numOfFrames = 1;
                tailFrameNum = frame.frameNum;
            } else if (numOfFrames < maxNumOfFrames) {
                ++numOfFrames;
            } else {
                frames.remove(tailFrameNum++);
            }
            if (!frame.isLast() && ++headFrameNum != frame.frameNum) {
                throw new IllegalStateException("Expected sequenced frames");
            }
            if (numOfFrames == 1) {
                notifyAll();
            }
        }
        frames.put(frame.frameNum, frame);
    }

    public SnowDataFrame firstFrame() throws InterruptedException {
        waitUntilFrameAvailable();
        if (destroyed) {
            return SnowDataFrame.last;
        }
        SnowDataFrame frame = frames.take(tailFrameNum);
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
        SnowDataFrame nextFrame = frames.take(Math.max(frame.frameNum, tailFrameNum) + 1);
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
        frames.removeAll();
        notifyAll();
    }
}
