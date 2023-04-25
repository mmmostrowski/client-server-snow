import { useCallback, useRef, MutableRefObject } from "react";
import { SnowCanvasRefHandler } from  '../components/SnowCanvas';
import StreamDecoder from '../stream/SnowDecoder';
import { SnowAnimationMetadata } from '../dto/SnowAnimationMetadata';
import { SnowBackground } from '../dto/SnowBackground';
import { SnowDataFrame } from '../dto/SnowDataFrame';
import { useSnowSession } from '../snow/SnowSessionsProvider';
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
    const session = useSnowSession(sessionIdx);
    const setSessionStatus = useSessionStatusUpdater(sessionIdx);


    return {
        startProcessingSnowAnimation: useCallback((startedSession: StartEndpointResponse): Promise<void> => {
            const buffer = new Array<SnowDataFrame>();
            const decoder = new StreamDecoder();

            let firstFrame = true;
            let metadata: SnowAnimationMetadata;
            let background: SnowBackground;
            let lastTimestamp: number;
            let fpsInterval: number;
            let fps: number;

            return new Promise(( accept, reject ) => {
                stompClientRef.current = startSnowDataStream(startedSession, (data: DataView) => {
                    accept();
                    onServerData(data);
                });
            });

            function onServerData(data: DataView): void {
                if (firstFrame) {
                    firstFrame = false;
                    metadata = decoder.decodeMetadata(data);
                    background = decoder.decodeBackground(data);
                    stateRef.current = "buffering";
                    setSessionStatus("buffering", {
                        bufferLevel: 0,
                        animationProgress: 0,
                    });
                    return;
                }

                const frame = decoder.decodeDataFrame(data);
                buffer.push(frame);

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
                fps = metadata.fps;
                fpsInterval = 1000 / fps;
                animate();
            }

            function animate(): void  {
                if (stateRef.current === "playing") {
                    requestAnimationFrame(animate);
                    setSessionStatus("playing", {
                        animationProgress: 0,
                        bufferLevel: bufferLevel(),
                    });
                }

                const now = Date.now();
                const elapsed = now - lastTimestamp;
                if (elapsed <= fpsInterval) {
                    return;
                }
                lastTimestamp = now - ( elapsed % fpsInterval );

                const frame = buffer.shift();
                if (!frame) {
                    return;
                }

                drawFrame(frame);
            }

            function drawFrame(frame: SnowDataFrame) {
              if (!canvasRef.current) {
                  return;
              }
              drawBackground(background);
              drawSnow(frame);
            }

            function drawBackground(background: SnowBackground) {
                const canvas = canvasRef.current;
                const font = animationConstraints.backgroundFont;

                canvas.clearBackground();
                canvas.setCurrentFont(font.color, font.scale);
                for (let y = 0; y < background.height; ++y) {
                    for (let x = 0; x < background.width; ++x) {
                        const char = background.pixels[x][y];
                        if (char === 0) {
                            continue;
                        }
                        canvas.drawChar(x, y,
                            String.fromCharCode(char)
                        );
                    }
                }
            }

            function drawSnow(frame: SnowDataFrame ) {
                const canvas = canvasRef.current;
                const font = animationConstraints.snowFont;
                const flakeShapes = animationConstraints.flakeShapes;
                const particlesX = frame.particlesX;
                const particlesY = frame.particlesY;
                const flakes = frame.flakeShapes;

                canvas.setCurrentFont(font.color, font.scale);
                for (let i = 0; i < frame.chunkSize; ++i) {
                    canvas.drawChar(
                        particlesX[i],
                        particlesY[i],
                        flakeShapes[flakes[i]]
                    );
                }
            }
        }, [ canvasRef ]),

        stopProcessingSnowAnimation: useCallback((response: StartEndpointResponse): Promise<void> => {
            stateRef.current = "stopped";
            stopSnowDataStream(stompClientRef.current);
            return Promise.resolve();
        }, []),
    };
}

