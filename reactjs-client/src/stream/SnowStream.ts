import * as React from "react";
import SnowDataFrame from '../dto/SnowDataFrame';
import SnowDecoder from './SnowDecoder';
import { Stomp, Client } from '@stomp/stompjs';

export interface SnowStreamProps {
    sessionId: string;
    presetName: string;
    width: number;
    height: number;
    fps: number;
}

export default class SnowStream extends React.Component<SnowStreamProps, {}>{
    private stompClient: Client|null = null;
    private isFirstFrame: boolean = true;
    private decoder: SnowDecoder = new SnowDecoder();

    startSession() {
        this.isFirstFrame = true;

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

    stopSession() {
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

    private startConsumingWebsocket() {
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

    private stopConsumingWebsocket() {
        if (this.stompClient) {
            this.stompClient.deactivate();
            this.stompClient = null;
        }
    }

    private handleMessage(message: any) {
        const data = new DataView(message.binaryBody.buffer);
        if (this.isFirstFrame) {
            this.isFirstFrame = false;
            this.handleSnowMetadata(data);
        } else {
            this.handleSnowDataFrame(data);
        }
    }

    private handleSnowMetadata(data : DataView) {
    }

    private handleSnowDataFrame(data : DataView): SnowDataFrame {
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

        return {
            frameNum: frameNum,
            chunkSize: chunkSize,
            particlesX: x,
            particlesY: y,
            flakeShapes: flakes,
        };
    }

    generateMyUniqueSessionId(): string {
        return Math.random().toString(36).slice(2)
            + Math.random().toString(36).slice(2)
            + Math.random().toString(36).slice(2);
    }

}