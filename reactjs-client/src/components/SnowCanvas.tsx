import * as React from 'react';
import { useRef, useEffect } from 'react';
// import SnowFlakes from './SnowFlakes'
// import { SnowDataFrame } from '../dto/SnowDataFrame'
import { useSnowSession } from '../snow/SnowSessionsProvider'
import { useResizeDetector } from 'react-resize-detector';

type SnowCanvasProps = {
    sessionIdx: number
}

export default function SnowCanvas({ sessionIdx } : SnowCanvasProps) {
    const { validatedWidth : width, validatedHeight : height } = useSnowSession(sessionIdx);;
    const { width: canvasWidth, height : canvasHeight, ref : canvasWrapperRef } = useResizeDetector();
    const canvasRef = useRef();

    const canvas : HTMLCanvasElement = canvasRef.current;
    const ctx = canvas === undefined ? null : canvas.getContext('2d');

    let canvasOffsetV : number;
    let canvasOffsetH : number;
    let workspaceCanvasWidth : number;
    let workspaceCanvasHeight : number;

    if (canvasHeight * ( width / height ) > canvasWidth) {
        workspaceCanvasWidth = canvasWidth;
        workspaceCanvasHeight = canvasWidth * ( height / width );
        canvasOffsetH = 0;
        canvasOffsetV = ( canvasHeight - workspaceCanvasHeight ) / 2;;
    } else {
        workspaceCanvasHeight = canvasHeight;
        workspaceCanvasWidth = canvasHeight * ( width / height );
        canvasOffsetH = ( canvasWidth - workspaceCanvasWidth ) / 2;
        canvasOffsetV = 0;
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
