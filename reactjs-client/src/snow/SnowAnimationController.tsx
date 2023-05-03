import {SnowDrawingRefHandler} from "../components/SnowAnimationPlayer/SnowDrawing";
import SnowDecoder from "../stream/SnowDecoder";
import SnowAnimationMetadata from "../dto/SnowAnimationMetadata";
import SnowBasis, {NoSnowBasis} from "../dto/SnowBasis";
import SnowBackground, {NoSnowBackground} from "../dto/SnowBackground";
import {
    AbortedEndpointResponse,
    DetailsEndpointResponse, fetchSnowDetails,
    SnowAnimationConfiguration,
    SnowClientHandler,
    StartEndpointResponse,
    startSnowDataStream,
    startSnowSession,
    stopSnowDataStream,
    stopSnowSession
} from "../stream/snowEndpoint";
import SnowDataFrame from "../dto/SnowDataFrame";

type BufferFrame = [ SnowDataFrame, SnowBasis ];

export type DetailsFromServer = DetailsEndpointResponse;

export class SnowAnimationController {
    private readonly sessionId: string;

    private onFinish: () => void;
    private onBuffering: (percent: number) => void;
    private onPlaying: (progress: number, bufferPercent: number) => void;
    private onError: (error: Error) => void;
    private onChecking: (sessionId: string) => void;
    private onFound: (response: DetailsFromServer) => void;
    private onNotFound: (response: DetailsFromServer) => void;

    private state: "stopped" | "buffering" | "playing" | "goodbye" = "stopped";
    private buffer: BufferFrame[];
    private metadata: SnowAnimationMetadata;
    private basis: SnowBasis = NoSnowBasis;
    private background: SnowBackground = NoSnowBackground;
    private canvas: SnowDrawingRefHandler;
    private decoder: SnowDecoder;
    private lastTimestamp: number;
    private firstFrame: boolean;
    private fpsInterval: number;
    private needsGoodbye: boolean;
    private goodbyeTextTimeoutSec: number;
    private streamHandler: SnowClientHandler;

    public constructor(sessionId: string, decoder: SnowDecoder = new SnowDecoder()) {
        this.sessionId = sessionId;
        this.decoder = decoder;
        this.configure({});
        this.reset();
    }

    public configure(config: {
        canvas?: SnowDrawingRefHandler,
        onBuffering?: (percent: number) => void,
        onPlaying?: (progress: number, bufferPercent: number) => void,
        onFinish?: () => void,
        onError?: (error: Error) => void,
        onChecking?: (sessionId: string) => void,
        onFound?: (response: DetailsFromServer) => void,
        onNotFound?: (response: DetailsFromServer) => void,
        goodbyeTextTimeoutSec?: number,
    }): void {
        if (config.canvas) {
            this.canvas = config.canvas;
        }

        const idle = () => {};
        this.onFound = config.onFound ?? idle;
        this.onNotFound = config.onNotFound ?? idle;
        this.onChecking = config.onChecking ?? idle;
        this.onBuffering = config.onBuffering ?? idle;
        this.onPlaying = config.onPlaying ?? idle;
        this.onFinish = config.onFinish ?? idle;
        this.onError = config.onError ?? idle;

        this.goodbyeTextTimeoutSec = config.goodbyeTextTimeoutSec ?? 2;
    }

    public async startProcessing(configuration: SnowAnimationConfiguration, controller: AbortController): Promise<void> {
        if (this.state !== 'stopped') {
            throw Error("Need to stopProcessing() first!");
        }
        try {
            this.state = "buffering";
            this.startStream(
                await startSnowSession(this.sessionId, configuration, controller),
                this.onServerData.bind(this)
            );
        } catch (error) {
            this.onError(error);
        }
    }

    public async stopProcessing(controller: AbortController): Promise<void> {
        if (this.state === 'stopped') {
            return;
        }
        if (this.state === 'goodbye') {
            this.state = "stopped";
            return;
        }
        try {
            this.state = "stopped";
            this.stopStream();
            await stopSnowSession(this.sessionId, controller);
        } catch (error) {
            this.onError(error);
        } finally {
            this.reset();
        }
    }

    public async checkDetails(abortController: AbortController): Promise<DetailsFromServer> {
        try {
            this.onChecking(this.sessionId);

            const response: DetailsEndpointResponse = await fetchSnowDetails(this.sessionId, abortController);
            if (response === AbortedEndpointResponse) {
                return;
            }
            if (response.running) {
                this.onFound(response);
            } else {
                this.onNotFound(response);
            }
        } catch (error) {
            this.onError(error);
        }
    }

