import * as React from "react";
import {forwardRef, useImperativeHandle, useRef} from "react";
import SnowBackground, {NoSnowBackground} from "../../dto/SnowBackground";
import SnowDataFrame from "../../dto/SnowDataFrame";
import SnowBasis, {NoSnowBasis} from "../../dto/SnowBasis";
import {SnowCanvas, SnowCanvasRefHandler} from "../SnowCanvas";
import {animationConfig} from "../../config/animation";


interface Props {
    width: number,
    height: number,
}

export interface SnowDrawingRefHandler {
    clear(): void;
    drawBackground(background: SnowBackground): void;
    drawSnow(frame: SnowDataFrame): void
    drawBasis(basis: SnowBasis): void
    drawGoodbye(): void;
}


export const SnowDrawing = forwardRef<SnowDrawingRefHandler, Props>(
    function SnowDrawing({ width, height }, ref )
{
    const canvasRef = useRef<SnowCanvasRefHandler>(null);
    const { canvas } = animationConfig;

    useImperativeHandle(ref, (): SnowDrawingRefHandler => {
        return {
            clear(): void {
                const canvas = canvasRef.current;
                canvas.clearCanvas();
            },
            drawBackground(background: SnowBackground): void {
                if (background === NoSnowBackground) {
                    return;
                }

                const canvas = canvasRef.current;
                const { width, height, pixels } = background;
                const font = animationConfig.backgroundFont;

                canvas.setCurrentFont(font.color, font.scale);
                for (let y = 0; y < height; ++y) {
                    for (let x = 0; x < width; ++x) {
                        const char = pixels[x][y];
                        if (char === 0) {
                            continue;
                        }
                        canvas.drawChar(x, y, String.fromCharCode(char));
                    }
                }
            },

            drawSnow(frame: SnowDataFrame): void {
                const canvas = canvasRef.current;
                const { particlesX, particlesY, flakeShapes: flakes, chunkSize } = frame;
                const font = animationConfig.snowFont;
                const flakeShapes = animationConfig.flakeShapes;

                canvas.setCurrentFont(font.color, font.scale);
                for (let i = 0; i < chunkSize; ++i) {
                    canvas.drawChar(particlesX[i], particlesY[i], flakeShapes[flakes[i]]);
                }
            },

            drawBasis(basis: SnowBasis): void {
                if (basis === NoSnowBasis) {
                    return;
                }

                const canvas = canvasRef.current;
                const { numOfPixels, x, y, pixels } = basis;
                const font = animationConfig.basisFont;
                const flakeShapes = animationConfig.flakeShapes;

                canvas.setCurrentFont(font.color, font.scale);
                for (let i = 0; i < numOfPixels; ++i) {
                    canvas.drawChar(x[i], y[i], flakeShapes[pixels[i]]);
                }
            },

            drawGoodbye(): void {
                const canvas = canvasRef.current;
                const { text, color, font, size } = animationConfig.goodbyeText;

                canvas.clearCanvas();
                canvas.setCurrentFont(color, size, font);
                canvas.drawTextInCenter(text);
            },
        };
    });

    return <SnowCanvas
        width={width}
        height={height}
        ref={canvasRef}
        canvasColor={canvas.color}
        canvasWorkspaceColor={canvas.backgroundColor}
    />;
});