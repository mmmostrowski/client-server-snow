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
    const session = useSnowSession(sessionIdx);
    const { sessionId, sessionIdError, validatedWidth, validatedHeight } = session;
    const dispatch = useSnowSessionDispatch(sessionIdx);

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
            <SnowCanvas session={session} />
        </div>
    )
}

