import * as React from "react";
import {useEffect, useRef} from "react";
import {SnowDrawing, SnowDrawingRefHandler} from "./SnowDrawing";
import {SnowAnimationConfiguration} from "../../stream/snowEndpoint";
import {useSnowSession} from "../../snow/SnowSessionsProvider";
import {SnowAnimationController} from "../../snow/SnowAnimationController";


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

    goodbyeText: {
        text: "Thank you for watching",
        color: "lightblue",
        font: "bold Arial",
        timeoutSec: 2.5,
    },
}


interface SnowAnimationProps {
    sessionIdx: number;
    play: boolean;
    configuration: SnowAnimationConfiguration;
    onFinish: () => void;
    onBuffering: (percent: number) => void;
    onPlaying: (progress: number, bufferPercent: number) => void;
    onError: (error: Error) => void;
}

export default function SnowAnimation(props: SnowAnimationProps): JSX.Element {
    const {
        sessionIdx,
        play,
        configuration,
        onBuffering, onPlaying, onFinish, onError
    } = props;
    const { sessionId} = useSnowSession(sessionIdx);
    const canvasRef = useRef<SnowDrawingRefHandler>(null);
    const snowControllerRef = useRef<SnowAnimationController>(new SnowAnimationController(sessionId));


    useEffect(() => {
        const snowController = snowControllerRef.current;
        if (!snowController) {
            return;
        }

        snowController.configure({
            goodbyeTextTimeoutSec: animationConstraints.goodbyeText.timeoutSec,
            canvas: canvasRef.current,
            onBuffering,
            onPlaying,
            onFinish,
            onError,
        });
    }, [ onBuffering, onPlaying, onFinish, onError, canvasRef ]);


    useEffect(() => {
        const snowController = snowControllerRef.current;
        if (!snowController) {
            return;
        }

        const abortController = new AbortController();
        if (play) {
            void snowController.startProcessing(configuration, abortController);
        } else {
            void snowController.stopProcessing(abortController);
        }
        return () => { abortController.abort() };
    }, [ play, sessionId, configuration ]);


    return <SnowDrawing sessionIdx={sessionIdx} ref={canvasRef} />;
}
