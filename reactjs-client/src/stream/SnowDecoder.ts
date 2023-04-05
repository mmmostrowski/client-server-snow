import SnowDataFrame from '../dto/SnowDataFrame';
import SnowAnimationMetadata from '../dto/SnowAnimationMetadata';
import SnowBackground from '../dto/SnowBackground';

export default class SnowDecoder {

    public decodeMetadata(data : DataView): SnowAnimationMetadata {
        let width=data.getInt32(0, false);
        let height=data.getInt32(4, false);
        let fps=data.getInt32(8, false);
        let bufferSizeInFrames=data.getInt32(12, false);

        return {
            width: width,
            height: height,
            fps: fps,
            bufferSizeInFrames: bufferSizeInFrames,
        }
    }

    public decodeBackground(data : DataView): SnowBackground {
        let ptr=16; // after metadata

        let width=data.getInt32(ptr, false);
        if (width <= 0) {
            return {
                width: 0,
                height: 0,
                pixels: [],
            };
        }
        ptr += 4;
        let height=data.getInt32(ptr, false);
        let pixels = [];
        ptr += 4;
        for (let x = 0; x < width; ++x) {
            pixels[x] = new Uint8Array(height);
            for (let y = 0; y < height; ++y) {
                pixels[x][y] = data.getUint8(ptr++);
            }
        }

        return {
            width: width,
            height: height,
            pixels: pixels,
        }
    }

    public decodeDataFrame(data : DataView): SnowDataFrame {
        let frameNum = data.getInt32(0, false);
        let chunkSize = data.getUint32(4, false);
        let x = new Float32Array(chunkSize);
        let y = new Float32Array(chunkSize);
        let flakes = new Uint8Array(chunkSize);

        let ptr = 8;
        for (var i = 0; i < chunkSize; ++i) {
            x[i] = data.getFloat32(ptr, false);
            y[i] = data.getFloat32(ptr + 4, false);
            flakes[i] = data.getUint8(ptr + 8);
            ptr += 9;
        }

        return {
            frameNum: frameNum,
            chunkSize: chunkSize,
            particlesX: x,
            particlesY: y,
            flakeShapes: flakes,
        };
    }


}
