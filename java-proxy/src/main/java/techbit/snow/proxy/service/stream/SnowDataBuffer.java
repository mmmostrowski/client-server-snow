package techbit.snow.proxy.service.stream;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import techbit.snow.proxy.dto.SnowDataFrame;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class SnowDataBuffer {

    private final BlockingBag<Integer, SnowDataFrame> frames;

    private final int maxNumOfFrames;

    private volatile boolean destroyed = false;

    private int numOfFrames = 0;

    private int headFrameNum = 0;

    volatile private int tailFrameNum = 0;

    public SnowDataBuffer(int maxNumOfFrames) {
        this(maxNumOfFrames, new BlockingBag<>());
    }

    public SnowDataBuffer(int maxNumOfFrames, BlockingBag<Integer, SnowDataFrame> frames) {
        this.frames = frames;
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
                tailFrameNum = frame.frameNum();
            } else if (numOfFrames < maxNumOfFrames) {
                ++numOfFrames;
            } else {
                frames.remove(tailFrameNum++);
            }
            if (frame.isValidDataFrame() && ++headFrameNum != frame.frameNum()) {
                throw new IllegalStateException("Expected sequenced frames");
            }
            if (numOfFrames == 1) {
                notifyAll();
            }
        }
        frames.put(frame.frameNum(), frame);
    }

    public SnowDataFrame firstFrame() throws InterruptedException {
        waitForContent();
        if (destroyed) {
            return SnowDataFrame.last;
        }
        return frames.take(tailFrameNum)
                .orElse(SnowDataFrame.empty);
    }

    public SnowDataFrame nextFrame(SnowDataFrame frame) throws InterruptedException {
        waitForContent();
        if (destroyed) {
            return SnowDataFrame.last;
        }
        return frames.take(Math.max(frame.frameNum() + 1, tailFrameNum))
                .orElse(SnowDataFrame.empty);
    }

    private void waitForContent() throws InterruptedException {
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
        numOfFrames = 0;
        headFrameNum = 0;
        tailFrameNum = 0;
        synchronized (this) {
            notifyAll();
        }
    }
}
