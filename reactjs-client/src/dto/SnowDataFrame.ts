
export default interface SnowDataFrame {
    isEndOfStream: boolean,
    frameNum: number,
    chunkSize: number,
    particlesX: Float32Array,
    particlesY: Float32Array,
    flakeShapes: Uint8Array,
}
