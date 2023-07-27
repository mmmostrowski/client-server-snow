import * as React from "react";
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import CircularProgress from '@mui/material/CircularProgress';
import {useDebouncedSession} from '../../snow/SessionsProvider'
import {useRefProbing} from "../../utils/useRefProbing";

interface Props {
    sessionIdx: number
}

export default function AnimationCircularStatus({ sessionIdx } : Props) {
    const { status, errorMsg, bufferLevelRef } = useDebouncedSession(sessionIdx);
    const bufferLevel = useRefProbing<number>(bufferLevelRef, 77);

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
        case "initializing-existing":
        case "initializing-new":
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
            title = errorMsg;
            break;
        case "buffering":
            color = "primary";
            progress = bufferLevel;
            insideText = `${Math.round(progress)}%`
            title = "Buffering..."
            break;
        case "playing":
            color = "success";
            progress = bufferLevel;
            insideText = '▶'
            title = `Playing ( buffer ${bufferLevel}% )`
            break;
        case "error-cannot-stop":
            color = "error";
            progress = 100;
            insideText = "error";
            title = errorMsg;
            break;
        case "error":
            color = "error";
            progress = 100;
            insideText = "error";
            title = errorMsg;
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

