import SnowDataFrame from '../dto/SnowDataFrame';
import SnowAnimationMetadata from '../dto/SnowAnimationMetadata';
import SnowBackground, {NoSnowBackground} from '../dto/SnowBackground';
import SnowBasis, {NoSnowBasis} from '../dto/SnowBasis';

export default class SnowDecoder
{

    public decodeHeader(data : DataView): [ SnowAnimationMetadata, SnowBackground ] {
        return [
            this.decodeMetadata(data),
            this.decodeBackground(data)
        ];
    }

    public decodeFrame(data : DataView): [ SnowDataFrame, SnowBasis ] {
        const frame = this.decodeDataFrame(data);
        const basis = this.decodeBasis(data, frame.chunkSize);
        return [ frame, basis ];
    }

    private decodeMetadata(data : DataView): SnowAnimationMetadata {
        const width= data.getInt32(0, false);
        const height= data.getInt32(4, false);
        const fps= data.getInt32(8, false);
        const bufferSizeInFrames= data.getInt32(12, false);
        const totalNumberOfFrames= data.getInt32(16, false);

        return { width, height, fps, bufferSizeInFrames, totalNumberOfFrames }
    }

    private decodeBackground(data : DataView): SnowBackground {
        let ptr= 20; // after metadata

        const width= data.getInt32(ptr, false);
        ptr += 4;
        if (width === 0) {
            return NoSnowBackground;
        }

        const height= data.getInt32(ptr, false);
        ptr += 4;

        const pixels = [];
        for (let x = 0; x < width; ++x) {
            pixels[x] = new Uint8Array(height);
            for (let y = 0; y < height; ++y) {
                pixels[x][y] = data.getUint8(ptr++);
            }
        }

        return { width, height, pixels }
    }

    private decodeDataFrame(data : DataView): SnowDataFrame {
        let ptr = 0;
        const frameNum = data.getInt32(ptr, false);
        ptr += 4;

        const chunkSize = data.getUint32(ptr, false);
        ptr += 4;

        const isLast = frameNum === -1;
        const particlesX = new Float32Array(chunkSize);
        const particlesY = new Float32Array(chunkSize);
        const flakeShapes = new Uint8Array(chunkSize);

        for (let i = 0; i < chunkSize; ++i) {
            particlesX[i] = data.getFloat32(ptr, false);
            ptr += 4;

            particlesY[i] = data.getFloat32(ptr, false);
            ptr += 4;

            flakeShapes[i] = data.getUint8(ptr);
            ptr += 1;
        }

        return { isEndOfStream: isLast, frameNum, chunkSize, particlesX, particlesY, flakeShapes };
    }

    private decodeBasis(data : DataView, frameChunkSize: number): SnowBasis {
        let ptr= 8 + 9 * frameChunkSize; // after data frame

        const numOfPixels = data.getInt32(ptr, false);
        ptr += 4;
        if (numOfPixels === 0) {
            return NoSnowBasis;
        }

        const x = new Uint32Array(numOfPixels);
        const y = new Uint32Array(numOfPixels);
        const pixels = new Uint8Array(numOfPixels);

        for (let i = 0; i < numOfPixels; ++i) {
            x[i] = data.getInt32(ptr, false);
            ptr += 4;
        }
        for (let i = 0; i < numOfPixels; ++i) {
            y[i] = data.getInt32(ptr, false);
            ptr += 4;
        }
        for (let i = 0; i < numOfPixels; ++i) {
            pixels[i] = data.getUint8(ptr);
            ptr += 1;
        }

        return { numOfPixels, x, y, pixels };
    }
}
