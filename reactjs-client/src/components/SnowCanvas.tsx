import * as React from 'react';
import {useRef, useEffect, forwardRef, useImperativeHandle, useCallback} from 'react';
import { useSnowSession } from '../snow/SnowSessionsProvider'
import { useResizeDetector } from 'react-resize-detector';

interface SnowCanvasProps {
    sessionIdx: number,
    canvasColor: string,
    canvasWorkspaceColor: string,
}

export interface SnowCanvasRefHandler {
    clearCanvas(): void;
    setCurrentFont(color: string, size: number, face?: string): void;
    drawChar(x: number, y: number, char: string): void;
    drawTextInCenter(text: string): void;
}

export const SnowCanvas = forwardRef<SnowCanvasRefHandler, SnowCanvasProps>(
    function SnowCanvas({ sessionIdx , canvasWorkspaceColor, canvasColor}, ref )
{
    const canvasRef = useRef<HTMLCanvasElement>(null);
    const { validatedWidth : width, validatedHeight : height } = useSnowSession(sessionIdx);
    const { width : canvasWidth, height : canvasHeight, ref : canvasWrapperRef } = useResizeDetector();

    const isHoriz = canvasHeight * width  > canvasWidth * height;
    const canvasOffsetH = isHoriz ? 0 : ( canvasWidth - canvasHeight * width / height ) / 2;
    const canvasOffsetV = !isHoriz ? 0 : ( canvasHeight - canvasWidth * height / width ) / 2;
    const scaleFactor = isHoriz ? canvasWidth / width : canvasHeight / height;


    const resetView = useCallback( (ctx: CanvasRenderingContext2D): void => {
        ctx.fillStyle = canvasColor;
        ctx.fillRect(0, 0,
            canvasRef.current.width,
            canvasRef.current.height
        );

        ctx.fillStyle = canvasWorkspaceColor;
        ctx.fillRect(
            canvasOffsetH, canvasOffsetV,
            canvasRef.current.width - canvasOffsetH * 2,
            canvasRef.current.height - canvasOffsetV * 2
        );

        const textHeight = Math.floor(scaleFactor * 2.5);
        ctx.font = `${textHeight}px Courier New`;
    }, [ canvasOffsetH, canvasOffsetV, scaleFactor ]);


    useImperativeHandle(ref, (): SnowCanvasRefHandler => {
        const ctx: CanvasRenderingContext2D = canvasRef.current?.getContext('2d');

        return {
            clearCanvas(): void {
                resetView(ctx);
            },
            drawChar(x: number, y: number, char: string): void {
                ctx.fillText(char, cellX(x), cellY(y));
            },
            setCurrentFont(color: string, size: number, face: string = "Courier New"): void {
                const textHeight = Math.floor(scaleFactor * size);
                ctx.font = `bold ${textHeight}px ${face}`;
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
            return x * scaleFactor + canvasOffsetH;
        }

        function cellY(y : number): number {
            return (y + 0.93 ) * scaleFactor + canvasOffsetV;
        }

    }, [ canvasWidth, canvasHeight, resetView, scaleFactor, canvasOffsetH, canvasOffsetV ]);


    // scale font to canvas size
    useEffect(() => {
        const ctx: CanvasRenderingContext2D = canvasRef.current?.getContext('2d');
        if (ctx) {
            resetView(ctx);
        }
    }, [ width, height, canvasWidth, canvasHeight, resetView ]);


    return (
        <div ref={canvasWrapperRef} className="snow-animation-canvas-wrapper" >
            <canvas ref={canvasRef} width={canvasWidth} height={canvasHeight} />
        </div>
    );
});

