package techbit.snow.proxy.service.stream.encoding;

import techbit.snow.proxy.dto.SnowBackground;
import techbit.snow.proxy.dto.SnowBasis;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.io.DataInputStream;
import java.io.IOException;

public interface StreamDecoder {

    SnowAnimationMetadata decodeMetadata(DataInputStream dataStream) throws IOException;

    SnowDataFrame decodeFrame(DataInputStream dataStream) throws IOException;

    SnowBackground decodeBackground(DataInputStream dataInputStream) throws IOException;

    SnowBasis decodeBasis(DataInputStream dataStream) throws IOException;

}
