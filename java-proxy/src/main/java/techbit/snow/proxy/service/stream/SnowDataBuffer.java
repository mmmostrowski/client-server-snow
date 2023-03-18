package techbit.snow.proxy.service.stream;

import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.util.Objects;
import java.util.Set;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class SnowDataBuffer {

    private final BlockingBag<Integer, SnowDataFrame> frames;
    private final Set<Object> clients = Sets.newHashSet();
    private final Object noMoreClientsLock = new Object();
    private final Object framesLock = new Object();
    private final Object removeFramesLock = new Object();
    private final int maxNumOfFrames;
    private volatile int lastValidFrameNum = Integer.MAX_VALUE;
    private volatile boolean destroyed = false;
    private volatile int numOfFrames = 0;
    private volatile int tailFrameNum = 0;
    private volatile int headFrameNum = 0;


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

        if (lastValidFrameNum != Integer.MAX_VALUE) {
            throw new IllegalArgumentException("You cannot push more frames to snow buffer after last frame is pushed.");
        }

        if (frame == SnowDataFrame.last) {
            lastValidFrameNum = headFrameNum;
        } else if (frame.frameNum() != headFrameNum + 1) {
            throw new IllegalStateException("Expected sequenced frames");
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
        return nextFrameAfter(frame.frameNum());
    }

    private SnowDataFrame nextFrameAfter(int frameNum) throws InterruptedException {
        if (destroyed || frameNum >= lastValidFrameNum) {
            return SnowDataFrame.last;
        }
        waitForContent();

        synchronized(framesLock) {
            if (frameNum + 1 < tailFrameNum) {
                return frames.take(tailFrameNum).orElseThrow();
            }
        }

        SnowDataFrame result;
        synchronized (removeFramesLock) {
            result = frameNum + 1 < tailFrameNum
                ? frames.take(tailFrameNum).orElseThrow()
                : frames.take(frameNum + 1).orElseThrow();
        }
        return result;
    }

    private void waitForContent() throws InterruptedException {
        if (numOfFrames == 0) {
            synchronized (framesLock) {
                framesLock.wait();
            }
        }
    }

    public void destroy() {
        destroyed = true;
        numOfFrames = 0;
        tailFrameNum = 0;

        synchronized (framesLock) {
            frames.removeAll();
            headFrameNum = 0;
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
                throw new IllegalArgumentException("Cannot unregister unknown client! Got: " + client);
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
}
