package techbit.snow.proxy.snow.stream;

import com.google.common.collect.Maps;
import techbit.snow.proxy.dto.SnowBasis;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.util.Map;

import static techbit.snow.proxy.lang.EmptyArray.NO;

public class TestingFrames {

    private static final Map<Integer, SnowBasis> basisInstances = Maps.newConcurrentMap();

    public static SnowDataFrame frame(int frameNum) {
        return new SnowDataFrame(frameNum, 0, NO.FLOATS, NO.FLOATS, NO.BYTES);
    }

    public static SnowDataFrame frameWithBasis(int frameNum, int basisId) {
        return new SnowDataFrame(frameNum, 0, NO.FLOATS, NO.FLOATS, NO.BYTES, basis(basisId));
    }

    public static SnowBasis basis(int instanceId) {
        return basisInstances.computeIfAbsent(instanceId, k -> new SnowBasis(
            1, NO.INTEGERS, NO.INTEGERS, NO.BYTES
        ));
    }

    public static SnowDataFrame frameWithNoBasis(int frameNum) {
        return frame(frameNum);
    }

}
