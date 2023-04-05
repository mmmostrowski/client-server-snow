import * as React from "react";

import { Stomp, Client } from '@stomp/stompjs';
import SnowCanvas from './SnowCanvas'
import { default as SnowStream, SnowStreamProps } from '../stream/SnowStream'


interface SnowAnimationProps extends SnowStreamProps {
    autostart: boolean;
}

interface SnowAnimationState {
    width: number;
    height: number;
}

export default class SnowAnimation extends React.Component<SnowAnimationProps, SnowAnimationState> {

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
    private isAnimationRunning: boolean = false;
    private snowStream : SnowStream = new SnowStream(this.props);

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
        this.snowStream.startSession();
    }

//     private startAnimation() {
//         this.isAnimationRunning = true;
//         this.animationLastTimestamp = Date.now();
//         window.requestAnimationFrame(this.animationFrame.bind(this));
//     }

    stop() {
        console.log("STOP");
        this.isAnimationRunning = false;
        this.snowStream.stopSession();
    }

//     private animationFrame() {
//         if (this.isAnimationRunning) {
//              window.requestAnimationFrame(this.animationFrame.bind(this));
//         }
//
//         let now = Date.now();
//         let elapsed = now - this.animationLastTimestamp;
//         if (elapsed > this.fpsInterval) {
//             this.animationLastTimestamp = now - ( elapsed % this.fpsInterval );
//
//             console.log('animation');
//         }
//     }

    render() {
        return (
            <div>
                <button onClick={this.start.bind(this)}>
                    Start
                </button>
                <button onClick={this.stop.bind(this)}>
                    Stop
                </button>
                <br/>
                <SnowCanvas ref={this.snowCanvasRef} width={this.state.width} height={this.state.height} />
            </div>
        );
    }

}