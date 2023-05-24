
export default interface SnowBackground {
    width: number,
    height: number,
    pixels: Uint8Array[],
}

export const NoSnowBackground: SnowBackground = {
    width: 0,
    height: 0,
    pixels: [],
}