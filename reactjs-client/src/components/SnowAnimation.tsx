import * as React from "react";
import { useEffect, useRef, useState, forwardRef, useContext, useReducer } from 'react';
import SnowCanvas from './SnowCanvas'
import { useSnowSession, useSnowSessionDispatch } from '../snow/SnowSessionProvider'
import { validateSnowSessionId } from '../snow/snowSessionValidator'
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';

interface SnowAnimationProps {
    presetName: string;
    isAnimationRunning: boolean;
    width: number;
    height: number;
    fps: number;
}

export default function SnowAnimation({ width, height, isAnimationRunning } : SnowAnimationProps) {
    const snowCanvasRef = useRef<SnowCanvas>(null);
    const { sessionId } = useSnowSession();
    const dispatch = useSnowSessionDispatch();

    useEffect(() => {
        snowCanvasRef.current.renderSnowFrame(null);
    });

    const sessionIdError = validateSnowSessionId(sessionId);

    return (
        <div className="snow-animation" >
            <div className="animation-header">
                <TextField
                    variant="standard"
                    label="Session id"
                    defaultValue={sessionId}
                    required
                    error={sessionIdError != null}
                    helperText={sessionIdError}
                    onChange={(e) => dispatch({
                        type: 'on-session-id-changed',
                        newSessionId : e.target.value
                    })}
                />
                {
                    isAnimationRunning
                        ? <Button className="stop-button" variant="contained">Stop</Button>
                        : <Button className="start-button" variant="contained">Start</Button>
                }
                </div>
            <SnowCanvas ref={snowCanvasRef} width={width} height={height} scaleFactorV={5} scaleFactorH={5} />
        </div>
    )
}

