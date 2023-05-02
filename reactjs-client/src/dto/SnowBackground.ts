
export default interface SnowBackground {
    isNone: boolean,
    width: number,
    height: number,
    pixels: Uint8Array[],
}

export const NoSnowBackground: SnowBackground = {
    isNone: true,
    width: 0,
    height: 0,
    pixels: [],
}