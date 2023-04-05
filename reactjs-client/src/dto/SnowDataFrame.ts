
export type SnowDataFrame = {
    frameNum: number,
    chunkSize: number,
    particlesX: Float32Array,
    particlesY: Float32Array,
    flakeShapes: Uint8Array,
}

export default SnowDataFrame;