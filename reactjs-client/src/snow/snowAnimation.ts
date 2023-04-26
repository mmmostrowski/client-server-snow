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
    },

    goodbyeText: {
        text: "Thank you for watching",
        color: "lightblue",
        font: "bold Arial",
        timeoutSec: 2.5,
    },
}


export function useSnowAnimation(sessionIdx: number, canvasRef: MutableRefObject<SnowCanvasRefHandler>, onFinish:() => void ) {
    const stateRef = useRef<"stopped"|"buffering"|"playing"|"goodbye">("stopped");
    const stompClientRef = useRef<SnowClientHandler>(null);
    const setSessionStatus = useSessionStatusUpdater(sessionIdx);
    const needsToGoodbyeRef = useRef<boolean>(false);

    return {
        startProcessing: useCallback((startedSession: StartEndpointResponse): Promise<void> => {
            const buffer = new Array<[SnowDataFrame, SnowBasis]>();
            const decoder = new StreamDecoder();

            let firstFrame = true;
            let metadata: SnowAnimationMetadata;
            let basis: SnowBasis;
            let background: SnowBackground;
            let lastTimestamp: number;
            let fpsInterval: number;

            return new Promise(( accept, reject ) => {
                startStream(startedSession, (data: DataView) => {
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

                const frame = decoder.decodeFrame(data);
//                 console.log(data);
                if (frame[0].isLast) {
                    needsToGoodbyeRef.current = true;
                }
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

            function startAnimation(): void {
                lastTimestamp = Date.now();
                fpsInterval = 1000 / metadata.fps;
                animate();
            }

            function animate(): void {
                if (stateRef.current === "stopped") {
                    animationStep();
                    return;
                }

                requestAnimationFrame(animate);
                const now = Date.now();
                const elapsed = now - lastTimestamp;
                if (elapsed > fpsInterval) {
                    lastTimestamp = now - ( elapsed % fpsInterval );
                    animationStep();
                }
            }

            function animationStep() {
                const canvas = canvasRef.current;
                if (!canvas) {
                    return;
                }
                if (stateRef.current === "stopped") {
                    canvas.clearBackground();
                    return;
                }
                if (stateRef.current === "goodbye") {
                    canvas.clearBackground();
                    drawGoodbye();
                    return;
                }

                if (buffer.length === 0) {
                    return;
                }
                const [ frame, frameBasis ] = buffer.shift();

                if (frame.isLast) {
                    stateRef.current = "goodbye"
                    stopStream();
                    setTimeout(() => {
                        if (onFinish && stateRef.current !== "stopped") {
                            onFinish();
                        }
                        stateRef.current = "stopped";
                        firstFrame = false;
                    }, animationConstraints.goodbyeText.timeoutSec * 1000 );
                } else {
                    if (!frameBasis.isNone || !basis) {
                        basis = frameBasis;
                    }
                    animationDraw(frame);
                }

                setSessionStatus("playing", {
                    animationProgress: frame.frameNum * 100 / metadata.totalNumberOfFrames,
                    bufferLevel: bufferLevel(),
                });
            }

            function animationDraw(frame: SnowDataFrame) {
                const canvas = canvasRef.current;
                if (!canvas) {
                    return;
                }

                canvas.clearBackground();
                drawBackground(background);
                drawSnow(frame);
                drawBasis(basis);
            }

            function drawGoodbye() {
                const canvas = canvasRef.current;
                if (!canvas) {
                    return;
                }

                const size = Math.abs(metadata.height / 10);
                const { text, color, font } = animationConstraints.goodbyeText;
                canvas.setCurrentFont(color, size + 'px' + font);
                canvas.drawTextInCenter(text);
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

            function isBufferFull(): boolean {
                return buffer.length >= metadata.bufferSizeInFrames;
            }

            function bufferLevel(): number {
                return Math.min(100, Math.round( buffer.length * 100 / metadata.bufferSizeInFrames ));
            }

        }, [ canvasRef, setSessionStatus ]),

        stopProcessing: useCallback(({ allowForGoodbye }: { allowForGoodbye: boolean }) => {
            if (stateRef.current === "stopped") {
                return;
            }

            if (allowForGoodbye) {
                if (needsToGoodbyeRef.current || stateRef.current === "goodbye") {
                    return;
                }
            }

            stopStream();

            stateRef.current = "stopped";
            return Promise.resolve();
        }, []),
    };

    function startStream(session: StartEndpointResponse, onStart: (data: DataView) => void) {
        if (stompClientRef.current) {
            throw Error("Please stopStream() first!");
        }
        stompClientRef.current = startSnowDataStream(session, onStart);
    }

    function stopStream() {
        if (stompClientRef.current) {
            stopSnowDataStream(stompClientRef.current);
            stompClientRef.current = null;
        }
    }
}

