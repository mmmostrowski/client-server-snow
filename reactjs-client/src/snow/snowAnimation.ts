import { useCallback, useRef, MutableRefObject } from "react";
import { SnowCanvasRefHandler } from  '../components/SnowCanvas';
import StreamDecoder from '../stream/SnowDecoder';
import SnowAnimationMetadata from '../dto/SnowAnimationMetadata';
import SnowBackground from '../dto/SnowBackground';
import SnowDataFrame from '../dto/SnowDataFrame';
import SnowBasis from '../dto/SnowBasis';
import { useSessionStatusUpdater } from '../snow/snowSessionStatus';
import {
    startSnowDataStream,
    stopSnowDataStream,
    StartEndpointResponse,
    SnowClientHandler,
} from '../stream/snowEndpoint';


const animationConstraints = {
    flakeShapes: [
        '#', // pressed
        '*', '*', '*', '*', '*', "'", ".", ",", "`"
    ],

    snowFont: {
        color: "white",
        scale: 1.5,
    },

    backgroundFont: {
        color: "lightblue",
        scale: 1.1,
    }
}


export function useSnowAnimation(sessionIdx: number, canvasRef: MutableRefObject<SnowCanvasRefHandler>) {
    const stateRef = useRef<"stopped"|"buffering"|"playing">("stopped");
    const stompClientRef = useRef<SnowClientHandler>(null);
    const setSessionStatus = useSessionStatusUpdater(sessionIdx);

    return {
        startProcessingSnowAnimation: useCallback((startedSession: StartEndpointResponse): Promise<void> => {
            const buffer = new Array<[SnowDataFrame, SnowBasis]>();
            const decoder = new StreamDecoder();

            let firstFrame = true;
            let metadata: SnowAnimationMetadata;
            let basis: SnowBasis;
            let background: SnowBackground;
            let lastTimestamp: number;
            let fpsInterval: number;

            return new Promise(( accept, reject ) => {
                stompClientRef.current = startSnowDataStream(startedSession, (data: DataView) => {
                    accept();
                    onServerData(data);
                });
            });

            function onServerData(data: DataView): void {
                if (firstFrame) {
                    firstFrame = false;
                    [ metadata, background ] = decoder.decodeHeader(data);
                    stateRef.current = "buffering";
                    setSessionStatus("buffering", {
                        bufferLevel: 0,
                        animationProgress: 0,
                    });
                    return;
                }

                buffer.push(decoder.decodeFrame(data));

                if (stateRef.current === "buffering") {
                    setSessionStatus("buffering", {
                        bufferLevel: bufferLevel(),
                    });

                    if (isBufferFull()) {
                        stateRef.current = "playing";
                        startAnimation();
                    }
                }
            }

            function isBufferFull(): boolean {
                return buffer.length >= metadata.bufferSizeInFrames;
            }

            function bufferLevel(): number {
                return Math.min(100, Math.round( buffer.length * 100 / metadata.bufferSizeInFrames ));
            }

            function startAnimation(): void {
                lastTimestamp = Date.now();
                fpsInterval = 1000 / metadata.fps;
                animate();
            }

            function animate(): void {
                if (stateRef.current !== "playing") {
                    canvasRef.current.clearBackground();
                    return;
                }
                requestAnimationFrame(animate);
                const now = Date.now();
                const elapsed = now - lastTimestamp;
                if (elapsed <= fpsInterval) {
                    return;
                }
                lastTimestamp = now - ( elapsed % fpsInterval );

                if (buffer.length === 0) {
                    return;
                }

                const [ frame, frameBasis ] = buffer.shift();
                if (!frameBasis.isNone || !basis) {
                    basis = frameBasis;
                }

                setSessionStatus("playing", {
                    animationProgress: frame.frameNum * 100 / metadata.totalNumberOfFrames,
                    bufferLevel: bufferLevel(),
                });

                drawFrame(frame);
                drawBasis(basis);
            }

            function drawFrame(frame: SnowDataFrame): void {
                const canvas = canvasRef.current;
                if (!canvas) {
                    return;
                }

                canvas.clearBackground();
                drawBackground(background);
                drawSnow(frame);
            }

            function drawBackground(background: SnowBackground): void {
                if (background.isNone) {
                    return;
                }

                const canvas = canvasRef.current;
                const font = animationConstraints.backgroundFont;
                const { width, height, pixels } = background;

                canvas.setCurrentFont(font.color, font.scale);
                for (let y = 0; y < height; ++y) {
                    for (let x = 0; x < width; ++x) {
                        const char = pixels[x][y];
                        if (char === 0) {
                            continue;
                        }
                        canvas.drawChar(x, y,
                            String.fromCharCode(char)
                        );
                    }
                }
            }

            function drawSnow(frame: SnowDataFrame): void {
                const { particlesX, particlesY, flakeShapes: flakes, chunkSize } = frame;
                const canvas = canvasRef.current;
                const font = animationConstraints.snowFont;
                const flakeShapes = animationConstraints.flakeShapes;

                canvas.setCurrentFont(font.color, font.scale);
                for (let i = 0; i < chunkSize; ++i) {
                    canvas.drawChar(
                        particlesX[i],
                        particlesY[i],
                        flakeShapes[flakes[i]]
                    );
                }
            }

            function drawBasis(basis: SnowBasis): void {
                if (basis.isNone) {
                    return;
                }

                const canvas = canvasRef.current;
                const flakeShapes = animationConstraints.flakeShapes;
                const { numOfPixels, x, y, pixels } = basis;

                for (let i = 0; i < numOfPixels; ++i) {
                    canvas.drawChar(
                        x[i],
                        y[i],
                        flakeShapes[pixels[i]]
                    );
                }
            }
        }, [ canvasRef, setSessionStatus ]),

        stopProcessingSnowAnimation: useCallback((response: StartEndpointResponse): Promise<void> => {
            if (stateRef.current === "stopped") {
                return;
            }
            stateRef.current = "stopped";
            stopSnowDataStream(stompClientRef.current);
            return Promise.resolve();
        }, []),
    };
}

