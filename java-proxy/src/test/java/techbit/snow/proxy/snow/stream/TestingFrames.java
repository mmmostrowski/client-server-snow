package techbit.snow.proxy.snow.stream;

import com.google.common.collect.Maps;
import techbit.snow.proxy.dto.SnowBasis;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.util.Map;

import static techbit.snow.proxy.lang.Array.*;

public class TestingFrames {

    private static final Map<Integer, SnowBasis> basisInstances = Maps.newConcurrentMap();

    public static SnowDataFrame frame(int frameNum) {
        return new SnowDataFrame(frameNum, 0, NO_FLOATS, NO_FLOATS, NO_BYTES);
    }

    public static SnowDataFrame frameWithBasis(int frameNum, int basisId) {
        return new SnowDataFrame(frameNum, 0, NO_FLOATS, NO_FLOATS, NO_BYTES, basis(basisId));
    }

    public static SnowBasis basis(int instanceId) {
        return basisInstances.computeIfAbsent(instanceId, k -> new SnowBasis(
            1, NO_INTS, NO_INTS, NO_BYTES
        ));
    }

    public static SnowDataFrame frameWithNoBasis(int frameNum) {
        return frame(frameNum);
    }

}
