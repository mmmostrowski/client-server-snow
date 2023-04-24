import * as React from "react";
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import CircularProgress from '@mui/material/CircularProgress';
import { useDelayedSnowSession } from '../../snow/SnowSessionsProvider'


export default function AnimationCircularProgress({ sessionIdx } : { sessionIdx: number }) {
    const { status, errorMsg, bufferLevel, animationProgress } = useDelayedSnowSession(sessionIdx);

    let color : "primary" | "error" | "info" | "success" | "inherit" | "secondary" | "warning" = "primary";
    let progress = 100;
    let title = "";
    let insideText = "";

    switch (status) {
        case "checking":
            color = "secondary";
            progress = null;
            insideText = ""
            title = "Checking on server..."
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
            break;
        case "initializing":
            color = "primary";
            progress = null;
            insideText = "Init"
            title = "Starting animation on server..."
            break;
        case "error-cannot-start-existing":
        case "error-cannot-start-new":
            color = "error";
            progress = 100;
            insideText = "error";
            title = `Error: ${errorMsg}`;
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
            break;
        case "error":
            color = "error";
            progress = 100;
            insideText = "error";
            title = `Error: ${errorMsg}`;
            break;
    }

    return (
        <Box className="snow-animation-circular-progress" >
            <CircularProgress
                value={progress}
                color={color}
                variant={ progress !== null ? "determinate" : "indeterminate"}
            />
            <Box className="snow-animation-circular-progress-inside" title={title} >
                <Typography color={color} >
                    {insideText}
                </Typography>
            </Box>
        </Box>
    );
}

