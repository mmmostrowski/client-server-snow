import * as React from "react";
import { useEffect, useRef } from 'react';
import { useSnowSession, useSnowSessionDispatch } from '../snow/SnowSessionsProvider'
import { validateSnowSessionId } from '../snow/snowSessionValidator'
import Button from '@mui/material/Button';
import SnowCanvas from './SnowCanvas'
import TextField from '@mui/material/TextField';

interface SnowAnimationProps {
    sessionIdx: number,
    presetName: string;
    isAnimationRunning: boolean;
    fps: number;
}

export default function SnowAnimation({ sessionIdx, isAnimationRunning } : SnowAnimationProps) {
    const snowCanvasRef = useRef<SnowCanvas>(null);
    const { sessionId, sessionIdError, validatedWidth, validatedHeight } = useSnowSession(sessionIdx);
    const dispatch = useSnowSessionDispatch(sessionIdx);

    useEffect(() => {
        snowCanvasRef.current.renderSnowFrame(null);
    });

    return (
        <div className="snow-animation" >
            <div className="animation-header">
                <TextField
                    variant="standard"
                    label="Session id"
                    value={sessionId}
                    required
                    error={sessionIdError != null}
                    helperText={sessionIdError}
                    onChange={ e => dispatch({
                        type: 'session-changed',
                        changes : {
                            sessionId: e.target.value
                        }
                    })}
                    onBlur={() => dispatch({ type: 'commit-session-changes' })}
                />
                {
                    isAnimationRunning
                        ? <Button className="stop-button" variant="contained">Stop</Button>
                        : <Button className="start-button" variant="contained">Start</Button>
                }
                </div>
            <SnowCanvas ref={snowCanvasRef} width={validatedWidth} height={validatedHeight} scaleFactorV={5} scaleFactorH={5} />
        </div>
    )
}

