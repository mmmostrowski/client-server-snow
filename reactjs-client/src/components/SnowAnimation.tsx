import * as React from "react";

import { Stomp, Client } from '@stomp/stompjs';
import SnowCanvas from './SnowCanvas'


type SnowAnimationProps = {
    sessionId: string;
    presetName: string;
    width: number;
    height: number;
    fps: number;

    autostart: boolean;
}

type SnowAnimationState = {
    width: number;
    height: number;
}

export default class SnowAnimation extends React.Component <SnowAnimationProps, SnowAnimationState> {

    static defaultProps = {
        autostart: true,
        width: 180,
        height: 40,
        fps: 25,
    }

    state: SnowAnimationState = {
        width: this.props.width,
        height: this.props.height,
    }

    private snowCanvasRef : React.RefObject<SnowCanvas> = React.createRef<SnowCanvas>();
    private animationLastTimestamp : number = 0;
    private fpsInterval : number = 1000 / this.props.fps;
    private isFirstFrame: boolean = true;
    private isAnimationRunning: boolean = false;
    private stompClient: Client|null = null;

    constructor(props: SnowAnimationProps) {
        super(props);
    }

    componentDidMount() {
        if (this.props.autostart) {
            this.start();
        }
    }

    componentWillUnmount() {
        this.stop();
    }

    start() {
        if (this.isAnimationRunning) {
            this.stop();
        }

        console.log("START");
        this.isFirstFrame = true;
        this.askServerForNewSession();
    }

    askServerForNewSession() {
        let paramsQuery = "/fps/" + this.props.fps
            + "/width/" + this.props.width
            + "/height/" + this.props.height
            + "/presetName/" + this.props.presetName;

        fetch('http://127.0.0.1:8080/start/' + this.props.sessionId + paramsQuery)
             .then((response) => response.json())
             .then((data) => data.running)
             .then((running) => {
                if (!running) {
                    throw "Server responded that snow session is not running!"
                }
                this.startConsumingWebsocket();
             })
             .catch((err) => {
                console.log(err.message);
             });
    }

    askServerToStopSession() {
        fetch('http://127.0.0.1:8080/stop/' + this.props.sessionId)
             .then((response) => response.json())
             .then((data) => data.running)
             .then((running) => {
                if (running) {
                    throw "Server responded that snow session is still running!"
                }
             })
             .catch((err) => {
                console.log(err.message);
             });
    }

    startConsumingWebsocket() {
        let clientId = this.generateMyUniqueSessionId();

        const client = new Client({
          brokerURL: 'ws://127.0.0.1:8080/ws/',
          onConnect: (frame) => {
            client.publish({
                destination: '/app/stream/' + this.props.sessionId,
                body: clientId
            });
            client.subscribe('/user/' + clientId + '/user/stream/', this.handleMessage.bind(this));
          },
        });
        client.activate();

        this.stompClient = client;
    }

    stopConsumingWebsocket() {
        if (this.stompClient) {
            this.stompClient.deactivate();
            this.stompClient = null;
        }
    }

    handleMessage(message: any) {
        const data = new DataView(message.binaryBody.buffer);
        if (this.isFirstFrame) {
            this.isFirstFrame = false;
            this.handleSnowMetadata(data);
        } else {
            this.handleSnowDataFrame(data);
        }
    }

    handleSnowMetadata(data : DataView) {
    }

    handleSnowDataFrame(data : DataView) {
        let frameNum = data.getInt32(0, false);
        let chunkSize = data.getUint32(4, false);
        let x = new Float32Array(chunkSize);
        let y = new Float32Array(chunkSize);
        let flakes = new Uint8Array(chunkSize);
        let ptr = 8;
        console.log("chunkSize", chunkSize);
        for (var i = 0; i < chunkSize; ++i) {
            x[i] = data.getFloat32(ptr, false);
            y[i] = data.getFloat32(ptr + 4, false);
            flakes[i] = data.getUint8(ptr + 8);
            ptr += 9;
        }
        console.log("frameNum", frameNum);

        this.snowCanvasRef.current.renderSnowFrame({
            frameNum: frameNum,
            chunkSize: chunkSize,
            particlesX: x,
            particlesY: y,
            flakeShapes: flakes,
        });

    }

    startAnimation() {
        this.isAnimationRunning = true;
        this.animationLastTimestamp = Date.now();
        window.requestAnimationFrame(this.animationFrame.bind(this));
    }


    stop() {
        console.log("STOP");
        this.isAnimationRunning = false;
        this.stopConsumingWebsocket();
        this.askServerToStopSession();
    }

    animationFrame() {
        if (this.isAnimationRunning) {
             window.requestAnimationFrame(this.animationFrame.bind(this));
        }

        let now = Date.now();
        let elapsed = now - this.animationLastTimestamp;
        if (elapsed > this.fpsInterval) {
            this.animationLastTimestamp = now - ( elapsed % this.fpsInterval );

            console.log('animation');
        }
    }

    handleStartClick() {
        this.start();
    }

    handleStopClick() {
        this.stop();
    }

    generateMyUniqueSessionId(): string {
        return Math.random().toString(36).slice(2)
            + Math.random().toString(36).slice(2)
            + Math.random().toString(36).slice(2);
    }

    render() {
        return (
            <div>
                <button onClick={this.handleStartClick.bind(this)}>
                    Start
                </button>
                <button onClick={this.handleStopClick.bind(this)}>
                    Stop
                </button>
                <br/>
                <SnowCanvas ref={this.snowCanvasRef} width={this.state.width} height={this.state.height} />
            </div>
        );
    }

}