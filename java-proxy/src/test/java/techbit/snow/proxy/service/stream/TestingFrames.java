package techbit.snow.proxy.service.stream;

import com.google.common.collect.Maps;
import techbit.snow.proxy.dto.SnowBasis;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.util.Map;

public interface TestingFrames {

    Map<Integer, SnowBasis> basisInstances = Maps.newConcurrentMap();

    static SnowDataFrame frame(int frameNum) {
        return new SnowDataFrame(frameNum, 0, null, null, null);
    }

    static SnowDataFrame frameWithBasis(int frameNum, int basisId) {
        return new SnowDataFrame(frameNum, 0, null, null, null, basis(basisId));
    }

    static SnowBasis basis(int instanceId) {
        return basisInstances.computeIfAbsent(instanceId, k -> new SnowBasis(
                0, null, null, null
        ));
    }

    static SnowDataFrame frameWithNoBasis(int frameNum) {
        return frame(frameNum);
    }

}
