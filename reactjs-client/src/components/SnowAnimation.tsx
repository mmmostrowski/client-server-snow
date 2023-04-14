import * as React from "react";
import { useSnowSession, useSnowSessionDispatch } from '../snow/SnowSessionsProvider'
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
    const status = session.status;
    const isStartActive = status === 'stopped' || status === 'error' || status == 'found';
    const isStopActive = status === 'buffering' || status === 'playing' || status == 'found';
    const isSessionIdInputActive = status === 'stopped' || status === 'checking' || status == 'found';
    const startLabel = status === 'found' ? "Attach" : "Start";

    return (
        <div className="snow-animation" >
            <div className="animation-header">
                <TextField
                    variant="standard"
                    label="Session id"
                    value={sessionId}
                    required
                    disabled={!isSessionIdInputActive}
                    error={sessionIdError != null}
                    helperText={sessionIdError}
                    onChange={ e => dispatch({
                        type: 'session-changed',
                        changes : {
                            sessionId: e.target.value
                        }
                    })}
                    onBlur={() => dispatch({ type: 'commit-session-changes' })}
                    style={{ minWidth: 70 }}
                />

                <Button className="start-button" variant="contained" disabled={!isStartActive}>{startLabel}</Button>
                <Button className="stop-button" variant="contained" disabled={!isStopActive}>Stop</Button>

                <CircularProgressWithLabel sessionIdx={sessionIdx}  />
            </div>
            <SnowCanvas session={session} />
            <Tooltip title="Animation progress" >
                <LinearProgress variant="determinate" value={session.animationProgress} />
            </Tooltip>
        </div>
    )
}

interface CircularProgressWithLabelProps {
    sessionIdx: number;
}

function CircularProgressWithLabel({ sessionIdx } : CircularProgressWithLabelProps) {
    const { status, errorMsg, bufferLevel } = useSnowSession(sessionIdx);
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
            title = "Click START button"
            break;
        case "buffering":
            color = "primary";
            progress = bufferLevel;
            insideText = `${Math.round(progress)}%`
            title = "Buffering..."
            break;
        case "playing":
            color = "success";
            progress = 100;
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
        <Box sx={{ position: 'relative', display: 'inline-flex', textAlign: "right", marginRight: 0, marginLeft: 'auto', padding: 1 }} >
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