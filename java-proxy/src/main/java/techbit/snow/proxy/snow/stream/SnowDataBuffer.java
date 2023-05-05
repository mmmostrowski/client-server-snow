package techbit.snow.proxy.snow.stream;

import com.google.common.collect.Sets;
import lombok.Generated;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Scope;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.util.Optional;
import java.util.Set;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Scope(SCOPE_PROTOTYPE)
public final class SnowDataBuffer {

    private volatile int lastValidFrameNum = Integer.MAX_VALUE;
    private final BlockingBag<Integer, SnowDataFrame> frames;
    private final Set<Object> clients = Sets.newHashSet();
    private final Object noMoreClientsLock = new Object();
    private final Object removeFramesLock = new Object();
    private final Object framesLock = new Object();
    private final int maxNumOfFrames;
    private volatile int numOfFrames;
    private volatile int tailFrameNum;
    private volatile int headFrameNum;
    private volatile boolean destroyed;


    public SnowDataBuffer(int maxNumOfFrames, BlockingBag<Integer, SnowDataFrame> frames) {
        this.frames = frames;
        this.maxNumOfFrames = maxNumOfFrames;
        if (maxNumOfFrames < 1) {
            throw new IllegalArgumentException("Buffer must have a positive size!");
        }
    }

    public void push(SnowDataFrame frame) {
        if (destroyed) {
            throw new IllegalStateException("You cannot push to snow buffer because it has been destroyed!");
        }

        if (lastValidFrameNum != Integer.MAX_VALUE) {
            throw new IllegalArgumentException("You cannot push more frames to snow buffer after last frame is pushed!");
        }

        if (frame == SnowDataFrame.LAST) {
            lastValidFrameNum = headFrameNum;
        } else if (frame.frameNum() != headFrameNum + 1) {
            throw new IllegalArgumentException("Expected frames in sequence!");
        }

        if (numOfFrames == 0) {
            numOfFrames = 1;
            tailFrameNum = 1;
            synchronized(framesLock) {
                frames.put(++headFrameNum, frame);
                framesLock.notifyAll();
            }
        } else if (numOfFrames < maxNumOfFrames) {
            synchronized(framesLock) {
                ++numOfFrames;
                frames.put(++headFrameNum, frame);
            }
        } else {
            synchronized(framesLock) {
                frames.put(++headFrameNum, frame);
                synchronized (removeFramesLock) {
                    frames.remove(tailFrameNum++);
                }
            }
        }
    }

    public SnowDataFrame firstFrame() throws InterruptedException {
        return nextFrameAfter(0);
    }

    public SnowDataFrame nextFrame(SnowDataFrame frame) throws InterruptedException {
        if (frame == SnowDataFrame.LAST) {
            return SnowDataFrame.LAST;
        }
        return nextFrameAfter(frame.frameNum());
    }

    @SuppressWarnings("RedundantThrows")
    private SnowDataFrame nextFrameAfter(int frame) throws InterruptedException {
        final int nextFrame = frame + 1;

        if (destroyed || nextFrame > lastValidFrameNum) {
            return SnowDataFrame.LAST;
        }

        return getFrame(nextFrame).orElseGet(() -> waitForFrame(nextFrame));
    }

    private Optional<SnowDataFrame> getFrame(int frame) {
        return isBehind(frame)
                ? frames.get(tailFrameNum)
                : frames.get(frame);
    }

    @Generated
    @SneakyThrows
    private SnowDataFrame waitForFrame(int frame) {
        waitForInitialFrame();
        synchronized (removeFramesLock) {
            return isBehind(frame)
                    ? frames.take(tailFrameNum)
                    : frames.take(frame);
        }
    }

    private void waitForInitialFrame() throws InterruptedException {
        synchronized (framesLock) {
            if (numOfFrames == 0) {
                framesLock.wait();
            }
        }
    }

    public void destroy() {
        destroyed = true;
        synchronized (framesLock) {
            numOfFrames = 0;
            tailFrameNum = 0;
            headFrameNum = 0;
            frames.removeAll();

            framesLock.notifyAll();
        }
    }

    public void registerClient(Object client) {
        synchronized (noMoreClientsLock) {
            clients.add(client);
        }
    }

    public void unregisterClient(Object client) {
        synchronized (noMoreClientsLock) {
            if (!clients.remove(client)) {
                throw new IllegalArgumentException("Unknown client. Cannot unregister! Got: " + client);
            }
            if (clients.isEmpty()) {
                noMoreClientsLock.notifyAll();
            }
        }
    }

    public void waitUntilAllClientsUnregister() throws InterruptedException {
        synchronized (noMoreClientsLock) {
            if (clients.isEmpty()) {
                return;
            }

            noMoreClientsLock.wait();
        }
    }

    boolean isBehind(int frameNum) {
        return frameNum < tailFrameNum;
    }

}
