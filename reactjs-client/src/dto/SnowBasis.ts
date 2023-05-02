
export default interface SnowBasis {
    isNone: boolean,
    numOfPixels: number,
    x: Uint32Array,
    y: Uint32Array,
    pixels: Uint8Array,
}

export const NoSnowBasis: SnowBasis = {
    isNone: true,
    numOfPixels: 0,
    x: new Uint32Array(0),
    y: new Uint32Array(0),
    pixels: new Uint8Array(0),
}
