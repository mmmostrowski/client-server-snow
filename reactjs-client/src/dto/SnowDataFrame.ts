
export default interface SnowDataFrame {
    isLast: boolean,
    frameNum: number,
    chunkSize: number,
    particlesX: Float32Array,
    particlesY: Float32Array,
    flakeShapes: Uint8Array,
}
