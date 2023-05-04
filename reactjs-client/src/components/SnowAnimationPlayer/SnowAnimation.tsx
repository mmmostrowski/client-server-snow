import * as React from "react";
import {useEffect, useRef} from "react";
import {SnowDrawing, SnowDrawingRefHandler} from "./SnowDrawing";
import {SnowAnimationConfiguration} from "../../stream/snowEndpoint";
import {DetailsFromServer, SnowAnimationController} from "../../snow/SnowAnimationController";
import {animationConstraints} from "../../config/animationContraints";


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
    checkingEnabled: boolean;
    checkEveryMs: number;
    width: number;
    height: number;
}

export default function SnowAnimation(props: SnowAnimationProps): JSX.Element {
    const {
        sessionId,
        play,
        configuration,
        checkingEnabled, checkEveryMs,
        width, height,
        onBuffering, onPlaying, onFinish, onError, onChecking, onFound, onNotFound
    } = props;
    const canvasRef = useRef<SnowDrawingRefHandler>(null);
    const snowControllerRef = useRef<SnowAnimationController>(null);


    // Bind controller with session
    useEffect(() => {
        const abortController = new AbortController();

        snowControllerRef.current = new SnowAnimationController(sessionId);
        snowControllerRef.current.startPeriodicChecking(abortController, checkEveryMs);

        return () => {
            abortController.abort();
            snowControllerRef.current.destroy();
        };
    }, [ sessionId, checkEveryMs ]);


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
            checkingEnabled,
        });
    }, [ onChecking, onFound, onNotFound, onBuffering, onPlaying, onFinish, onError, checkingEnabled ]);


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


    return <SnowDrawing width={width} height={height} ref={canvasRef} />;
}
