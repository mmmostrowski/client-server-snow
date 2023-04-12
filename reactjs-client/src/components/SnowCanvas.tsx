import * as React from 'react';
import { useRef } from 'react';
import SnowFlakes from './SnowFlakes'
import { SnowDataFrame } from '../dto/SnowDataFrame'

type SnowCanvasProps = {
    width: number,
    height: number,
    scaleFactorV: number,
    scaleFactorH: number,
}

export default class SnowCanvas extends React.Component<SnowCanvasProps, {}> {

    private flakeShapes = new SnowFlakes();
    private canvasRef = React.createRef<HTMLCanvasElement>();
    private lastFrame : SnowDataFrame|null = null;

    componentDidMount() {
        this.setupViewport();
        this.renderSnowFrame(this.lastFrame);
    }

    componentDidUpdate() {
        this.setupViewport();
        this.renderSnowFrame(this.lastFrame);
    }

    renderSnowFrame(frame : SnowDataFrame|null) {
        if (frame == null) {
            return;
        }

        const canvas = this.canvasRef.current;
        if (!canvas) {
            return;
        }

        const ctx = canvas.getContext('2d');
        if (!ctx) {
            return;
        }

        // Draw background
        ctx.fillStyle = 'black';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
        for (let i = 0; i < frame.chunkSize; ++i) {
            this.flakeShapes.renderFlakeShape(ctx,
                frame.flakeShapes[i],
                this.viewportX(frame.particlesX[i]),
                this.viewportY(frame.particlesY[i]));
        }
    }

    private get canvasWidth() {
        return this.props.width * this.props.scaleFactorH;
    }

    private get canvasHeight() {
        return this.props.height * this.props.scaleFactorV;
    }

    private setupViewport() {
        this.flakeShapes.setupViewport(
            this.props.width,
            this.props.height,
            this.props.scaleFactorH,
            this.props.scaleFactorV
        );
    }

    private viewportX(x : number): number {
        return x * this.props.scaleFactorH;
    }

    private viewportY(y : number): number {
        return y * this.props.scaleFactorV;
    }

    render() {
        return (
            <canvas width={this.canvasWidth} height={this.canvasHeight} ref={this.canvasRef} />
        );
    }

}
