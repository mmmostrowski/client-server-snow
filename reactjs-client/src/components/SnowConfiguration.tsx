import * as React from "react";
import TextField from '@mui/material/TextField';
import Container from '@mui/material/Container';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';
import FormHelperText from '@mui/material/FormHelperText';

import { snowConstraints, useSnowSession, useSnowSessionDispatch } from '../snow/SnowSessionsProvider'

interface SnowConfigurationProps {
    sessionIdx: number
}


export default function SnowConfiguration({ sessionIdx } : SnowConfigurationProps) {
    const { width, height, fps, widthError, heightError, fpsError, presetName, isEditable } = useSnowSession(sessionIdx);
    const dispatch = useSnowSessionDispatch(sessionIdx);

    function handlePresetChange(e : any) {
        dispatch({
            type: 'session-changed',
            changes: {
                presetName: e.target.value
            }
        });
    }

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

    function handleFpsChange(e : any) {
        dispatch({
            type: 'session-changed',
            changes: {
                fps: e.target.value
            }
        });
    }

    return (
        <>
            <Container className="dupa" sx={{ padding: 2, paddingRight: [ 4, 4, 4 ] }} >
                <Select
                    id="demo-simple-select-helper"
                    value={presetName}
                    onChange={handlePresetChange}
                    variant="outlined"
                    size="small"
                    disabled={!isEditable}
                    sx={{ width: 180 }} >
                {
                    Object.entries(snowConstraints.presets).map(([presetName, presetLabel]) =>
                        <MenuItem key={presetName} value={presetName}>{presetLabel}</MenuItem>
                    )
                }
                </Select>
                <FormHelperText sx={{ pl: 2 }} >Animation preset</FormHelperText>
            </Container>
            <Container sx={{ padding: 2 }} >
                <TextField
                    value={width}
                    onChange={handleWidthChange}
                    onBlur={() => dispatch({ type: 'commit-session-changes' })}
                    inputProps={{ inputMode: 'numeric' }}
                    size="small"
                    disabled={!isEditable}
                    label="Width"
                    variant="outlined"
                    helperText={widthError != null ? widthError : 'Horizontal canvas size'}
                    error={widthError != null}
                    sx={{ width: 180 }}
                />
            </Container>
            <Container sx={{ padding: 2 }} >
                 <TextField
                    value={height}
                    onChange={handleHeightChange}
                    onBlur={() => dispatch({ type: 'commit-session-changes' })}
                    inputProps={{ inputMode: 'numeric' }}
                    size="small"
                    disabled={!isEditable}
                    label="Height"
                    variant="outlined"
                    helperText={heightError != null ? heightError : 'Vertical canvas size'}
                    error={heightError != null}
                    sx={{ width: 180 }}
                />
            </Container>
            <Container sx={{ padding: 2 }} >
                 <TextField
                    value={fps}
                    onChange={handleFpsChange}
                    onBlur={() => dispatch({ type: 'commit-session-changes' })}
                    inputProps={{ inputMode: 'numeric' }}
                    size="small"
                    disabled={!isEditable}
                    label="Fps"
                    variant="outlined"
                    helperText={fpsError != null ? fpsError : 'Frames per second'}
                    error={fpsError != null}
                    sx={{ width: 180 }}
                />
            </Container>
          </>
    );
}