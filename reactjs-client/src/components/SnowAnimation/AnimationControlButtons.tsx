import * as React from "react";
import { MutableRefObject } from 'react';
import Button from '@mui/material/Button';
import { useSnowSession, useDelayedSnowSession } from '../../snow/SnowSessionsProvider'
import {
    useSessionStatusUpdater,
    SessionStatusUpdater,
    useSessionErrorStatusUpdater,
    SessionErrorStatusUpdater } from '../../snow/snowSessionStatus'
import {
    startStreamSnowData,
    stopStreamSnowData,
    SnowAnimationConfiguration,
    SnowStreamStartResponse,
    SnowStreamStopResponse } from '../../stream/snowEndpoint'


interface AnimationControlButtonsProps {
    sessionIdx: number,
    isLockedRef: MutableRefObject<boolean>,
}

export default function AnimationControlButtons({ sessionIdx, isLockedRef }: AnimationControlButtonsProps): JSX.Element {
    const {
        isStopped,
        sessionId, hasSessionIdError, isSessionExists,
        presetName,
        validatedWidth: width, validatedHeight: height, validatedFps: fps,
        foundWidth, foundHeight, foundFps, foundPresetName,
    } = useSnowSession(sessionIdx);
    const { status } = useDelayedSnowSession(sessionIdx);
    const setSessionStatus: SessionStatusUpdater = useSessionStatusUpdater(sessionIdx);
    const setSessionErrorStatus: SessionErrorStatusUpdater = useSessionErrorStatusUpdater(sessionIdx);

    const isStartActive: boolean =
           isStopped
        || status === "error-cannot-start-new"
        || status === "error-cannot-start-existing"
        || status === "error-cannot-stop";

    const isStopActive: boolean =
           status === "buffering"
        || status === "playing";


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
                setSessionErrorStatus(error, "error-cannot-start-new");
            });
        }

        function start(animationParams: SnowAnimationConfiguration): Promise<void> {
            if (!isActive()) {
                return Promise.resolve();
            }

            setSessionStatus('initializing');

            return startStreamSnowData({
                sessionId: sessionId,
                ...animationParams,
            })
            .then(( data: SnowStreamStartResponse ) => {
                if (!isActive()) {
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
            setSessionStatus('stopped-not-found');
        })
        .catch(( error: Error ) => {
            if (!isActive()) {
                return;
            }
            setSessionErrorStatus(error, "error-cannot-stop");
        });
    }

    function isActive(): boolean {
        return !hasSessionIdError && !isLockedRef.current;
    }

    return (
        <>
            <Button
                className="start-button"
                variant="contained"
                title={isSessionExists ? "Active animation found on server. Attach to it!" : "Start new animation on server!"}
                onClick={handleStart}
                disabled={!isStartActive}>{isSessionExists ? "Play" : "Start"}</Button>

            <Button
                className="stop-button"
                variant="contained"
                title="Stop animation on server!"
                onClick={handleStop}
                disabled={!isStopActive}>Stop</Button>
        </>
    );
}