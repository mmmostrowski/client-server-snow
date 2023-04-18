import * as React from "react";
import { useEffect, useState, useMemo, useRef, MutableRefObject, FocusEvent, FocusEventHandler } from "react";
import { useSnowSession, useSnowSessionDispatch, useDelayedSnowSession } from '../snow/SnowSessionsProvider'
import { fetchSnowDataDetails } from '../stream/snowEndpoint'
import useSessionInput from '../snow/snowSessionInput'
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import Tooltip from '@mui/material/Tooltip';
import Typography from '@mui/material/Typography';
import CircularProgress from '@mui/material/CircularProgress';
import LinearProgress from '@mui/material/LinearProgress';
import SnowCanvas from './SnowCanvas'
import TextField from '@mui/material/TextField';


interface SnowAnimationProps {
    sessionIdx: number,
}

export default function SnowAnimation({ sessionIdx } : SnowAnimationProps) {
    const session = useSnowSession(sessionIdx);
    const dispatch = useSnowSessionDispatch(sessionIdx);
    const { sessionId, sessionIdError } = session;
    const { status : delayedStatus } = useDelayedSnowSession(sessionIdx);
    const [ refreshCounter, setRefreshCounter ] = useState(0);
    const status = session.status;
    const isStartActive = delayedStatus === 'stopped' || delayedStatus === 'error' || delayedStatus === 'found';
    const isStopActive = delayedStatus === 'buffering' || delayedStatus === 'playing' || delayedStatus === 'found';
    const isSessionIdInputActive = status === 'stopped' || status === 'checking' || status === 'found' || status == 'error';
    const startLabel = delayedStatus === 'found' ? "Attach" : "Start";
    const animationProgress = status === 'playing' ? session.animationProgress : 0;
    const hasSessionIdError = sessionIdError !== null

    useEffect(() => {
        const handler = setTimeout(() => {
            if (isSessionIdInputActive) {
                setRefreshCounter(refreshCounter + 1);
            }
        }, 1000);
        return () => {
            clearTimeout(handler);
        };
    });

    let ignored = false;
    useEffect(() => {
        if (ignored) {
            return;
        }

        if (hasSessionIdError) {
            dispatch({
                type : 'session-changed',
                sessionIdx: sessionIdx,
                changes: {
                    status: 'error',
                    errorMsg: 'Invalid session id',
                },
            });
            return;
        }

        dispatch({
            type : 'session-changed',
            sessionIdx: sessionIdx,
            changes: {
                status: 'checking',
            },
        });

        fetchSnowDataDetails(sessionId)
            .then((data : any) => {
                if (ignored) {
                    return;
                }
                if (data.running) {
                    dispatch({
                        type : 'session-changed',
                        sessionIdx: sessionIdx,
                        changes: {
                            status: 'found',
                            foundWidth: data.width,
                            foundHeight: data.height,
                            foundFps: data.fps,
                            foundPresetName: data.presetName,
                        },
                    });
                } else {
                    dispatch({
                        type : 'session-changed',
                        sessionIdx: sessionIdx,
                        changes: {
                            status: 'stopped',
                        },
                    });
                }
            })
            .catch(error => {
                console.error(error);
                dispatch({
                    type : 'session-changed',
                    sessionIdx: sessionIdx,
                    changes: {
                        status: 'error',
                        errorMsg: error.message,
                    },
                });
            });
        return () => { ignored = true };
    }, [ sessionId, sessionIdx, sessionIdError, refreshCounter ]);

    const [
        inputRef,
        handleSessionIdBlur,
        handleSessionIdChange
    ] = useSessionInput(sessionIdx, 'sessionId', sessionId);


    return (
        <div className="snow-animation" >
            <div className="animation-header">

                <CircularProgressWithLabel sessionIdx={sessionIdx}  />

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
                    onChange={handleSessionIdChange}
                    onBlur={handleSessionIdBlur}
                    style={{ minWidth: 70 }}
                />

                <Button className="start-button" variant="contained" disabled={!isStartActive}>{startLabel}</Button>
                <Button className="stop-button" variant="contained" disabled={!isStopActive}>Stop</Button>

            </div>
            <SnowCanvas session={session} />
            <Tooltip title="Animation progress" >
                <LinearProgress variant="determinate" value={animationProgress} />
            </Tooltip>
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
        case "found":
            color = "success";
            progress = 100;
            insideText = "exists"
            title = "Session exists!"
            fontWeight = 'bold'
            break;
        case "initializing":
            color = "primary";
            progress = null;
            insideText = "Init"
            title = "Connecting to server..."
            fontWeight = 'normal'
            break;
        case "stopped":
            color = "inherit";
            progress = 100
            insideText = "●"
            title = "Animation is stopped"
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
            title = "Playing"
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
            <CircularProgress variant={ progress !== null ? "determinate" : "indeterminate"} color={color} value={progress} />
            <Tooltip title={title} >
                <Box sx={{
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
                        sx={{ fontWeight: fontWeight }}
                     >
                        {insideText}
                    </Typography>
                </Box>
            </Tooltip>
        </Box>
    );
}