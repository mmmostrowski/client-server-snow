import * as React from "react";
import { useEffect, useState } from "react";
import { useSnowSession } from '../snow/SnowSessionsProvider';
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
import {DetailsFromServer} from "../snow/SnowAnimationController";


interface SnowAnimationProps {
    sessionIdx: number,
}

export default function SnowAnimationPlayer({ sessionIdx } : SnowAnimationProps): JSX.Element {
    const setSessionStatus: SessionStatusUpdater = useSessionStatusUpdater(sessionIdx);
    const setSessionErrorStatus: SessionErrorStatusUpdater = useSessionErrorStatusUpdater(sessionIdx);
    const [ isLocked, setIsLocked ] = useState(false);
    const [ playAnimation, setPlayAnimation ] = useState<boolean>(false);
    const [ animationConfiguration, setAnimationConfiguration ] = useState<SnowAnimationConfiguration>(null);
    const {
        status, hasError,
        sessionId, isSessionExists, hasSessionIdError, cannotStartSession,
        presetName, animationProgress,
        validatedWidth: width, validatedHeight: height, validatedFps: fps,
        foundWidth, foundHeight, foundFps, foundPresetName,
    } = useSnowSession(sessionIdx);


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

    function handleChecking(sessionId: string): void {
        console.log("handleChecking", sessionId, status);
        if (status === 'stopped-not-checked' || hasError) {
            setSessionStatus('checking');
        }
    }

    function handleSessionFound(response: DetailsFromServer): void {
        console.log("handleSessionFound", sessionId);

        if (status === 'buffering' || status === 'initializing' || cannotStartSession) {
            return;
        }

        if (status === 'checking'
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
        console.log("handleSessionNotFound", sessionId);

        if (status === 'buffering' || status === 'initializing' || cannotStartSession) {
            return;
        }

        if (status === 'checking') {
            setSessionStatus('stopped-not-found');
        }
        if (status === 'playing') {
            // stopProcessing({ allowForGoodbye: true });
        }
        return;
    }

    useEffect(() => {
        if (hasSessionIdError) {
            setSessionErrorStatus('Invalid session id');
            return;
        }
    }, [ hasSessionIdError, setSessionErrorStatus ]);


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
