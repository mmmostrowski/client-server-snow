import * as React from "react";
import {useEffect, useState} from "react";
import {useSession, useSessionDispatch} from '../snow/SessionsProvider';
import {SnowAnimationConfiguration} from '../stream/snowEndpoint';
import {
    SessionErrorStatusUpdater,
    SessionPlayingStatusUpdater,
    SessionStatusUpdater,
    useSessionErrorStatusUpdater,
    useSessionPlayingStatusUpdater,
    useSessionStatusUpdater
} from '../snow/snowSessionStatus';
import SnowAnimation from "./SnowAnimationPlayer/SnowAnimation";
import AnimationCircularStatus from "./SnowAnimationPlayer/AnimationCircularStatus";
import AnimationSessionId from "./SnowAnimationPlayer/AnimationSessionId";
import AnimationControlButtons from "./SnowAnimationPlayer/AnimationControlButtons";
import {CannotStartError, CannotStopError, DetailsFromServer} from "../snow/SnowAnimationController";
import AnimationLinearProgress from "./AnimationLinearProgress";
import {animationSceneFromBase64, animationSceneToBase64} from "../snow/animationScenes";


interface Props {
    sessionIdx: number,
}

export default function SnowAnimationPlayer({ sessionIdx } : Props): JSX.Element {
    const setSessionStatus: SessionStatusUpdater = useSessionStatusUpdater(sessionIdx);
    const setSessionErrorStatus: SessionErrorStatusUpdater = useSessionErrorStatusUpdater(sessionIdx);
    const setSessionPlayingStatus: SessionPlayingStatusUpdater = useSessionPlayingStatusUpdater(sessionIdx);
    const dispatch = useSessionDispatch(sessionIdx);
    const [ isLocked, setIsLocked ] = useState(false);
    const {
        status, hasError, hasConfigError,
        isSessionExists, hasSessionIdError, cannotStartSession,
        presetName, sceneName, isInitializing,
        validatedWidth: width, validatedHeight: height, validatedFps: fps,
        foundWidth, foundHeight, foundFps, foundPresetName, foundSceneName,
    } = useSession(sessionIdx);
    const [ playAnimation, setPlayAnimation ] = useState<boolean>(status === 'playing' || status === 'buffering');
    const [ animationConfiguration, setAnimationConfiguration ] = useState<SnowAnimationConfiguration>({
        width, height, fps,
        presetName,
        scene: animationSceneToBase64(sceneName)
    });


    function handleStart(): void {
        if (isLocked) {
            return;
        }
        setPlayAnimation(true);
        if (isSessionExists) {
            setSessionStatus('initializing-existing', {
                fps: '' + foundFps,
                width: '' + foundWidth,
                height: '' + foundHeight,
                presetName: foundPresetName,
                sceneName: foundSceneName,
            });
            dispatch({ type: 'accept-or-reject-session-changes' });
            setAnimationConfiguration({
                width: foundWidth,
                height: foundHeight,
                fps: foundFps,
                presetName: foundPresetName,
                scene: animationSceneToBase64(foundSceneName),
            });
        } else {
            setSessionStatus('initializing-new');
            setAnimationConfiguration({
                width, height, fps,
                presetName,
                scene: animationSceneToBase64(sceneName),
            });
        }
    }

    async function handleStop(): Promise<void> {
        if (isLocked) {
            return;
        }
        setPlayAnimation(false);
        if (status !== 'error-cannot-stop') {
            setSessionStatus('stopped-not-found');
        }
    }


    function handleAnimationBuffering(startingBuffering: boolean, bufferPercent: number): void {
        if (startingBuffering) {
            setSessionStatus('buffering');
        }
        setSessionPlayingStatus(0, bufferPercent);
    }

    function handleAnimationPlaying(firstFrame: boolean, progressPercent: number, bufferPercent: number): void {
        if (firstFrame) {
            setSessionStatus('playing');
        }
        setSessionPlayingStatus(progressPercent, bufferPercent);
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
                foundSceneName: animationSceneFromBase64(response.scene),
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
                <AnimationCircularStatus sessionIdx={sessionIdx}/>
                <AnimationSessionId sessionIdx={sessionIdx} isEditing={(underEdit: boolean) => setIsLocked(underEdit)} />
                <AnimationControlButtons sessionIdx={sessionIdx} handleStart={handleStart} handleStop={handleStop} />
            </div>
            <SnowAnimation checkEveryMs={1300}
                           play={playAnimation}
                           sessionIdx={sessionIdx}
                           checkingEnabled={!hasConfigError}
                           configuration={animationConfiguration}
                           width={isSessionExists ? foundWidth : width}
                           height={isSessionExists ? foundHeight : height}
                           onChecking={handleChecking}
                           onFound={handleSessionFound}
                           onNotFound={handleSessionNotFound}
                           onBuffering={handleAnimationBuffering}
                           onPlaying={handleAnimationPlaying}
                           onError={handleAnimationError}
                           onFinish={handleStop}
            />
            <AnimationLinearProgress sessionIdx={sessionIdx} />
        </div>
    )
}

