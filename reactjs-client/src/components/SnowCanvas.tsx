import * as React from 'react';
import { useRef, useEffect, forwardRef, useImperativeHandle } from 'react';
import { useSnowSession } from '../snow/SnowSessionsProvider'
import { useResizeDetector } from 'react-resize-detector';

type SnowCanvasProps = {
    sessionIdx: number,
}

export interface SnowCanvasRefHandler {
    clearBackground(): void;
    setCurrentFont(color: string, scaleFactor: number): void;
    drawChar(x: number, y: number, char: string): void;
}

export const SnowCanvas = forwardRef<SnowCanvasRefHandler, SnowCanvasProps>(function SnowCanvas({ sessionIdx }, ref ) {
    const { validatedWidth : width, validatedHeight : height } = useSnowSession(sessionIdx);
    const { width: canvasWidth, height : canvasHeight, ref : canvasWrapperRef } = useResizeDetector();
    const canvasRef = useRef<HTMLCanvasElement>(null);

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

    useImperativeHandle(ref, () => {
        function viewportX(x : number): number {
            return x * scaleFactorH + canvasOffsetH;
        }

        function viewportY(y : number): number {
            return y * scaleFactorV + canvasOffsetV;
        }

        return {
            clearBackground() {
                resetView();
            },
            drawChar(x: number, y: number, char: string) {
                whenContext((ctx) => {
                    const textOffsetH = Math.floor(scaleFactorV * 0);
                    const textOffsetV = Math.floor(scaleFactorV * 0);

                    ctx.fillText(char,
                        viewportX(x) + textOffsetH,
                        viewportY(y) + textOffsetV
                    );
                });
            },
            setCurrentFont(color: string, scaleFactor: number) {
                whenContext((ctx) => {
                    const textHeight = Math.floor(scaleFactorV * scaleFactor);
                    ctx.font = `bold ${textHeight}px Courier New`;
                    ctx.fillStyle = color;
                });
            },
        };
      }, [ scaleFactorH, scaleFactorV, canvasOffsetH, canvasOffsetV ]);


    // scale font to canvas
    useEffect(() => {
        resetView();
    }, [ width, height, canvasWidth, canvasHeight, scaleFactorV ]);

    function resetView() {
        whenContext((ctx) => {
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
        });
    }

    function whenContext(callback: (ctx: CanvasRenderingContext2D) => void ): void {
        const ctx = canvasRef.current?.getContext('2d');
        if (ctx) {
            callback(ctx);
        }
    }

    return (
        <div ref={canvasWrapperRef} className="snow-animation-canvas-wrapper" >
            <canvas ref={canvasRef} width={canvasWidth} height={canvasHeight} />
        </div>
    );
});

