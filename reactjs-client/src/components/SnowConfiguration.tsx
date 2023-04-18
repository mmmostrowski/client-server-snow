import * as React from "react";
import { useEffect, useRef } from "react";
import TextField from '@mui/material/TextField';
import Container from '@mui/material/Container';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';
import FormHelperText from '@mui/material/FormHelperText';
import useSessionInput from '../snow/snowSessionInput'

import { snowConstraints, useDelayedSnowSession, useSnowSessionDispatch, useSnowSession } from '../snow/SnowSessionsProvider'

interface SnowConfigurationProps {
    sessionIdx: number
}

export default function SnowConfiguration({ sessionIdx } : SnowConfigurationProps) {
    let { width: userWidth,
          height: userHeight,
          fps: userFps,
          presetName: userPresetName } = useSnowSession(sessionIdx);
    let { status,
          widthError, heightError, fpsError,
          foundWidth, foundHeight, foundFps, foundPresetName } = useDelayedSnowSession(sessionIdx);
    const dispatch = useSnowSessionDispatch(sessionIdx);
    const isEditable = status === 'stopped';
    const isAvailable = status !== 'initializing' && status !== 'checking' && status !== 'error';

    const width = status === 'found' ? foundWidth : userWidth;
    const height = status === 'found' ? foundHeight : userHeight;
    const fps = status === 'found' ? foundFps : userFps;
    const presetName = status === 'found' ? foundPresetName : userPresetName;

    function handlePresetChange(e : any) {
        dispatch({
            type: 'session-changed',
            changes: {
                presetName: e.target.value
            }
        });
    }

    return (
        <>
            <Container sx={{ padding: 2, paddingRight: [ 4, 4, 4 ] }} >
                <Select
                    id="demo-simple-select-helper"
                    value={isAvailable ? presetName : "?"}
                    onChange={handlePresetChange}
                    variant="outlined"
                    size="small"
                    disabled={!isEditable}
                    sx={{ width: 180 }} >
                    { isAvailable && Object.entries(snowConstraints.presets).map(([presetName, presetLabel]) =>
                            <MenuItem key={presetName} value={presetName}>{presetLabel}</MenuItem>
                        ) }
                    { !isAvailable && <MenuItem key="?" value="?">?</MenuItem> }
                </Select>
                <FormHelperText sx={{ pl: 2 }} >Animation preset</FormHelperText>
            </Container>
            <Container sx={{ padding: 2 }} >
                <ConfigNumberField
                    sessionIdx={sessionIdx}
                    varName="width"
                    isAvailable={isAvailable}
                    isEditable={isEditable}
                    value={width}
                    errorMsg={widthError}
                    label="Width"
                    helperText="Horizontal canvas size"
                />
            </Container>
            <Container sx={{ padding: 2 }} >
                <ConfigNumberField
                    sessionIdx={sessionIdx}
                    varName="height"
                    isAvailable={isAvailable}
                    isEditable={isEditable}
                    value={height}
                    errorMsg={heightError}
                    label="Height"
                    helperText="Vertical canvas size"
                />
            </Container>
            <Container sx={{ padding: 2 }} >
                <ConfigNumberField
                    sessionIdx={sessionIdx}
                    varName="fps"
                    isAvailable={isAvailable}
                    isEditable={isEditable}
                    value={fps}
                    errorMsg={fpsError}
                    label="Fps"
                    helperText="Frames per second"
                />
            </Container>
          </>
    );
}


type ConfigNumberFieldProps = {
    sessionIdx: number, varName: string, isAvailable: boolean, isEditable: boolean,
    value: string|number, errorMsg : string, label : string, helperText : string }

function ConfigNumberField(
    { sessionIdx, varName, isAvailable, isEditable, value, errorMsg, label, helperText }: ConfigNumberFieldProps) {

    const [
        inputRef,
        handleBlur,
        handleChange
    ] = useSessionInput(sessionIdx, varName, value);

    const restoreOnceAvailableRef = useRef(false);
    useEffect(() => {
        if (!isAvailable) {
            inputRef.current.value = '?';
            restoreOnceAvailableRef.current = true;
        } else if (restoreOnceAvailableRef.current) {
            restoreOnceAvailableRef.current = false;
            inputRef.current.value = value;
        }
    });

    return <TextField
        InputLabelProps={{ shrink: true }}
        inputRef={inputRef}
        defaultValue={isAvailable ? value : ""}
        onChange={handleChange}
        onBlur={handleBlur}
        inputProps={{ inputMode: 'numeric' }}
        size="small"
        disabled={!isEditable}
        label={label}
        variant="outlined"
        helperText={errorMsg != null ? errorMsg : helperText}
        error={errorMsg != null}
        sx={{ width: 180 }}
    />
}