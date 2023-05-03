import * as React from "react";
import {useEffect, useRef} from "react";
import {SnowDrawing, SnowDrawingRefHandler} from "./SnowDrawing";
import {SnowAnimationConfiguration} from "../../stream/snowEndpoint";
import {DetailsFromServer, SnowAnimationController} from "../../snow/SnowAnimationController";


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
    sessionId: string;
    play: boolean;
    configuration: SnowAnimationConfiguration;
    onFinish: () => void;
    onBuffering: (percent: number) => void;
    onPlaying: (progress: number, bufferPercent: number) => void;
    onError: (error: Error) => void;
    onChecking: (sessionId: string, periodicCheck: boolean) => void;
    onFound: (response: DetailsFromServer, periodicCheck: boolean) => void;
    onNotFound: (periodicCheck: boolean) => void;
    checkEveryMs: number;
    checkingEnabled: boolean;
    allowForGoodbye: boolean;
    width: number;
    height: number;
}

export default function SnowAnimation(props: SnowAnimationProps): JSX.Element {
    const {
        sessionId,
        play,
        configuration, allowForGoodbye,
        checkEveryMs, checkingEnabled,
        width, height,
        onBuffering, onPlaying, onFinish, onError, onChecking, onFound, onNotFound
    } = props;
    const canvasRef = useRef<SnowDrawingRefHandler>(null);
    const snowControllerRef = useRef<SnowAnimationController>(null);


    // Bind controller with session
    useEffect(() => {
        if (snowControllerRef.current) {
            snowControllerRef.current.destroy();
        }
        const abortController = new AbortController();
        snowControllerRef.current = new SnowAnimationController(sessionId);
        if (checkingEnabled) {
            snowControllerRef.current.startPeriodicChecking(abortController, checkEveryMs);
        }
        return () => { abortController.abort() };
    }, [ sessionId, checkEveryMs, checkingEnabled ]);


    // Configure controller
    useEffect(() => {
        snowControllerRef.current.configure({
            goodbyeTextTimeoutSec: animationConstraints.goodbyeText.timeoutSec,
            canvas: canvasRef.current,
            onChecking,
            onFound,
            onNotFound,
            onBuffering,
            onPlaying,
            onFinish,
            onError,
            allowForGoodbye,
        });
    }, [ onChecking, onFound, onNotFound, onBuffering, onPlaying, onFinish, onError, allowForGoodbye ]);


    // Check session details
    useEffect(() => {
        const abortController = new AbortController();

        if (checkingEnabled) {
            void snowControllerRef.current.checkDetails(abortController);
        }

        return () => { abortController.abort() };
    }, [ sessionId, checkingEnabled ]);



    // Start / Stop controller
    useEffect(() => {
        const abortController = new AbortController();
        if (play) {
            void snowControllerRef.current.startProcessing(configuration, abortController);
        } else {
            void snowControllerRef.current.stopProcessing(abortController);
        }
        return () => { abortController.abort() };
    }, [ play, configuration ]);


    return <SnowDrawing width={width} height={height} ref={canvasRef} />;
}