    private reset(): void {
        this.firstFrame = true;
        this.lastTimestamp = null;
        this.needsGoodbye = false;
        this.basis = NoSnowBasis;
        this.background = NoSnowBackground;
        this.buffer = new Array<[SnowDataFrame, SnowBasis]>();
    }

    private onServerData(data: DataView): void {
        if (this.state === "stopped" || this.state === 'goodbye') {
            return;
        }

        this.notifyBuffering();

        if (this.firstFrame) {
            this.firstFrame = false;
            [this.metadata, this.background] = this.decoder.decodeHeader(data);
            return;
        }

        const frame: BufferFrame = this.decoder.decodeFrame(data);
        const [dataFrame] = frame;
        // console.log(frame);
        if (dataFrame.isLast) {
            this.needsGoodbye = true;
        }

        this.buffer.push(frame);
        if (this.isBufferFullyLoaded()) {
            this.startAnimation();
        }
    }

    private startAnimation(): void {
        this.state = "playing";
        this.animationLoop();
    }

    private animationLoop(): void {
        if (this.state !== "stopped") {
            requestAnimationFrame(this.animationLoop.bind(this));
        }
        this.synchronizeFps(() => {
            this.animationStep();
        });
    }

    private animationStep(): void {
        if (this.state === "stopped") {
            this.canvas.clear();
            return;
        }

        if (this.state === "goodbye") {
            this.canvas.drawGoodbye();
            return;
        }

        if (this.buffer.length > 0) {
            this.animateFrame(this.buffer.shift());
        }
    }

    private animateFrame([frame, basis]: BufferFrame): void {
        this.notifyPlaying(frame);

        if (frame.isLast) {
            this.sayGoodbye();
            return;
        }

        if (!basis.isNone) {
            this.basis = basis;
        }

        this.drawAnimationFrame(frame);
    }

    private drawAnimationFrame(frame: SnowDataFrame): void {
        this.canvas.clear();
        this.canvas.drawBackground(this.background);
        this.canvas.drawSnow(frame);
        this.canvas.drawBasis(this.basis);
    }

    private sayGoodbye(): void {
        this.state = "goodbye";
        this.stopStream();
        this.reset();
        setTimeout(() => {
            if (this.state !== "goodbye") {
                return;
            }
            this.state = "stopped";
            this.onFinish();
        }, this.goodbyeTextTimeoutSec * 1000);
        return;
    }

    private startStream(session: StartEndpointResponse, onData: (data: DataView) => void): void {
        if (session === AbortedEndpointResponse) {
            return;
        }
        if (this.streamHandler) {
            throw Error("Please stopStream() first!");
        }
        this.streamHandler = startSnowDataStream(session, onData);
    }

    private stopStream(): void {
        if (!this.streamHandler) {
            return;
        }
        stopSnowDataStream(this.streamHandler);
        this.streamHandler = null;
    }

    private isBufferFullyLoaded(): boolean {
        return this.state === "buffering"
            && this.buffer.length >= this.metadata.bufferSizeInFrames;
    }

    private notifyPlaying(frame: SnowDataFrame) {
        this.onPlaying(this.animationProgress(frame), this.bufferLevel());
    }

    private animationProgress(frame: SnowDataFrame): number {
        if (frame.frameNum === -1) {
            return 100;
        }
        return frame.frameNum * 100 / this.metadata.totalNumberOfFrames;
    }

    private notifyBuffering(): void {
        if (this.state !== "buffering") {
            return;
        }
        this.onBuffering(this.bufferLevel());
    }

    private bufferLevel(): number {
        if (this.buffer.length === 0) {
            return 0;
        }
        return Math.min(100, Math.round(this.buffer.length * 100 / this.metadata.bufferSizeInFrames));
    }

    private synchronizeFps(callback: () => void): void {
        if (this.lastTimestamp === null) {
            this.lastTimestamp = Date.now();
            this.fpsInterval = 1000 / this.metadata.fps;
        } else {
            const now = Date.now();
            const elapsed = now - this.lastTimestamp;
            if (elapsed <= this.fpsInterval) {
                return;
            }
            this.lastTimestamp = now - (elapsed % this.fpsInterval);
        }

        callback();
    }
}