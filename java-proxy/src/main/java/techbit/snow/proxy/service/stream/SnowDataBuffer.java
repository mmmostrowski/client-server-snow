package techbit.snow.proxy.service.stream;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import techbit.snow.proxy.dto.SnowDataFrame;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Service
@Scope(SCOPE_PROTOTYPE)
public class SnowDataBuffer {

    public static SnowDataBuffer ofSize(int maxNumOfFrames) {
        return new SnowDataBuffer(maxNumOfFrames);
    }

    private final BlockingBag<Integer, SnowDataFrame> frames =  new BlockingBag<>();

    private final int maxNumOfFrames;

    private boolean destroyed = false;

    private int numOfFrames = 0;

    private int headFrameNum = 0;

    volatile private int tailFrameNum = 0;

    private SnowDataBuffer(int maxNumOfFrames) {
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
                tailFrameNum = frame.getFrameNum();
            } else if (numOfFrames < maxNumOfFrames) {
                ++numOfFrames;
            } else {
                frames.remove(tailFrameNum++);
            }
            if (!frame.isLast() && ++headFrameNum != frame.getFrameNum()) {
                throw new IllegalStateException("Expected sequenced frames");
            }
            if (numOfFrames == 1) {
                notifyAll();
            }
        }
        frames.put(frame.getFrameNum(), frame);
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
        SnowDataFrame nextFrame = frames.take(Math.max(frame.getFrameNum(), tailFrameNum) + 1);
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
