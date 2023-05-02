import * as React from "react";
import SnowBackground from "../../dto/SnowBackground";
import SnowDataFrame from "../../dto/SnowDataFrame";
import SnowBasis from "../../dto/SnowBasis";
import {SnowCanvas, SnowCanvasRefHandler} from "../SnowCanvas";
import {forwardRef, useImperativeHandle, useRef} from "react";

const animationConstraints = {
    flakeShapes: [
        '#', // pressed
        '*', '*', '*', '*', '*', "'", ".", ",", "`"
    ],

    snowFont: {
        color: "white",
        scale: 1.3,
    },

    backgroundFont: {
        color: "lightblue",
        scale: 1.1,
    },

    basisFont: {
        color: "white",
        scale: 1.1,
    },

    goodbyeText: {
        text: "Thank you for watching",
        color: "lightblue",
        font: "bold Arial",
        size: 10,
        timeoutSec: 2.5,
    },
}

interface SnowDrawingProps {
    sessionIdx: number,
}

export interface SnowDrawingRefHandler {
    clear(): void;
    drawBackground(background: SnowBackground): void;
    drawSnow(frame: SnowDataFrame): void
    drawBasis(basis: SnowBasis): void
    drawGoodbye(): void;
}


export const SnowDrawing = forwardRef<SnowDrawingRefHandler, SnowDrawingProps>(
    function SnowDrawing({ sessionIdx }, ref )
{
    const canvasRef = useRef<SnowCanvasRefHandler>(null);

    useImperativeHandle(ref, (): SnowDrawingRefHandler => {
        return {
            clear(): void {
                const canvas = canvasRef.current;
                canvas.clearCanvas();
            },
            drawBackground(background: SnowBackground): void {
                if (background.isNone) {
                    return;
                }

                const canvas = canvasRef.current;
                const { width, height, pixels } = background;
                const font = animationConstraints.backgroundFont;

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
                const font = animationConstraints.snowFont;
                const flakeShapes = animationConstraints.flakeShapes;

                canvas.setCurrentFont(font.color, font.scale);
                for (let i = 0; i < chunkSize; ++i) {
                    canvas.drawChar(particlesX[i], particlesY[i], flakeShapes[flakes[i]]);
                }
            },

            drawBasis(basis: SnowBasis): void {
                if (basis.isNone) {
                    return;
                }

                const canvas = canvasRef.current;
                const { numOfPixels, x, y, pixels } = basis;
                const font = animationConstraints.basisFont;
                const flakeShapes = animationConstraints.flakeShapes;

                canvas.setCurrentFont(font.color, font.scale);
                for (let i = 0; i < numOfPixels; ++i) {
                    canvas.drawChar(x[i], y[i], flakeShapes[pixels[i]]);
                }
            },

            drawGoodbye(): void {
                const canvas = canvasRef.current;
                const { text, color, font, size } = animationConstraints.goodbyeText;

                canvas.clearCanvas();
                canvas.setCurrentFont(color, size, font);
                canvas.drawTextInCenter(text);
            },
        };
    });

    return <SnowCanvas sessionIdx={sessionIdx} ref={canvasRef} />;
});