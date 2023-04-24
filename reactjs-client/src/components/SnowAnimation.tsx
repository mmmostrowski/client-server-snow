import * as React from "react";
import { useEffect, useState, useRef } from "react";
import SnowCanvas from './SnowCanvas'
import LinearProgress from '@mui/material/LinearProgress';
import AnimationCircularProgress from './SnowAnimation/AnimationCircularProgress'
import AnimationControlButtons from './SnowAnimation/AnimationControlButtons'
import AnimationSessionId from './SnowAnimation/AnimationSessionId'
import { useSnowSession } from '../snow/SnowSessionsProvider'
import {
    fetchSnowDataDetails,
    SnowStreamDetailsResponse } from '../stream/snowEndpoint'
import {
    useSessionStatusUpdater,
    SessionStatusUpdater,
    useSessionErrorStatusUpdater,
    SessionErrorStatusUpdater } from '../snow/snowSessionStatus'


interface SnowAnimationProps {
    sessionIdx: number,
    refreshPeriodMs: number
}

export default function SnowAnimation({ sessionIdx, refreshPeriodMs } : SnowAnimationProps): JSX.Element {
    const {
        status, hasError,
        sessionId, hasSessionIdError, cannotStartSession,
        animationProgress,
    } = useSnowSession(sessionIdx);
    const setSessionStatus: SessionStatusUpdater = useSessionStatusUpdater(sessionIdx);
    const setSessionErrorStatus: SessionErrorStatusUpdater = useSessionErrorStatusUpdater(sessionIdx);
    const [ refreshCounter, setRefreshCounter ] = useState<number>(0);
    const sessionIdUnderEditRef = useRef<boolean>(false);


    // Periodical session checking
    useEffect(() => {
        const handler = setTimeout(() => {
             setRefreshCounter((c: number) => c + 1);
        }, refreshPeriodMs);
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

    return (
        <div className="snow-animation" >
            <div className="animation-header">
                <AnimationCircularProgress sessionIdx={sessionIdx}/>
                <AnimationSessionId sessionIdx={sessionIdx} underEditRef={sessionIdUnderEditRef} />
                <AnimationControlButtons sessionIdx={sessionIdx} isLockedRef={sessionIdUnderEditRef}/>
            </div>
            <SnowCanvas sessionIdx={sessionIdx} />
            <LinearProgress value={animationProgress}
                title="Animation progress"
                variant="determinate" />
        </div>
    )
}
