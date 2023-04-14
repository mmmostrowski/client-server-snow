import * as React from 'react';
import { useRef, useEffect } from 'react';
import SnowFlakes from './SnowFlakes'
import { SnowDataFrame } from '../dto/SnowDataFrame'
import { SnowSession } from '../snow/SnowSessionsProvider'
import { useResizeDetector } from 'react-resize-detector';

type SnowCanvasProps = {
    session : SnowSession
}

export default function SnowCanvas({ session } : SnowCanvasProps) {
    const { width: canvasWidth, height : canvasHeight, ref : canvasWrapperRef } = useResizeDetector();
    const canvasRef = useRef();
    const { validatedWidth : width, validatedHeight : height } = session;

    const canvas : HTMLCanvasElement = canvasRef.current;
    const ctx = canvas === undefined ? null : canvas.getContext('2d');

    let canvasOffsetV = 0;
    let canvasOffsetH = 0;

    let workspaceCanvasHeight = canvasHeight;
    let workspaceCanvasWidth = canvasHeight * ( width / height );
    if (workspaceCanvasWidth > canvasWidth) {
        workspaceCanvasWidth = canvasWidth;
        workspaceCanvasHeight = canvasWidth * ( height / width );
        canvasOffsetV = ( canvasHeight - workspaceCanvasHeight ) / 2;;
    } else {
        canvasOffsetH = ( canvasWidth - workspaceCanvasWidth ) / 2;
    }

    const scaleFactorH = workspaceCanvasWidth / width;
    const scaleFactorV = workspaceCanvasHeight / height;

    useEffect(() => {
        if (canvas === undefined) {
            return;
        }

        clearBackground();

        const textHeight = Math.floor(scaleFactorV * 2.5);
        const textOffsetH = Math.floor(scaleFactorV * -0.25);
        const textOffsetV = Math.floor(scaleFactorV * 1.9);

        ctx.fillStyle = 'white';
        ctx.font = `${textHeight}px Courier New`;
        for (let y = 0; y < height; ++y) {
            for (let x = 0; x < width; ++x) {
                ctx.fillText("*", viewportX(x) + textOffsetH, viewportY(y) + textOffsetV);
            }
        }
    });

    function clearBackground() {
        ctx.fillStyle = 'black';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
    }

    function viewportX(x : number): number {
        return x * scaleFactorH + canvasOffsetH;
    }

    function viewportY(y : number): number {
        return y * scaleFactorV + canvasOffsetV;
    }


    return (
        <div ref={canvasWrapperRef} className="snow-animation-canvas-wrapper" >
            <canvas ref={canvasRef} width={canvasWidth} height={canvasHeight} />
        </div>
    );
}


//     private flakeShapes = new SnowFlakes();
//     private canvasRef = React.createRef<HTMLCanvasElement>();
//     private canvasWrapperRef = React.createRef<HTMLDivElement>();
//     private lastFrame : SnowDataFrame|null = null;
//
//     componentDidMount() {
//         this.setupViewport();
//         this.renderSnowFrame(this.lastFrame);
//     }
//
//     componentDidUpdate() {
//         this.setupViewport();
//         this.renderSnowFrame(this.lastFrame);
//     }
//
//     renderSnowFrame(frame : SnowDataFrame|null) {
//         if (frame == null) {
//             return;
//         }
//
//         const canvas = this.canvasRef.current;
//         if (!canvas) {
//             return;
//         }
//
//         const ctx = canvas.getContext('2d');
//         if (!ctx) {
//             return;
//         }
//
//         // Draw background
//         ctx.fillStyle = 'black';
//         ctx.fillRect(0, 0, canvas.width, canvas.height);
//         for (let i = 0; i < frame.chunkSize; ++i) {
//             this.flakeShapes.renderFlakeShape(ctx,
//                 frame.flakeShapes[i],
//                 this.viewportX(frame.particlesX[i]),
//                 this.viewportY(frame.particlesY[i]));
//         }
//     }
//
//     private get canvasWidth() {
//         return this.props.width * this.props.scaleFactorH;
//     }
//
//     private get canvasHeight() {
//         return this.props.height * this.props.scaleFactorV;
//     }
//
//     private setupViewport() {
//         this.flakeShapes.setupViewport(
//             this.props.width,
//             this.props.height,
//             this.props.scaleFactorH,
//             this.props.scaleFactorV
//         );
//     }
//
//     private handleResize(e : any): void {
//         console.log(e);
//
//     }
//
//     render() {
//         return (
//             <div ref={this.size.ref} style={{ width: '100%', height: '100%' }} >
//                 <canvas ref={this.canvasRef} />
//             </div>
//         );
//     }
// }
