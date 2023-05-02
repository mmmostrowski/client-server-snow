import * as React from "react";
import {useRef} from "react";
import {SnowCanvasRefHandler} from "../SnowCanvas";
import {SnowDrawing} from "./SnowDrawing";

const animationConstraints = {
    flakeShapes: [
        '#', // pressed
        '*', '*', '*', '*', '*', "'", ".", ",", "`"
    ],

    snowFont: {
        color: "white",
        scale: 1.3,
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

interface SnowAnimationProps {
    sessionIdx: number;
    onFinish: () => void;
    onBuffering: (percent: number) => void;
    onPlaying: (progress: number) => void;
}

export default function SnowAnimation({ sessionIdx, onFinish }: SnowAnimationProps): JSX.Element {
    const canvasRef = useRef<SnowCanvasRefHandler>(null);

    return <SnowDrawing sessionIdx={sessionIdx} />;
}


//     const stateRef = useRef<"stopped" | "buffering" | "playing" | "goodbye">("stopped");
//     const stompClientRef = useRef<SnowClientHandler>(null);
//     const needsToGoodbyeRef = useRef<boolean>(false);
//     const canvasRef = useRef<SnowDrawingRefHandler>(null);
//
//     return {
//         startProcessing: useCallback((startedSession: StartEndpointResponse): Promise<void> => {
//             const buffer = new Array<[SnowDataFrame, SnowBasis]>();
//             const decoder = new StreamDecoder();
//
//             let firstFrame = true;
//             let metadata: SnowAnimationMetadata;
//             let basis: SnowBasis;
//             let background: SnowBackground;
//             let lastTimestamp: number;
//             let fpsInterval: number;
//
//             return new Promise((accept) => {
//                 startStream(startedSession, (data: DataView) => {
//                     accept();
//                     onServerData(data);
//                 });
//             });
//
//             function onServerData(data: DataView): void {
//                 if (firstFrame) {
//                     firstFrame = false;
//                     [metadata, background] = decoder.decodeHeader(data);
//                     stateRef.current = "buffering";
//                     setSessionStatus("buffering", {
//                         bufferLevel: 0,
//                         animationProgress: 0,
//                     });
//                     return;
//                 }
//
//                 const frame = decoder.decodeFrame(data);
// //                 console.log(data);
//                 if (frame[0].isLast) {
//                     needsToGoodbyeRef.current = true;
//                 }
//                 buffer.push(frame);
//
//                 if (stateRef.current === "buffering") {
//                     setSessionStatus("buffering", {
//                         bufferLevel: bufferLevel(),
//                     });
//
//                     if (isBufferFull()) {
//                         stateRef.current = "playing";
//                         startAnimation();
//                     }
//                 }
//             }
//
//             function startAnimation(): void {
//                 lastTimestamp = Date.now();
//                 fpsInterval = 1000 / metadata.fps;
//                 animate();
//             }
//
//             function animate(): void {
//                 if (stateRef.current === "stopped") {
//                     animationStep();
//                     return;
//                 }
//
//                 requestAnimationFrame(animate);
//                 const now = Date.now();
//                 const elapsed = now - lastTimestamp;
//                 if (elapsed > fpsInterval) {
//                     lastTimestamp = now - (elapsed % fpsInterval);
//                     animationStep();
//                 }
//             }
//
//             function animationStep() {
//                 const canvas = canvasRef.current;
//                 if (!canvas) {
//                     return;
//                 }
//                 if (stateRef.current === "stopped") {
//                     canvas.clear();
//                     return;
//                 }
//                 if (stateRef.current === "goodbye") {
//                     canvas.drawGoodbye();
//                     return;
//                 }
//
//                 if (buffer.length === 0) {
//                     return;
//                 }
//                 const [frame, frameBasis] = buffer.shift();
//
//                 if (frame.isLast) {
//                     stateRef.current = "goodbye"
//                     stopStream();
//                     setTimeout(() => {
//                         if (onFinish && stateRef.current !== "stopped") {
//                             onFinish();
//                         }
//                         stateRef.current = "stopped";
//                         firstFrame = false;
//                     }, animationConstraints.goodbyeText.timeoutSec * 1000);
//                 } else {
//                     if (!frameBasis.isNone || !basis) {
//                         basis = frameBasis;
//                     }
//                     animationDraw(frame);
//                 }
//
//
//                 setSessionStatus("playing", {
//                     animationProgress: frame.frameNum * 100 / metadata.totalNumberOfFrames,
//                     bufferLevel: bufferLevel(),
//                 });
//             }
//
//             function animationDraw(frame: SnowDataFrame) {
//                 const canvas = canvasRef.current;
//                 if (!canvas) {
//                     return;
//                 }
//
//                 canvas.clear();
//                 canvas.drawBackground(background);
//                 canvas.drawSnow(frame);
//                 canvas.drawBasis(basis);
//             }
//
//
//             function isBufferFull(): boolean {
//                 return buffer.length >= metadata.bufferSizeInFrames;
//             }
//
//             function bufferLevel(): number {
//                 return Math.min(100, Math.round(buffer.length * 100 / metadata.bufferSizeInFrames));
//             }
//
//         }, [canvasRef, setSessionStatus]),
//
//         stopProcessing: useCallback(({allowForGoodbye}: { allowForGoodbye: boolean }) => {
//             if (stateRef.current === "stopped") {
//                 return;
//             }
//
//             if (allowForGoodbye) {
//                 if (needsToGoodbyeRef.current || stateRef.current === "goodbye") {
//                     return;
//                 }
//             }
//
//             stopStream();
//
//             stateRef.current = "stopped";
//             return Promise.resolve();
//         }, []),
//     };
//
//     function startStream(session: StartEndpointResponse, onStart: (data: DataView) => void) {
//         if (stompClientRef.current) {
//             throw Error("Please stopStream() first!");
//         }
//         stompClientRef.current = startSnowDataStream(session, onStart);
//     }
//
//     function stopStream() {
//         if (stompClientRef.current) {
//             stopSnowDataStream(stompClientRef.current);
//             stompClientRef.current = null;
//         }
//     }