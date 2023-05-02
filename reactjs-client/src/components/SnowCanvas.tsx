import * as React from 'react';
import { useRef, useEffect, forwardRef, useImperativeHandle } from 'react';
import { useSnowSession } from '../snow/SnowSessionsProvider'
import { useResizeDetector } from 'react-resize-detector';

interface SnowCanvasProps {
    sessionIdx: number,
}

export interface SnowCanvasRefHandler {
    clearCanvas(): void;
    setCurrentFont(color: string, size: string|number, face?: string): void; // TODO: simplify size param
    drawChar(x: number, y: number, char: string): void;
    drawTextInCenter(text: string): void;
}

export const SnowCanvas = forwardRef<SnowCanvasRefHandler, SnowCanvasProps>(
    function SnowCanvas({ sessionIdx }, ref )
{
    const canvasRef = useRef<HTMLCanvasElement>(null);
    const ctx: CanvasRenderingContext2D = canvasRef.current?.getContext('2d');
    const { validatedWidth : width, validatedHeight : height } = useSnowSession(sessionIdx);
    const { width : canvasWidth, height : canvasHeight, ref : canvasWrapperRef } = useResizeDetector();

    let canvasOffsetV : number;
    let canvasOffsetH : number;
    let workspaceCanvasWidth : number;
    let workspaceCanvasHeight : number;

    // TODO: (canvasHeight * width  > canvasWidth * height) {
    if (canvasHeight * ( width / height ) > canvasWidth) {
        workspaceCanvasWidth = canvasWidth;
        workspaceCanvasHeight = canvasWidth * ( height / width );
        canvasOffsetH = 0;
        canvasOffsetV = ( canvasHeight - workspaceCanvasHeight ) / 2;
    } else {
        workspaceCanvasHeight = canvasHeight;
        workspaceCanvasWidth = canvasHeight * ( width / height );
        canvasOffsetH = ( canvasWidth - workspaceCanvasWidth ) / 2;
        canvasOffsetV = 0;
    }

    const scaleFactorH = workspaceCanvasWidth / width;
    const scaleFactorV = workspaceCanvasHeight / height;

    function resetView() {
        ctx.fillStyle = '#778';
        ctx.fillRect(0, 0,
            canvasRef.current.width,
            canvasRef.current.height
        );

        ctx.fillStyle = 'black';
        ctx.fillRect(
            canvasOffsetH, canvasOffsetV,
            canvasRef.current.width - canvasOffsetH * 2,
            canvasRef.current.height - canvasOffsetV * 2
        );

        const textHeight = Math.floor(scaleFactorV * 2.5);
        ctx.font = `${textHeight}px Courier New`;
    }

    useImperativeHandle(ref, (): SnowCanvasRefHandler => {
        return {
            clearCanvas() {
                resetView();
            },
            drawChar(x: number, y: number, char: string): void {
                ctx.fillText(char, cellX(x), cellY(y));
            },
            setCurrentFont(color: string, size: string|number, face: string = "Courier New"): void {
                if (typeof size === 'string') {
                    ctx.font = size + ' ' + face;
                } else {
                    const textHeight = Math.floor(scaleFactorV * size);
                    ctx.font = `bold ${textHeight}px ${face}`;
                }
                ctx.fillStyle = color;
            },
            drawTextInCenter(text: string): void {
                ctx.save();
                ctx.textAlign="center";
                ctx.fillText(text,
                    canvasWidth / 2,
                    canvasHeight / 2,
                );
                ctx.restore();
            },
        };

        function cellX(x : number): number {
            return x * scaleFactorH + canvasOffsetH;
        }

        function cellY(y : number): number {
            return (y + 0.93 ) * scaleFactorV + canvasOffsetV;
        }

    }, [ scaleFactorH, scaleFactorV, canvasOffsetH, canvasOffsetV ]);

    // scale font to canvas size
    useEffect(() => {
        if (ctx) {
            resetView();
        }
    }, [ width, height, canvasWidth, canvasHeight, scaleFactorV ]);

    return (
        <div ref={canvasWrapperRef} className="snow-animation-canvas-wrapper" >
            <canvas ref={canvasRef} width={canvasWidth} height={canvasHeight} />
        </div>
    );
});

