import * as React from "react";
import {useEffect, useRef} from "react";
import {SnowDrawing, SnowDrawingRefHandler} from "./SnowDrawing";
import {SnowAnimationConfiguration} from "../../stream/snowEndpoint";
import {useSnowSession} from "../../snow/SnowSessionsProvider";
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
    sessionIdx: number;
    play: boolean;
    configuration: SnowAnimationConfiguration;
    onFinish: () => void;
    onBuffering: (percent: number) => void;
    onPlaying: (progress: number, bufferPercent: number) => void;
    onError: (error: Error) => void;
    onChecking: (sessionId: string) => void;
    onFound: (response: DetailsFromServer) => void;
    onNotFound: (response: DetailsFromServer) => void;
}

export default function SnowAnimation(props: SnowAnimationProps): JSX.Element {
    const {
        sessionIdx,
        play,
        configuration,
        onBuffering, onPlaying, onFinish, onError, onChecking, onFound, onNotFound
    } = props;
    const { sessionId} = useSnowSession(sessionIdx);
    const canvasRef = useRef<SnowDrawingRefHandler>(null);
    const snowControllerRef = useRef<SnowAnimationController>(new SnowAnimationController(sessionId));


    // Bind controller with session
    useEffect(() => {
        const abortController = new AbortController();
        void snowControllerRef.current.stopProcessing(abortController);
        snowControllerRef.current = new SnowAnimationController(sessionId);
        return () => { abortController.abort() };
    }, [ sessionId ]);


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
        });
    }, [ onChecking, onFound, onNotFound, onBuffering, onPlaying, onFinish, onError, canvasRef ]);


    // Check session details
    useEffect(() => {
        const abortController = new AbortController();

        void snowControllerRef.current.checkDetails(abortController);

        return () => { abortController.abort() };
    }, [ sessionId ]);



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


    return <SnowDrawing sessionIdx={sessionIdx} ref={canvasRef} />;
}
