import * as React from "react";
import {useEffect, useRef} from "react";
import {SnowDrawing, SnowDrawingRefHandler} from "./SnowDrawing";
import {SnowAnimationConfiguration} from "../../stream/snowEndpoint";
import {DetailsFromServer} from "../../snow/SnowAnimationController";
import {animationConfig} from "../../config/animation";
import {useSession} from "../../snow/SessionsProvider";


interface Props {
    sessionIdx: number;
    play: boolean;
    configuration: SnowAnimationConfiguration;
    onFinish: () => void;
    onBuffering: (startingBuffering: boolean, percent: number) => void;
    onPlaying: (firstFrame: boolean, progress: number, bufferPercent: number) => void;
    onError: (error: Error) => void;
    onChecking: (sessionId: string, periodicCheck: boolean) => void;
    onFound: (response: DetailsFromServer, periodicCheck: boolean) => void;
    onNotFound: (periodicCheck: boolean) => void;
    checkingEnabled: boolean;
    checkEveryMs: number;
    width: number;
    height: number;
}

export default function SnowAnimation(props: Props): JSX.Element {
    const {
        sessionIdx,
        play,
        configuration,
        checkingEnabled, checkEveryMs,
        width, height,
        onBuffering, onPlaying, onFinish, onError, onChecking, onFound, onNotFound
    } = props;
    const canvasRef = useRef<SnowDrawingRefHandler>(null);
    const { snowController, sessionId } = useSession(sessionIdx);


    // Bind controller with session
    useEffect(() => {

        snowController.startPeriodicChecking(checkEveryMs);

    }, [ snowController, checkEveryMs ]);


    // Configure controller
    useEffect(() => {
        snowController.processInForeground({
            goodbyeTextTimeoutSec: animationConfig.goodbyeText.timeoutSec,
            sessionId: sessionId,
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
        return () => {
            snowController.continueProcessingInBackground();
        };
    }, [ onChecking, onFound, onNotFound, onBuffering, onPlaying, onFinish, onError, checkingEnabled ]);


    // Check session details
    useEffect(() => {
        const abortController = new AbortController();

        void snowController.checkDetails(abortController);

        return () => { abortController.abort() };
    }, [ snowController ]);


    // Start / Stop controller
    useEffect(() => {
        if (play) {
            if (!snowController.isRunning()) {
                void snowController.startProcessing(configuration);
            }
        } else {
            if (snowController.isRunning()) {
                void snowController.stopProcessing();
            }
        }
    }, [ play, configuration ]);


    return <SnowDrawing width={width} height={height} ref={canvasRef} />;
}
