
export default interface SnowDataFrame {
    isLast: boolean,
    frameNum: number,
    chunkSize: number,
    particlesX: Float32Array,
    particlesY: Float32Array,
    flakeShapes: Uint8Array,
}

export const LastDataFrame: SnowDataFrame = {
    isLast: true,
    frameNum: -1,
    chunkSize: 0,
    particlesX: new Float32Array(0),
    particlesY: new Float32Array(0),
    flakeShapes: new Uint8Array(0),
}