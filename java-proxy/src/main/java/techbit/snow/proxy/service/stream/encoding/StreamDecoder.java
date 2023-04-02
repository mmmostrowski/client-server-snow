package techbit.snow.proxy.service.stream.encoding;

import techbit.snow.proxy.dto.SnowAnimationBackground;
import techbit.snow.proxy.dto.SnowAnimationBasis;
import techbit.snow.proxy.dto.SnowAnimationMetadata;
import techbit.snow.proxy.dto.SnowDataFrame;

import java.io.DataInputStream;
import java.io.IOException;

public interface StreamDecoder {

    SnowAnimationMetadata decodeMetadata(DataInputStream dataStream) throws IOException;

    SnowDataFrame decodeFrame(DataInputStream dataStream) throws IOException;

    SnowAnimationBackground decodeBackground(DataInputStream dataInputStream) throws IOException;

    SnowAnimationBasis decodeBasis(DataInputStream dataStream) throws IOException;
}
