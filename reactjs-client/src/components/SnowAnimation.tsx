import * as React from "react";
import { useEffect, useState } from "react";
import { useSnowSession,
        useDelayedSnowSession,
        } from '../snow/SnowSessionsProvider'
import { fetchSnowDataDetails,
        startStreamSnowData,
        stopStreamSnowData,
        SnowAnimationConfiguration,
        SnowStreamStartResponse,
        SnowStreamStopResponse,
        SnowStreamDetailsResponse
        } from '../stream/snowEndpoint'
import useSessionInput from '../snow/snowSessionInput'
import { useSessionStatusUpdater,
        SessionStatusUpdater,
        useSessionErrorStatusUpdater,
        SessionErrorStatusUpdater } from '../snow/snowSessionStatus'
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import CircularProgress from '@mui/material/CircularProgress';
import LinearProgress from '@mui/material/LinearProgress';
import SnowCanvas from './SnowCanvas'
import TextField from '@mui/material/TextField';


interface SnowAnimationProps {
    sessionIdx: number,
    refreshPeriodMs: number
}

export default function SnowAnimation({ sessionIdx, refreshPeriodMs } : SnowAnimationProps): JSX.Element {
    const {
        status, hasError, isStopped,
        sessionId, sessionIdError, hasSessionIdError, isSessionExists, cannotStartSession,
        presetName, animationProgress,
        validatedWidth: width, validatedHeight: height, validatedFps: fps,
        foundWidth, foundHeight, foundFps, foundPresetName,
    } = useSnowSession(sessionIdx);
    const setSessionStatus: SessionStatusUpdater = useSessionStatusUpdater(sessionIdx);
    const setSessionErrorStatus: SessionErrorStatusUpdater = useSessionErrorStatusUpdater(sessionIdx);
    const { status: delayedStatus } = useDelayedSnowSession(sessionIdx);
    const [ refreshCounter, setRefreshCounter ] = useState<number>(0);

    const isStartActive: boolean =
           isStopped
        || hasError
        || status === 'checking';

    const isStopActive: boolean =
           delayedStatus === 'buffering'
        || delayedStatus === 'playing';

    const isSessionIdInputActive: boolean = isStartActive;


    function handleStart(): void {
        if (isSessionExists) {
            startExisting();
        } else {
            startNew();
        }

        function startExisting() {
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

        function startNew() {
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
            if (isSessionIdUnderEdit() || hasSessionIdError) {
                return;
            }

            setSessionStatus('initializing');

            return startStreamSnowData({
                sessionId: sessionId,
                ...animationParams,
            })
            .then(( data: SnowStreamStartResponse ) => {
                if (hasSessionIdError) {
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
            setSessionErrorStatus(error, "error-cannot-stop");
        });
    }

    function handleSessionIdChange(value: string) {
        setSessionStatus("stopped-not-checked");
    }

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
                    || status === 'stopped-not-checked') {
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
        hasError, cannotStartSession, hasSessionIdError, setSessionErrorStatus,
    ]);

    const [ inputRef, handleSessionIdInputBlur, handleSessionIdInputChange, isSessionIdUnderEdit ] =
            useSessionInput(sessionIdx, 'sessionId', sessionId, handleSessionIdChange);

    return (
        <div className="snow-animation" >
            <div className="animation-header">

                <CircularProgressWithLabel sessionIdx={sessionIdx} />

                <TextField
                    InputLabelProps={{ shrink: true }}
                    inputRef={inputRef}
                    variant="standard"
                    label="Session id"
                    defaultValue={sessionId}
                    required
                    disabled={!isSessionIdInputActive}
                    error={hasSessionIdError}
                    helperText={sessionIdError}
                    onChange={handleSessionIdInputChange}
                    onBlur={handleSessionIdInputBlur}
                    style={{ minWidth: 70 }}
                    autoComplete="off"
                />

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

            </div>
            <SnowCanvas sessionIdx={sessionIdx} />
            <LinearProgress value={animationProgress}
                title="Animation progress"
                variant="determinate" />
        </div>
    )
}

interface CircularProgressWithLabelProps {
    sessionIdx: number;
}

function CircularProgressWithLabel({ sessionIdx } : CircularProgressWithLabelProps) {
    const { status, errorMsg, bufferLevel, animationProgress } = useDelayedSnowSession(sessionIdx);

    let color : "primary" | "error" | "info" | "success" | "inherit" | "secondary" | "warning" = "primary";
    let fontWeight
    let progress
    let title
    let insideText

    switch (status) {
        case "checking":
            color = "secondary";
            progress = null;
            insideText = ""
            title = "Checking on server..."
            fontWeight = 'normal'
            break;
        case "stopped-not-checked":
        case "stopped-not-found":
            color = "inherit";
            progress = 100
            insideText = "●"
            title = "No such active animation found on server"
            break;
        case "stopped-found":
            color = "success";
            progress = 100;
            insideText = "exists"
            title = "Animation session exists on server!"
            fontWeight = 'bold'
            break;
        case "initializing":
            color = "primary";
            progress = null;
            insideText = "Init"
            title = "Starting animation on server..."
            fontWeight = 'normal'
            break;
        case "error-cannot-start-existing":
        case "error-cannot-start-new":
            color = "error";
            progress = 100;
            insideText = "error";
            title = `Error: ${errorMsg}`;
            fontWeight = "bold";
            break;
        case "buffering":
            color = "primary";
            progress = bufferLevel;
            insideText = `${Math.round(progress)}%`
            title = "Buffering..."
            break;
        case "playing":
            color = "success";
            progress = animationProgress;
            insideText = '▶'
            title = "Animation playing..."
            fontWeight = "bold";
            break;
        case "error":
            color = "error";
            progress = 100;
            insideText = "error";
            title = `Error: ${errorMsg}`;
            fontWeight = "bold";
            break;
    }

    return (
        <Box sx={{ position: 'relative', display: 'inline-flex', textAlign: "right", marginRight: 0, padding: 1 }} >
            <CircularProgress
                variant={ progress !== null ? "determinate" : "indeterminate"}
                color={color}
                value={progress}
            />
            <Box title={title}
                 sx={{
                    top: 0,
                    left: 0,
                    bottom: 0,
                    right: 0,
                    position: 'absolute',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    height: 56,
                }} >
                <Typography
                    fontSize="10px"
                    variant="caption"
                    component="div"
                    color={color}
                    sx={{ fontWeight: fontWeight }} >
                    {insideText}
                </Typography>
            </Box>
        </Box>
    );
}

