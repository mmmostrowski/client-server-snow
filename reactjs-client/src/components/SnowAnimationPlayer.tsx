import * as React from "react";
import { useEffect, useState } from "react";
import { useSnowSession } from '../snow/SnowSessionsProvider';
import {
    fetchSnowDetails,
    SnowAnimationConfiguration,
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
    const [ playAnimation, setPlayAnimation ] = useState<boolean>(false);
    const [ animationConfiguration, setAnimationConfiguration ] = useState<SnowAnimationConfiguration>(null);

    function handleStart() {
        if (isLocked) {
            return;
        }
        setSessionStatus('initializing');
        setPlayAnimation(true);
        if (isSessionExists) {
            setAnimationConfiguration({
                width: foundWidth,
                height: foundHeight,
                fps: foundFps,
                presetName: foundPresetName,
            });
        } else {
            setAnimationConfiguration({
                width: width,
                height: height,
                fps: fps,
                presetName: presetName,
            });
        }
    }

    async function handleStop() {
        if (isLocked) {
            return;
        }
        setSessionStatus('stopped-not-found');
        setPlayAnimation(false);
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
            // console.log("CHECKING DURING STATUS: " + status);
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

        checkStatus()
            // .then(() => console.log('STATUS CHECKED'));

        return () => {
            controller.abort()
        };
    }, [
        status, setSessionStatus, sessionId, refreshCounter,
        hasError, hasSessionIdError, setSessionErrorStatus,
        cannotStartSession,
    ]);


    function handleAnimationBuffering(percent: number): void {
        setSessionStatus('buffering', {
            bufferLevel: percent,
            animationProgress: 0,
        });
    }

    function handleAnimationPlaying(progress: number, bufferPercent: number): void {
        setSessionStatus('playing', {
            animationProgress: progress,
            bufferLevel: bufferPercent,
        });
    }

    function handleAnimationError(error: Error): void {
        setSessionErrorStatus(error);
    }

    return (
        <div className="snow-animation" >
            <div className="animation-header">
                <AnimationCircularProgress sessionIdx={sessionIdx}/>
                <AnimationSessionId sessionIdx={sessionIdx} isEditing={(underEdit: boolean) => setIsLocked(underEdit)} />
                <AnimationControlButtons sessionIdx={sessionIdx} handleStart={handleStart} handleStop={handleStop} />
            </div>
            <SnowAnimation sessionIdx={sessionIdx}
                           play={playAnimation}
                           configuration={animationConfiguration}
                           onFinish={handleStop}
                           onBuffering={handleAnimationBuffering}
                           onPlaying={handleAnimationPlaying}
                           onError={handleAnimationError}
            />
            <LinearProgress value={animationProgress}
                            title="Animation progress"
                            variant="determinate" />
        </div>
    )
}
