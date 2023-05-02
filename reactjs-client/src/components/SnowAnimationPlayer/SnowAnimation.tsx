import * as React from "react";
import { useEffect, useRef } from "react";
import { SnowDrawing, SnowDrawingRefHandler } from "./SnowDrawing";
import {
    SnowAnimationConfiguration, SnowClientHandler,
    StartEndpointResponse,
    startSnowDataStream, startSnowSession,
    stopSnowDataStream, stopSnowSession
} from "../../stream/snowEndpoint";
import SnowBasis, {NoSnowBasis} from "../../dto/SnowBasis";
import SnowDataFrame from "../../dto/SnowDataFrame";
import SnowAnimationMetadata from "../../dto/SnowAnimationMetadata";
import SnowBackground, {NoSnowBackground} from "../../dto/SnowBackground";
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

    private onFinish: () => void;
    private onBuffering: (percent: number) => void;
    private onPlaying: (progress: number, bufferPercent: number) => void;
    private onError: (error: Error) => void;

    private readonly sessionId: string;

    private canvas: SnowDrawingRefHandler;
    private state: State = "stopped";
    private firstFrame: boolean = true;
    private metadata: SnowAnimationMetadata;
    private basis: SnowBasis = NoSnowBasis;
    private background: SnowBackground = NoSnowBackground;
    private lastTimestamp: number;
    private fpsInterval: number;
    private buffer: [SnowDataFrame, SnowBasis][];
    private decoder: SnowDecoder = new SnowDecoder();
    private needsGoodbye: boolean = false;
    private snowHandler: SnowClientHandler;

    public constructor(sessionId: string) {
        this.sessionId = sessionId;
        this.reset();
    }

    public configure(
        onBuffering: (percent: number) => void,
        onPlaying: (progress: number, bufferPercent: number) => void,
        onFinish: () => void,
        onError: (error: Error) => void,
        canvas: SnowDrawingRefHandler
    ): void {
        this.onFinish = onFinish;
        this.onBuffering = onBuffering;
        this.onPlaying = onPlaying;
        this.onError = onError;
        this.canvas = canvas;
    }

    public async startProcessing(configuration: SnowAnimationConfiguration, controller: AbortController) {
        try {
            if (this.state != 'stopped') {
                return;
            }
            this.startStream(
                await startSnowSession(this.sessionId, configuration, controller),
                this.onServerData.bind(this)
            );
            this.state = "buffering";
        } catch (error) {
            this.onError(error);
        }
    }

    public async stopProcessing(controller: AbortController) {
        try {
            if (this.state == 'stopped') {
                return;
            }
            this.state = "stopped";
            this.stopStream();
            await stopSnowSession(this.sessionId, controller);
        } catch (error) {
            this.onError(error);
        } finally {
            this.reset();
        }
    }

    private reset(): void {
        this.state = "stopped";
        this.basis = NoSnowBasis;
        this.background = NoSnowBackground;
        this.needsGoodbye = false;
        this.firstFrame = true;
        this.buffer = new Array<[SnowDataFrame, SnowBasis]>();
    }

    private onServerData(data: DataView): void {
        if (this.state === "stopped") {
            return;
        }

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
            this.canvas.clear();
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

    private animationStep(): void {
        if (this.state === "goodbye") {
            this.canvas.drawGoodbye();
            return;
        }

        if (this.buffer.length === 0) {
            return;
        }
        const [ frame, frameBasis] = this.buffer.shift();

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

    private animationDraw(frame: SnowDataFrame): void {
        this.canvas.clear();
        this.canvas.drawBackground(this.background);
        this.canvas.drawSnow(frame);
        this.canvas.drawBasis(this.basis);
    }

    private startStream(session: StartEndpointResponse, onData: (data: DataView) => void): void {
        if (this.snowHandler) {
            throw Error("Please stopStream() first!");
        }
        this.snowHandler = startSnowDataStream(session, onData);
    }

    private stopStream(): void {
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
    const canvasRef = useRef<SnowDrawingRefHandler>(null);
    const snowControllerRef = useRef<SnowAnimationController>(new SnowAnimationController(sessionId));


    useEffect(() => {
        const snowController = snowControllerRef.current;
        if (!snowController) {
            return;
        }

        snowController.configure(onBuffering, onPlaying, onFinish, onError, canvasRef.current);
    }, [ onBuffering, onPlaying, onFinish, onError, canvasRef ]);


    useEffect(() => {
        const snowController = snowControllerRef.current;
        if (!snowController) {
            return;
        }

        const abortController = new AbortController();
        if (play) {
            void snowController.startProcessing(configuration, abortController);
        } else {
            void snowController.stopProcessing(abortController);
        }
        return () => { abortController.abort() };
    }, [ play, sessionId, configuration ]);


    return <SnowDrawing sessionIdx={sessionIdx} ref={canvasRef} />;
}
