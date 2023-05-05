import * as React from "react";
import { useEffect, useState } from "react";
import {useSession, useSessionDispatch} from '../snow/SessionsProvider';
import { SnowAnimationConfiguration } from '../stream/snowEndpoint';
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
import {CannotStartError, CannotStopError, DetailsFromServer} from "../snow/SnowAnimationController";


interface SnowAnimationProps {
    sessionIdx: number,
}

export default function SnowAnimationPlayer({ sessionIdx } : SnowAnimationProps): JSX.Element {
    const setSessionStatus: SessionStatusUpdater = useSessionStatusUpdater(sessionIdx);
    const setSessionErrorStatus: SessionErrorStatusUpdater = useSessionErrorStatusUpdater(sessionIdx);
    const dispatch = useSessionDispatch(sessionIdx);
    const [ isLocked, setIsLocked ] = useState(false);
    const [ playAnimation, setPlayAnimation ] = useState<boolean>(false);
    const [ animationConfiguration, setAnimationConfiguration ] = useState<SnowAnimationConfiguration>(null);
    const {
        status, hasError, hasConfigError,
        sessionId, isSessionExists, hasSessionIdError, cannotStartSession,
        presetName, animationProgress, isInitializing,
        validatedWidth: width, validatedHeight: height, validatedFps: fps,
        foundWidth, foundHeight, foundFps, foundPresetName,
    } = useSession(sessionIdx);


    function handleStart() {
        if (isLocked) {
            return;
        }
        setPlayAnimation(true);
        if (isSessionExists) {
            setSessionStatus('initializing-existing', {
                fps: '' + foundFps,
                width: '' + foundWidth,
                height: '' + foundHeight,
            });
            dispatch({ type: 'accept-or-reject-session-changes' });
            setAnimationConfiguration({
                width: foundWidth,
                height: foundHeight,
                fps: foundFps,
                presetName: foundPresetName,
            });
        } else {
            setSessionStatus('initializing-new');
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
        setPlayAnimation(false);
        if (status !== 'error-cannot-stop') {
            setSessionStatus('stopped-not-found');
        }
    }


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
        setPlayAnimation(false);
        if (error instanceof CannotStartError) {
            if (isSessionExists) {
                setSessionErrorStatus(error, 'error-cannot-start-existing');
            } else {
                setSessionErrorStatus(error, 'error-cannot-start-new');
            }
        } else if (error instanceof CannotStopError) {
            setSessionErrorStatus(error, 'error-cannot-stop');
        } else {
            setSessionErrorStatus(error);
        }
    }

    function handleChecking(sessionId: string, periodicCheck: boolean): void {
        if ((periodicCheck && cannotStartSession) || hasSessionIdError) {
            return;
        }

        if (status === 'stopped-not-checked' || hasError) {
            setSessionStatus('checking');
        }
    }

    function handleSessionFound(response: DetailsFromServer): void {
        if (status === 'buffering' || isInitializing || cannotStartSession || hasSessionIdError) {
            return;
        }

        if (status === 'checking'
            || status === 'stopped-found'
            || status === 'stopped-not-found'
            || status === 'stopped-not-checked') {
            setSessionStatus('stopped-found', {
                foundWidth: response.width,
                foundHeight: response.height,
                foundFps: response.fps,
                foundPresetName: response.presetName,
            });
        }
    }

    function handleSessionNotFound(): void {
        if (status === 'buffering' || isInitializing || cannotStartSession || hasSessionIdError) {
            return;
        }

        if (status === 'checking' || status === 'stopped-found') {
            setSessionStatus('stopped-not-found');
        }
    }

    useEffect(() => {
        if (hasSessionIdError) {
            setSessionErrorStatus('Invalid session id');
        }
    }, [ hasSessionIdError, setSessionErrorStatus]);


    return (
        <div className="snow-animation" >
            <div className="animation-header">
                <AnimationCircularProgress sessionIdx={sessionIdx}/>
                <AnimationSessionId sessionIdx={sessionIdx} isEditing={(underEdit: boolean) => setIsLocked(underEdit)} />
                <AnimationControlButtons sessionIdx={sessionIdx} handleStart={handleStart} handleStop={handleStop} />
            </div>
            <SnowAnimation sessionId={sessionId}
                           width={isSessionExists ? foundWidth : width}
                           height={isSessionExists ? foundHeight : height}
                           play={playAnimation}
                           configuration={animationConfiguration}
                           checkingEnabled={!hasConfigError}
                           checkEveryMs={1300}
                           onFinish={handleStop}
                           onBuffering={handleAnimationBuffering}
                           onPlaying={handleAnimationPlaying}
                           onError={handleAnimationError}
                           onChecking={handleChecking}
                           onFound={handleSessionFound}
                           onNotFound={handleSessionNotFound}
            />
            <LinearProgress value={animationProgress}
                            title="Animation progress"
                            variant="determinate" />
        </div>
    )
}
