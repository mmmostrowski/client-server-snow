import * as React from "react";
import { useEffect, useState } from "react";
import AnimationPanel from './SnowAnimation/AnimationPanel'
import { useSnowSession } from '../snow/SnowSessionsProvider'
import {
    startStreamSnowData,
    stopStreamSnowData,
    fetchSnowDataDetails,
    SnowAnimationConfiguration,
    SnowStreamStartResponse,
    SnowStreamStopResponse,
    SnowStreamDetailsResponse,
} from '../stream/snowEndpoint'
import {
    useSessionStatusUpdater,
    SessionStatusUpdater,
    useSessionErrorStatusUpdater,
    SessionErrorStatusUpdater
} from '../snow/snowSessionStatus'


interface SnowAnimationProps {
    sessionIdx: number,
    refreshEveryMs?: number
}

export default function SnowAnimation({ sessionIdx, refreshEveryMs } : SnowAnimationProps): JSX.Element {
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
    });

    // Session checking
    useEffect(() => {
        if (hasSessionIdError) {
            setSessionErrorStatus ('Invalid session id');
            return;
        }

        if (cannotStartSession) {
            return;
        }

        if (status === 'stopped-not-checked' || hasError) {
            setSessionStatus('checking');
        }

        const controller = new AbortController();

        fetchSnowDataDetails(sessionId, controller)
            .then(( data: SnowStreamDetailsResponse ) => {
                if (!data.running) {
                    setSessionStatus('stopped-not-found');
                    return;
                }

                if (status === 'checking'
                    || status === 'stopped-not-found'
                    || status === 'stopped-not-checked')
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

            return startStreamSnowData({
                sessionId: sessionId,
                ...animationParams,
            })
            .then(( data: SnowStreamStartResponse ) => {
                if (!isActive) {
                    return;
                }

                setSessionStatus('playing', {
                    ...animationParams,
                    validatedWidth: animationParams.width,
                    validatedHeight: animationParams.height,
                    validatedFps: animationParams.fps,
                });
            })
        }

    }

    function handleStop(): void {
        stopStreamSnowData({
            sessionId: sessionId,
        })
        .then(( data: SnowStreamStopResponse ) => {
            if (!isActive) {
                return;
            }
            setSessionStatus('stopped-not-found');
        })
        .catch(( error: Error ) => {
            if (!isActive) {
                return;
            }
            setSessionErrorStatus(error, "error-cannot-stop");
        });
    }

    return <AnimationPanel
        sessionIdx={sessionIdx}
        handleIsEditingSessionId={(underEdit: boolean) => setIsLocked(underEdit)}
        animationProgress={animationProgress}
        handleStart={handleStart}
        handleStop={handleStop}
     />
}
