import * as React from "react";
import { useEffect, useState, useRef } from "react";
import AnimationPanel from './SnowAnimationPlayer/AnimationPanel';
import { useSnowSession } from '../snow/SnowSessionsProvider';
import { useSnowAnimation } from '../snow/snowAnimation';
import { SnowCanvasRefHandler } from  './SnowCanvas';
import {
    startSnowSession,
    stopSnowSession,
    fetchSnowDetails,
    SnowAnimationConfiguration,
    DetailsEndpointResponse,
} from '../stream/snowEndpoint';
import {
    useSessionStatusUpdater,
    SessionStatusUpdater,
    useSessionErrorStatusUpdater,
    SessionErrorStatusUpdater
} from '../snow/snowSessionStatus';


interface SnowAnimationProps {
    sessionIdx: number,
    refreshEveryMs?: number
}

export default function SnowAnimationPlayer({ sessionIdx, refreshEveryMs } : SnowAnimationProps): JSX.Element {
    const {
        status, hasError,
        sessionId, isSessionExists, hasSessionIdError, cannotStartSession,
        presetName, animationProgress,
        validatedWidth: width, validatedHeight: height, validatedFps: fps,
        foundWidth, foundHeight, foundFps, foundPresetName,
    } = useSnowSession(sessionIdx);
    const setSessionStatus: SessionStatusUpdater = useSessionStatusUpdater(sessionIdx);
    const setSessionErrorStatus: SessionErrorStatusUpdater = useSessionErrorStatusUpdater(sessionIdx);
    const [ refreshCounter, setRefreshCounter ] = useState<number>(0);
    const [ isLocked, setIsLocked ] = useState(false);
    const isActive = !hasSessionIdError && !isLocked;
    const canvasRef = useRef<SnowCanvasRefHandler>(null);
    const { startProcessing, stopProcessing } = useSnowAnimation(sessionIdx, canvasRef, handleAnimationFinished);

    function handleStart(): void {
        if (isSessionExists) {
            startExisting();
        } else {
            startNew();
        }

        function startExisting(): void {
            start({
                width: foundWidth,
                height: foundHeight,
                fps: foundFps,
                presetName: foundPresetName,
            })
            .catch(( error: Error ) => {
                if (!isActive) {
                    return;
                }
                setSessionErrorStatus(error, "error-cannot-start-existing");
            });
        }

        function startNew(): void {
            start({
                  width: width,
                  height: height,
                  fps: fps,
                  presetName: presetName,
            })
            .catch(( error: Error ) => {
                if (!isActive) {
                    return;
                }
                setSessionErrorStatus(error, "error-cannot-start-new");
            });
        }

        function start(animationParams: SnowAnimationConfiguration): Promise<void> {
            if (!isActive) {
                return Promise.resolve();
            }

            setSessionStatus('initializing');

            return startSnowSession(sessionId, animationParams)
                .then(startProcessing)
                .then(() => {
                    setSessionStatus('buffering', {
                        ...animationParams,
                        validatedWidth: animationParams.width,
                        validatedHeight: animationParams.height,
                        validatedFps: animationParams.fps,
                    });
                } );
        }

    }

    function handleStop(): void {
        stopProcessing({ allowForGoodbye: false });
        setSessionStatus('stopped-not-found');
        stopSnowSession(sessionId)
            .catch(( error: Error ) => {
                if (!isActive) {
                    return;
                }
                setSessionErrorStatus(error, "error-cannot-stop");
            });
    }

    function handleAnimationFinished(): void {
        setSessionStatus('stopped-not-found');
    }

    // Periodical session checking
    useEffect(() => {
        if (refreshEveryMs === undefined) {
            return;
        }
        const handler = setTimeout(() => {
              setRefreshCounter((c: number) => c + 1);
        }, refreshEveryMs);
        return () => {
            clearTimeout(handler);
        };
    }, [ refreshCounter ]);

    // Session checking
    useEffect(() => {
        if (hasSessionIdError) {
            setSessionErrorStatus ('Invalid session id');
            return;
        }

        if (status === 'buffering' || status === 'initializing' || cannotStartSession) {
            return;
        }

        if (status === 'stopped-not-checked' || hasError) {
            setSessionStatus('checking');
        }

        const controller = new AbortController();

        console.log("CHECKING DURING STATUS: " + status);
        fetchSnowDetails(sessionId, controller)
            .then(( data: DetailsEndpointResponse ) => {
                if (!data) {
                    return;
                }

                if (!data.running) {
                    if (status === 'checking') {
                        setSessionStatus('stopped-not-found');
                    }
                    if ( status === 'playing' ) {
                        stopProcessing({ allowForGoodbye: true });
                    }
                    return;
                }

                if (status === 'checking'
                    || status === 'stopped-not-found'
                    || status === 'stopped-not-checked' )
                {
                    setSessionStatus('stopped-found', {
                        foundWidth: data.width,
                        foundHeight: data.height,
                        foundFps: data.fps,
                        foundPresetName: data.presetName,
                    });
                }
            })
            .catch(( error : Error ) => {
                setSessionErrorStatus(error);
            });

        return () => {
            controller.abort()
        };
    }, [
        status, setSessionStatus, sessionId, refreshCounter,
        hasError, hasSessionIdError, setSessionErrorStatus,
        cannotStartSession,
    ]);

    return <AnimationPanel
        sessionIdx={sessionIdx}
        handleIsEditingSessionId={(underEdit: boolean) => setIsLocked(underEdit)}
        animationProgress={animationProgress}
        handleStart={handleStart}
        handleStop={handleStop}
        snowCanvasRef={canvasRef}
     />
}
