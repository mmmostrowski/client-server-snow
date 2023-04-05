package techbit.snow.proxy.snow.transcoding;

import techbit.snow.proxy.dto.*;

import java.io.DataInputStream;
import java.io.IOException;

public interface StreamDecoder {

    SnowAnimationMetadata decodeMetadata(DataInputStream dataStream, ServerMetadata serverMetadata) throws IOException;

    SnowBackground decodeBackground(DataInputStream dataInputStream) throws IOException;

    SnowDataFrame decodeFrame(DataInputStream dataStream) throws IOException;

    SnowBasis decodeBasis(DataInputStream dataStream) throws IOException;

}
