import {SnowDrawingRefHandler} from "../components/SnowAnimationPlayer/SnowDrawing";
import SnowDecoder from "../stream/SnowDecoder";
import SnowAnimationMetadata from "../dto/SnowAnimationMetadata";
import SnowBasis, {NoSnowBasis} from "../dto/SnowBasis";
import SnowBackground, {NoSnowBackground} from "../dto/SnowBackground";
import {
    AbortedEndpointResponse,
    DetailsEndpointResponse,
    fetchSnowDetails,
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
    private sessionId: string;

    private onBuffering: (startingBuffering: boolean, percent: number) => void;
    private onPlaying: (firstFrame:boolean, progress: number, bufferPercent: number) => void;
    private onChecking: (sessionId: string, periodicCheck: boolean) => void;
    private onFound: (response: DetailsFromServer, periodicCheck: boolean) => void;
    private onNotFound: (periodicCheck: boolean) => void;
    private onError: (error: Error) => void;
    private onFinish: () => void;

    private state: "stopped" | "buffering" | "playing" | "goodbye" = "stopped";
    private buffer: BufferFrame[];
    private isDestroyed: boolean;
    private decoder: SnowDecoder;
    private fpsInterval: number;
    private firstData: boolean;
    private firstFrame: boolean;
    private lastTimestamp: number;
    private checkingEnabled: boolean;
    private isLastFrameInBuffer: boolean;
    private goodbyeTextTimeoutSec: number;
    private inBackground: boolean;
    private basis: SnowBasis;
    private background: SnowBackground;
    private canvas: SnowDrawingRefHandler;
    private metadata: SnowAnimationMetadata;
    private streamHandler: SnowClientHandler;
    private periodicHandler: ReturnType<typeof setInterval>;
    private abortController = new AbortController();

    public constructor(sessionId: string, decoder: SnowDecoder = new SnowDecoder()) {
        this.isDestroyed = false;
        this.sessionId = sessionId;
        this.decoder = decoder;
        this.reset();
    }

    public processingInForeground(
        sessionId: string,
        canvas: SnowDrawingRefHandler,
        checkingEnabled: boolean,
        goodbyeTextTimeoutSec: number,
        onBuffering: (startingBuffering: boolean, percent: number) => void,
        onPlaying: (firstFrame: boolean, progress: number, bufferPercent: number) => void,
        onChecking: (sessionId: string, periodicCheck: boolean) => void,
        onFound: (response: DetailsFromServer, periodicCheck: boolean) => void,
        onNotFound: (periodicCheck: boolean) => void,
        onError: (error: Error) => void,
        onFinish: () => void,
    ): void {
        this.canvas = canvas;
        this.inBackground = false;
        this.sessionId = sessionId;
        this.checkingEnabled = checkingEnabled;
        this.goodbyeTextTimeoutSec = goodbyeTextTimeoutSec;

        this.onFound = onFound;
        this.onNotFound = onNotFound;
        this.onChecking = onChecking;
        this.onBuffering = onBuffering;
        this.onPlaying = onPlaying;
        this.onFinish = onFinish;
        this.onError = onError;

        if (this.state === 'playing') {
            this.startAnimation();
        }
    }

    public processingInBackground(): void {
        this.inBackground = true;
    }

    public destroy(): void {
        this.abortController.abort()
        this.stopPeriodicChecking();
        this.isDestroyed = true;
        this.turnOff();
    }

    public isRunning(): boolean {
        return this.state !== 'stopped';
    }

    public async startProcessing(configuration: SnowAnimationConfiguration): Promise<void> {
        if (this.state !== 'stopped') {
            return;
        }
        this.disallowWhenDestroyed();

        if (this.state !== 'stopped') {
            throw Error("Need to stopProcessing() first!");
        }
        try {
            this.startStream(
                await startSnowSession(this.sessionId, configuration, this.abortController),
                this.onServerData.bind(this)
            );
            this.state = "buffering";
        } catch (error) {
            console.error(error);
            this.onError(new CannotStartError(error.message));
        }
    }

    public async stopProcessing(): Promise<void> {
        this.disallowWhenDestroyed();

        if (this.state === 'stopped') {
            return;
        }
        if (this.state === 'goodbye') {
            this.state = "stopped";
            return;
        }
        try {
            this.turnOff();
            await stopSnowSession(this.sessionId, this.abortController);
        } catch (error) {
            console.error(error);
            this.onError(new CannotStopError(error.message));
        }
    }

    public startPeriodicChecking(refreshEveryMs: number): void {
        this.disallowWhenDestroyed();
        this.stopPeriodicChecking();
        this.periodicHandler = setInterval(() => {
            void this.askServerForDetails(this.abortController, true);
        }, refreshEveryMs);
    }

    public stopPeriodicChecking(): void {
        if (this.periodicHandler) {
            clearInterval(this.periodicHandler);
            this.periodicHandler = null;
        }
    }

    public async checkDetails(abortController?: AbortController): Promise<void> {
        this.disallowWhenDestroyed();

        await this.askServerForDetails(abortController ?? this.abortController, false);
    }

    private async askServerForDetails(abortController: AbortController, periodicCheck: boolean): Promise<void> {
        try {
            if (!this.checkingEnabled) {
                return;
            }

            this.onChecking(this.sessionId, periodicCheck);

            const response: DetailsEndpointResponse = await fetchSnowDetails(this.sessionId, abortController);
            if (!this.checkingEnabled || response === AbortedEndpointResponse) {
                return;
            }
            if (response.running) {
                this.onFound(response, periodicCheck);
            } else {
                if (!this.isLastFrameInBuffer
                    && (this.state === "playing" || this.state === "buffering"))
                {
                    this.turnOff();
                    this.onFinish();
                }
                this.onNotFound(periodicCheck);
            }
        } catch (error) {
            console.error(error);
            this.onError(new CannotCheckError(error.message));
        }
    }

    private turnOff() {
        this.state = "stopped";
        this.stopStream();
        this.reset();
    }

    private reset(): void {
        this.firstData = true;
        this.firstFrame = true;
        this.lastTimestamp = null;
        this.basis = NoSnowBasis;
        this.background = NoSnowBackground;
        this.inBackground = false;
        this.isLastFrameInBuffer = false;
        this.buffer = new Array<[SnowDataFrame, SnowBasis]>();
    }

    private onServerData(data: DataView): void {
        if (this.state === "stopped" || this.state === 'goodbye' || this.isDestroyed) {
            return;
        }

        if (this.firstData) {
            this.notifyStartBuffering();
            this.firstData = false;
            [ this.metadata, this.background ] = this.decoder.decodeHeader(data);
            return;
        }

        const frame: BufferFrame = this.decoder.decodeFrame(data);
        this.buffer.push(frame);
        // console.log(frame);

        if (this.isLastFrame(frame[0])) {
            this.isLastFrameInBuffer = true;
        }

        if (this.isBufferOverflowed()) {
            if (this.state === "buffering") {
                this.startAnimation();
            } else {
                this.buffer.shift();
            }
        } else {
            this.notifyBuffering();
        }
    }

    private startAnimation(): void {
        this.state = "playing";
        this.animationLoop();
    }

    private animationLoop(): void {
        if (this.inBackground) {
            return;
        }

        if (this.state === "stopped" || this.isDestroyed) {
            this.canvas.clear();
            return;
        }

        requestAnimationFrame(this.animationLoop.bind(this));
        this.synchronizeFps(() => {
            this.animationStep();
        });
    }

    private animationStep(): void {
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
        this.firstFrame = false;

        if (this.isLastFrame(frame)) {
            this.sayGoodbye();
            return;
        }

        if (basis !== NoSnowBasis) {
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
        this.turnOff();

        this.state = "goodbye";
        setTimeout(() => {
            if (this.state !== "goodbye") {
                return;
            }
            this.state = "stopped";
            this.onFinish();
        }, this.goodbyeTextTimeoutSec * 1000);
    }

    private startStream(startResponse: StartEndpointResponse, onData: (data: DataView) => void): void {
        if (startResponse === AbortedEndpointResponse || this.isDestroyed) {
            return;
        }
        if (this.streamHandler) {
            throw Error("Please stopStream() first!");
        }
        this.streamHandler = startSnowDataStream(startResponse, onData);
    }

    private stopStream(): void {
        if (!this.streamHandler) {
            return;
        }
        stopSnowDataStream(this.streamHandler);
        this.streamHandler = null;
    }

    private isBufferOverflowed(): boolean {
        return this.buffer.length > this.metadata.bufferSizeInFrames + 1;
    }

    private notifyPlaying(frame: SnowDataFrame): void {
        this.onPlaying(this.firstFrame, this.animationProgress(frame), this.bufferLevel());
    }

    private isLastFrame(frame: SnowDataFrame): boolean {
        return frame.frameNum === this.metadata.totalNumberOfFrames;
    }

    private animationProgress(frame: SnowDataFrame): number {
        if (frame.frameNum === -1) {
            return 100;
        }
        return frame.frameNum * 100 / this.metadata.totalNumberOfFrames;
    }

    private notifyStartBuffering(): void {
        this.onBuffering(true,0);
    }

    private notifyBuffering(): void {
        if (this.state !== "buffering") {
            return;
        }
        this.onBuffering(false, this.bufferLevel());
    }

    private bufferLevel(): number {
        if (this.buffer.length === 0) {
            return 0;
        }
        return Math.min(100, Math.round(100 * this.buffer.length / this.metadata.bufferSizeInFrames));
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

    private disallowWhenDestroyed(): void {
        if (this.isDestroyed) {
            throw Error("You cannot use destroyed controller!");
        }
    }
}

export class CannotStartError extends Error {
    constructor(message: string) {
        super(message);
        this.name = this.constructor.name;
    }
}

export class CannotStopError extends Error {
    constructor(message: string) {
        super(message);
        this.name = this.constructor.name;
    }
}

export class CannotCheckError extends Error {
    constructor(message: string) {
        super(message);
        this.name = this.constructor.name;
    }
}
