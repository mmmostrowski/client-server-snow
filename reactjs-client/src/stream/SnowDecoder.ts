import SnowDataFrame from '../dto/SnowDataFrame';

export default class SnowDecoder {


    public decodeDataFrame(data : DataView): SnowDataFrame {
        let frameNum = data.getInt32(0, false);
        let chunkSize = data.getUint32(4, false);
        let x = new Float32Array(chunkSize);
        let y = new Float32Array(chunkSize);
        let flakes = new Uint8Array(chunkSize);
        let ptr = 8;
        console.log("chunkSize", chunkSize);
        for (var i = 0; i < chunkSize; ++i) {
            x[i] = data.getFloat32(ptr, false);
            y[i] = data.getFloat32(ptr + 4, false);
            flakes[i] = data.getUint8(ptr + 8);
            ptr += 9;
        }
        console.log("frameNum", frameNum);

        return {
            frameNum: frameNum,
            chunkSize: chunkSize,
            particlesX: x,
            particlesY: y,
            flakeShapes: flakes,
        };
    }


}
