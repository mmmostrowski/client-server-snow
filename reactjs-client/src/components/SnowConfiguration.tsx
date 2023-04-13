import * as React from "react";
import TextField from '@mui/material/TextField';
import Container from '@mui/material/Container';
import { useSnowSession, useSnowSessionDispatch } from '../snow/SnowSessionsProvider'

interface SnowConfigurationProps {
    sessionIdx: number
}


export default function SnowConfiguration({ sessionIdx } : SnowConfigurationProps) {
    const { sessionId, width, height, widthError, heightError } = useSnowSession(sessionIdx);
    const dispatch = useSnowSessionDispatch(sessionIdx);

    function handleWidthChange(e : any) {
        dispatch({
            type: 'session-changed',
            changes: {
                width: e.target.value
            }
        });
    }

    function handleHeightChange(e : any) {
        dispatch({
            type: 'session-changed',
            changes: {
                height: e.target.value
            }
        });
    }

    return (
        <>
            <Container sx={{ padding: 2 }} >
                <TextField
                    value={width}
                    onChange={handleWidthChange}
                    onBlur={() => dispatch({ type: 'commit-session-changes' })}
                    inputProps={{ inputMode: 'numeric' }}
                    size="small"
                    label="Width"
                    variant="outlined"
                    helperText={widthError != null ? widthError : 'Horizontal canvas size'}
                    error={widthError != null}
                />
            </Container>
            <Container sx={{ padding: 2 }} >
                 <TextField
                    value={height}
                    onChange={handleHeightChange}
                    onBlur={() => dispatch({ type: 'commit-session-changes' })}
                    inputProps={{ inputMode: 'numeric' }}
                    size="small"
                    label="Height"
                    variant="outlined"
                    helperText={heightError != null ? heightError : 'Vertical canvas size'}
                    error={heightError != null}
                />
            </Container>
          </>
    );
}