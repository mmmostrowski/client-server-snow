import * as React from "react";
import { useEffect, useState, useRef } from "react";
import { useSnowSession } from '../snow/SnowSessionsProvider';
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
import SnowAnimation from "./SnowAnimationPlayer/SnowAnimation";
import AnimationCircularProgress from "./SnowAnimationPlayer/AnimationCircularProgress";
import AnimationSessionId from "./SnowAnimationPlayer/AnimationSessionId";
import AnimationControlButtons from "./SnowAnimationPlayer/AnimationControlButtons";
import LinearProgress from "@mui/material/LinearProgress";


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

    async function handleStart() {
        if (isSessionExists) {
            await startExisting();
        } else {
            await startNew();
        }

        async function startExisting() {
            try {
                await start({
                    width: foundWidth,
                    height: foundHeight,
                    fps: foundFps,
                    presetName: foundPresetName,
                })
            } catch (error) {
                if (!isActive) {
                    return;
                }
                setSessionErrorStatus(error, "error-cannot-start-existing");
            }
        }

        async function startNew() {
            try {
                await start({
                    width: width,
                    height: height,
                    fps: fps,
                    presetName: presetName,
                })
            } catch (error) {
                if (!isActive) {
                    return;
                }
                setSessionErrorStatus(error, "error-cannot-start-new");
            }
        }

        async function start(animationParams: SnowAnimationConfiguration) {
            if (!isActive) {
                return Promise.resolve();
            }

            setSessionStatus('initializing');

            await startSnowSession(sessionId, animationParams);

            setSessionStatus('buffering', {
                ...animationParams,

                validatedWidth: animationParams.width,
                validatedHeight: animationParams.height,
                validatedFps: animationParams.fps,
            });
        }

    }

    async function handleStop() {
        try{
            // stopProcessing({ allowForGoodbye: false });
            setSessionStatus('stopped-not-found');
            await stopSnowSession(sessionId)
        } catch (error) {
            if (!isActive) {
                return;
            }
            setSessionErrorStatus(error, "error-cannot-stop");
        }
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

        async function checkStatus() {
            console.log("CHECKING DURING STATUS: " + status);
            try {
                const data = await fetchSnowDetails(sessionId, controller);

                if (!data) {
                    return;
                }

                if (!data.running) {
                    if (status === 'checking') {
                        setSessionStatus('stopped-not-found');
                    }
                    if (status === 'playing') {
                        // stopProcessing({ allowForGoodbye: true });
                    }
                    return;
                }

                if (status === 'checking'
                    || status === 'stopped-not-found'
                    || status === 'stopped-not-checked') {
                    setSessionStatus('stopped-found', {
                        foundWidth: data.width,
                        foundHeight: data.height,
                        foundFps: data.fps,
                        foundPresetName: data.presetName,
                    });
                }
            } catch( error ) {
                setSessionErrorStatus(error);
            }
        }

        const controller = new AbortController();

        checkStatus().then(() => console.log('STATUS CHECKED'));

        return () => {
            controller.abort()
        };
    }, [
        status, setSessionStatus, sessionId, refreshCounter,
        hasError, hasSessionIdError, setSessionErrorStatus,
        cannotStartSession,
    ]);


    function handleAnimationBuffering(progress: number) {

    }

    function handleAnimationPlaying(percent: number) {

    }

    return (
        <div className="snow-animation" >
            <div className="animation-header">
                <AnimationCircularProgress sessionIdx={sessionIdx}/>
                <AnimationSessionId sessionIdx={sessionIdx} isEditing={(underEdit: boolean) => setIsLocked(underEdit)} />
                <AnimationControlButtons sessionIdx={sessionIdx} handleStart={handleStart} handleStop={handleStop} />
            </div>
            <SnowAnimation sessionIdx={sessionIdx}
                           onFinish={handleAnimationFinished}
                           onBuffering={handleAnimationBuffering}
                           onPlaying={handleAnimationPlaying}
            />
            <LinearProgress value={animationProgress}
                            title="Animation progress"
                            variant="determinate" />
        </div>
    )
}
