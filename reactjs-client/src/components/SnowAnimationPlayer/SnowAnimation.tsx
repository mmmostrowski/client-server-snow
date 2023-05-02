import * as React from "react";
import {useEffect, useRef, useState} from "react";
import {SnowCanvasRefHandler} from "../SnowCanvas";
import {SnowDrawing, SnowDrawingRefHandler} from "./SnowDrawing";
import {
    SnowAnimationConfiguration, SnowClientHandler,
    StartEndpointResponse,
    startSnowDataStream, startSnowSession,
    stopSnowDataStream
} from "../../stream/snowEndpoint";
import SnowBasis from "../../dto/SnowBasis";
import SnowDataFrame from "../../dto/SnowDataFrame";
import SnowAnimationMetadata from "../../dto/SnowAnimationMetadata";
import SnowBackground from "../../dto/SnowBackground";
import SnowDecoder from "../../stream/SnowDecoder";
import {useSnowSession} from "../../snow/SnowSessionsProvider";

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
    play: boolean;
    configuration: SnowAnimationConfiguration;
    onFinish: () => void;
    onBuffering: (percent: number) => void;
    onPlaying: (progress: number, bufferPercent: number) => void;
    onError: (error: Error) => void;
}

type State = "stopped" | "buffering" | "playing" | "goodbye";

class SnowAnimationController {

    private readonly onFinish: () => void;
    private readonly onBuffering: (percent: number) => void;
    private readonly onPlaying: (progress: number, bufferPercent: number) => void;
    private readonly onError: (error: Error) => void;

    private canvas: SnowDrawingRefHandler;
    private state: State = "stopped";
    private firstFrame = true;
    private metadata: SnowAnimationMetadata;
    private basis: SnowBasis;
    private background: SnowBackground;
    private lastTimestamp: number;
    private fpsInterval: number;
    private buffer = new Array<[SnowDataFrame, SnowBasis]>();
    private decoder = new SnowDecoder();
    private needsGoodbye: boolean = false;
    private snowHandler: SnowClientHandler;

    constructor(
        onBuffering: (percent: number) => void,
        onPlaying: (progress: number, bufferPercent: number) => void,
        onFinish: () => void,
        onError: (error: Error) => void,
        canvas: SnowDrawingRefHandler
    ) {
        this.onFinish = onFinish;
        this.onBuffering = onBuffering;
        this.onPlaying = onPlaying;
        this.onError = onError;
        this.canvas = canvas;
    }

    public async startProcessing(sessionId: string, configuration: SnowAnimationConfiguration, controller?: AbortController) {
        try {
            const startedSession = await startSnowSession(sessionId, configuration, controller);
            await this.startStream(startedSession, this.onServerData.bind(this));
            this.state = "buffering";
        } catch (error) {
            this.onError(error);
        }
    }

    public async stopProcessing() {

    }

    private onServerData(data: DataView): void {
        if (this.firstFrame) {
            this.firstFrame = false;
            [ this.metadata, this.background ] = this.decoder.decodeHeader(data);
            this.state = "buffering";
            this.onBuffering(0);
            return;
        }

        const frame = this.decoder.decodeFrame(data);
        // console.log(frame);
        if (frame[0].isLast) {
            this.needsGoodbye = true;
        }
        this.buffer.push(frame);

        if (this.state === "buffering") {
            this.onBuffering(this.bufferLevel());
            if (this.isBufferFull()) {
                this.state = "playing";
                this.startAnimation();
            }
        }
    }

    private startAnimation(): void {
        this.lastTimestamp = Date.now();
        this.fpsInterval = 1000 / this.metadata.fps;
        this.animate();
    }

    private animate(): void {
        if (this.state === "stopped") {
            this.animationStep();
            return;
        }

        requestAnimationFrame(this.animate.bind(this));
        const now = Date.now();
        const elapsed = now - this.lastTimestamp;
        if (elapsed > this.fpsInterval) {
            this.lastTimestamp = now - (elapsed % this.fpsInterval);
            this.animationStep();
        }
    }

    private animationStep() {
        if (this.state === "stopped") {
            this.canvas.clear();
            return;
        }
        if (this.state === "goodbye") {
            this.canvas.drawGoodbye();
            return;
        }

        if (this.buffer.length === 0) {
            return;
        }
        const [frame, frameBasis] = this.buffer.shift();

        if (frame.isLast) {
            this.state = "goodbye";
            this.stopStream();
            setTimeout(() => {
                this.onFinish();
                this.state = "stopped";
                this.firstFrame = false;
            }, animationConstraints.goodbyeText.timeoutSec * 1000);
        } else {
            if (!frameBasis.isNone || !this.basis) {
                this.basis = frameBasis;
            }
            this.animationDraw(frame);
        }

        this.onPlaying(this.animationProgress(frame), this.bufferLevel());
    }

    private animationDraw(frame: SnowDataFrame) {
        this.canvas.clear();
        this.canvas.drawBackground(this.background);
        this.canvas.drawSnow(frame);
        this.canvas.drawBasis(this.basis);
    }

    private startStream(session: StartEndpointResponse, onStart: (data: DataView) => void) {
        if (this.snowHandler) {
            throw Error("Please stopStream() first!");
        }
        this.snowHandler = startSnowDataStream(session, onStart);
    }

    private stopStream() {
        if (!this.snowHandler) {
            return;
        }
        stopSnowDataStream(this.snowHandler);
        this.snowHandler = null;
    }

    private animationProgress(frame: SnowDataFrame): number {
        return frame.frameNum * 100 / this.metadata.totalNumberOfFrames;
    }

    private isBufferFull(): boolean {
        return this.buffer.length >= this.metadata.bufferSizeInFrames;
    }

    private bufferLevel(): number {
        return Math.min(100, Math.round(this.buffer.length * 100 / this.metadata.bufferSizeInFrames));
    }

}



export default function SnowAnimation(props: SnowAnimationProps): JSX.Element {
    const {
        sessionIdx,
        play,
        configuration,
        onBuffering, onPlaying, onFinish, onError
    } = props;
    const { sessionId} = useSnowSession(sessionIdx);
    const [ snowController, setController ] = useState<SnowAnimationController>(null);
    const canvasRef = useRef<SnowDrawingRefHandler>(null);


    useEffect(() => {
        let canvas: SnowDrawingRefHandler = canvasRef.current;
        setController(new SnowAnimationController(onBuffering, onPlaying, onFinish, onError, canvas));
    }, []);


    useEffect(() => {
        if (!snowController) {
            return;
        }

        if (play) {
            const abortController = new AbortController();
            snowController.startProcessing(sessionId, configuration, abortController);
            return () => { abortController.abort(); };
        } else {
            snowController.stopProcessing();
        }
    }, [ play, snowController ]);


    return <SnowDrawing sessionIdx={sessionIdx} ref={canvasRef} />;
}
